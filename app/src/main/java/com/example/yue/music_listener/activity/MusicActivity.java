package com.example.yue.music_listener.activity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.yue.music_listener.R;
import com.example.yue.music_listener.bean.Mp3Info;
import com.example.yue.music_listener.service.MusicService;
import com.example.yue.music_listener.utils.AlbumUtil;
import com.example.yue.music_listener.utils.Blur;
import com.example.yue.music_listener.utils.Mp3Util;

import de.hdodenhof.circleimageview.CircleImageView;

public class MusicActivity extends AppCompatActivity {
    private static final int ROTATE_TIME=20000;
    private CircleImageView musicImage;
    private ImageView bgImage,playMode,previousImage,nextImage,playImage,menuImage;
    private ChangeMusicInfoUIReceiver receiver;
    private ObjectAnimator ob;
    private Toast cycleToast;
    private Toast singleToast;
    private Toast randomToast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        musicImage=(CircleImageView) findViewById(R.id.music_image_icon);
        bgImage=(ImageView)findViewById(R.id.music_bg);
        playMode=(ImageView)findViewById(R.id.music_play_mode);
        previousImage=(ImageView)findViewById(R.id.music_play_previous);
        nextImage=(ImageView)findViewById(R.id.music_play_next);
        playImage=(ImageView)findViewById(R.id.music_play);
        menuImage=(ImageView)findViewById(R.id.music_menu);
        setAnim();

        init();

        initToast();

        loadBg();

        setDefaultBg();

        initUI();

        initEvent();

        initBroadCast();
    }

    /**
     * 初始化Toast实例
     */
    private void initToast() {
        cycleToast=Toast.makeText(MusicActivity.this,"列表循环",Toast.LENGTH_SHORT);
        singleToast=Toast.makeText(MusicActivity.this,"单曲循环",Toast.LENGTH_SHORT);
        randomToast=Toast.makeText(MusicActivity.this,"随机播放",Toast.LENGTH_SHORT);
    }

    private void init() {
        setMusicPic();
        if(MusicService.mediaPlayer.isPlaying()){
            ob.start();
        }
    }

    /**
     * 设置动画
     */
    private void setAnim() {
        ob = ObjectAnimator.ofFloat(musicImage, "Rotation", 0f , 360f).setDuration(ROTATE_TIME);
        ob.setInterpolator(new LinearInterpolator());
        ob.setRepeatCount(ValueAnimator.INFINITE);
        ob.setRepeatMode(ValueAnimator.RESTART);
    }

    /**
     * 注册广播
     */
    private void initBroadCast() {
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.yue.broadcast.CHANGE_UI");
        receiver=new ChangeMusicInfoUIReceiver();
        registerReceiver(receiver,intentFilter);
    }

    /**
     * 初始化点击事件
     */
    private void initEvent() {
        playImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MusicService.mediaPlayer.isPlaying()){
                    playImage.setImageDrawable(getResources().getDrawable(R.mipmap.pause_icon));
                    startRotate();
                }else {
                    playImage.setImageDrawable(getResources().getDrawable(R.mipmap.play_icon));
                    stopRotate();

                }
                Intent intent=new Intent("com.yue.broadcast.OPERATE");
                intent.putExtra("code",MusicService.PLAY_PAUSE);
                sendBroadcast(intent);
            }
        });
        nextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent("com.yue.broadcast.OPERATE");
                intent.putExtra("code",MusicService.NEXT);
                sendBroadcast(intent);
                setDefaultBg();
            }
        });
        previousImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent("com.yue.broadcast.OPERATE");
                intent.putExtra("code",MusicService.PERVIOUS);
                sendBroadcast(intent);
                setDefaultBg();
            }
        });
        playMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (Mp3Util.playModeCode){
                    case Mp3Util.SINGLE_MODE:
                        Mp3Util.playModeCode=Mp3Util.CYCLE_MODE;
                        playMode.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.cycle_icon));
                        singleToast.cancel();
                        randomToast.cancel();
                        cycleToast.show();
                        break;
                    case Mp3Util.CYCLE_MODE:
                        Mp3Util.playModeCode=Mp3Util.RANDOM_MODE;
                        playMode.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.random_icon));
                        singleToast.cancel();
                        cycleToast.cancel();
                        randomToast.show();
                        break;
                    case Mp3Util.RANDOM_MODE:
                        Mp3Util.playModeCode=Mp3Util.SINGLE_MODE;
                        playMode.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.single_cycle_icon));
                        cycleToast.cancel();
                        randomToast.cancel();
                        singleToast.show();
                        break;
                }
            }
        });
    }

    /**
     * 初始化UI显示
     */
    private void initUI() {
        if(MusicService.mediaPlayer.isPlaying()){
            playImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.pause_icon));
        }else {
            playImage.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.play_icon));
        }

        //设置播放模式图标
        switch (Mp3Util.playModeCode){
            case Mp3Util.RANDOM_MODE:
                playMode.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.random_icon));
                break;
            case Mp3Util.CYCLE_MODE:
                playMode.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.cycle_icon));
                break;
            case Mp3Util.SINGLE_MODE:
                playMode.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.single_cycle_icon));
                break;
        }
    }

    /**
     * 加载背景
     */
    private void loadBg() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap= AlbumUtil.getArtwork(MusicActivity.this, Mp3Util.currentMusic.getSong_id(),Mp3Util.currentMusic.getAlbum_id(),false);
                //如果有专辑封面就设置为背景
                if(bitmap!=null) {
                    //高斯模糊
                    bitmap = Blur.fastBlur(bitmap, 0.15f, 4);
                    final Bitmap finalBitmap = bitmap;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bgImage.setImageBitmap(finalBitmap);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 设置默认背景图片
     */
    private void setDefaultBg() {
        Bitmap defaultBg=BitmapFactory.decodeResource(getResources(),R.mipmap.music_bg_default);
        defaultBg=Blur.fastBlur(defaultBg,0.3f,1);
        bgImage.setImageBitmap(defaultBg);
    }

    /**
     * 无限旋转唱片
     */
    private void startRotate() {
        ob.start();
    }

    /**
     * 设置唱片图片
     */
    private void setMusicPic() {
        musicImage.setImageBitmap(AlbumUtil.getArtwork(this,Mp3Util.currentMusic.getSong_id(),Mp3Util.currentMusic.getAlbum_id(),true));
    }

    /**
     * 暂停旋转
     */
    private void stopRotate(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ob.pause();
        }
    }

    class ChangeMusicInfoUIReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            setMusicPic();
            loadBg();
            changePlayImage();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     *
     */
    private void changePlayImage(){
        if (MusicService.mediaPlayer.isPlaying()){
            playImage.setImageDrawable(getResources().getDrawable(R.mipmap.pause_icon));
        }else {
            playImage.setImageDrawable(getResources().getDrawable(R.mipmap.play_icon));
        }
    }
}
