package com.example.yue.music_listener.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yue.music_listener.R;
import com.example.yue.music_listener.adapter.MusicListAdapter;
import com.example.yue.music_listener.adapter.NavAdapter;
import com.example.yue.music_listener.bean.Mp3Info;
import com.example.yue.music_listener.bean.NavBean;
import com.example.yue.music_listener.bean.OnItemClickListener;
import com.example.yue.music_listener.service.MusicService;
import com.example.yue.music_listener.utils.AlbumUtil;
import com.example.yue.music_listener.utils.Blur;
import com.example.yue.music_listener.utils.Mp3Util;
import com.example.yue.music_listener.utils.SpUtils;
import com.example.yue.music_listener.view.ControlRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ControlRecyclerView recyclerView;
    private ImageView bgImage,navBottomPic,playImage,nextImage,perviousImage;
    private NavAdapter adapter;
    private TextView songText,nameText;
    private List<NavBean> navBeanList=new ArrayList<>();
    private RecyclerView musicList;
    private MusicListAdapter musicListAdapter;
    private RelativeLayout bottomLayout;
    private ChangeUIReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //recyclerView = (ControlRecyclerView) findViewById(R.id.nav_recycler_view);
        bgImage = (ImageView) findViewById(R.id.main_bg);
        musicList = (RecyclerView) findViewById(R.id.music_list_list);
        navBottomPic = (ImageView) findViewById(R.id.bottom_nav_pic);
        playImage = (ImageView) findViewById(R.id.bottom_nav_play);
        nextImage = (ImageView) findViewById(R.id.bottom_nav_next);
        perviousImage = (ImageView) findViewById(R.id.bottom_nav_previous);
        songText = (TextView) findViewById(R.id.bottom_nav_song);
        nameText = (TextView) findViewById(R.id.bottom_nav_name);
        bottomLayout = (RelativeLayout) findViewById(R.id.bottom_nav);
        setMarquee();
        //读写sd卡权限检查
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            findSongsAndInitList();
        }
        getMusicFromCache();

        initList();

        initRecyclerView();

        loadBg();

        initEvent();

        registerBroadCast();
    }

    /**
     * 从缓存获取上一次退出放的音乐
     */
    private void getMusicFromCache() {
        Mp3Util.currentMusic= (Mp3Info) SpUtils.getObject(this,"music");
        Mp3Util.posInAll=SpUtils.getInt(this,"musicPosInAll");
        if(Mp3Util.currentMusic!=null&&Mp3Util.posInAll!=-1){
            startPlayMusicService(Mp3Util.posInAll);
        }
    }

    /**
     * 设置TextView获取焦点，实现跑马灯
     */
    private void setMarquee() {
        songText.setSelected(true);
    }

    /**
     * 注册广播
     */
    private void registerBroadCast() {
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.yue.broadcast.CHANGE_UI");
        receiver=new ChangeUIReceiver();
        registerReceiver(receiver,intentFilter);
    }

    /**
     * 初始化点击事件
     */
    private void initEvent() {
        musicListAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                startPlayMusicService(position);
            }
        });

        playImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!MusicService.mediaPlayer.isPlaying()){
                    playImage.setImageDrawable(getResources().getDrawable(R.mipmap.pause_icon));
                }else {
                    playImage.setImageDrawable(getResources().getDrawable(R.mipmap.play_icon));
                }
                Intent intent=new Intent("com.yue.broadcast.OPERATE");
                intent.putExtra("code",MusicService.PLAY_PAUSE);
                sendBroadcast(intent);
            }
        });
        bottomLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Mp3Util.currentMusic!=null) {
                    Intent intent = new Intent(MainActivity.this, MusicActivity.class);
                    startActivity(intent);
                }
            }
        });
        nextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent("com.yue.broadcast.OPERATE");
                intent.putExtra("code",MusicService.NEXT);
                sendBroadcast(intent);
            }
        });
        perviousImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent("com.yue.broadcast.OPERATE");
                intent.putExtra("code",MusicService.PERVIOUS);
                sendBroadcast(intent);
            }
        });
    }

    /**
     * 启动播放音乐的服务
     * @param position
     */
    private void startPlayMusicService(int position) {
        Mp3Util.currentMusic=Mp3Util.getInstance().get(position);
        //设置全局变量，当前播放歌曲在所有歌曲列表中的位置
        Mp3Util.posInAll=position;

        Intent intent=new Intent(MainActivity.this, MusicService.class);
        startService(intent);

        setBottomNav(Mp3Util.getInstance().get(position));
    }

    /**
     * 设置底部播放显示
     */
    private void setBottomNav(Mp3Info mp3) {
        navBottomPic.setImageBitmap(AlbumUtil.getArtwork(this,mp3.getSong_id(),mp3.getAlbum_id(),true));
        songText.setText(mp3.getTitle());
        nameText.setText(mp3.getArtist());
        if (MusicService.mediaPlayer.isPlaying()){
            playImage.setImageDrawable(getResources().getDrawable(R.mipmap.pause_icon));
        }else {
            playImage.setImageDrawable(getResources().getDrawable(R.mipmap.play_icon));
        }

        setUserInfo(mp3);
    }

    /**
     * 设置用户喜好
     */
    private void setUserInfo(Mp3Info mp3) {
        SpUtils.putObject(this,"music",mp3);
        SpUtils.putInt(this,"musicPosInAll",Mp3Util.posInAll);
    }

    /**
     * 加载高斯模糊过的背景图片
     */
    private void loadBg() {
        //单独开启一个线程进行高斯模糊
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap=BitmapFactory.decodeResource(getResources(),R.mipmap.bg_default);
                //高斯模糊
                bitmap= Blur.fastBlur(bitmap,0.1f,5);
                final Bitmap finalBitmap = bitmap;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bgImage.setImageBitmap(finalBitmap);
                    }
                });
            }
        }).start();
    }

    /**
     * 初始化RecyclerView
     */
    private void initRecyclerView() {
        GridLayoutManager manager=new GridLayoutManager(this,2);
        adapter=new NavAdapter(this,navBeanList);
        //recyclerView.setAdapter(adapter);
        //recyclerView.setLayoutManager(manager);
    }

    /**
     * 初始化导航栏目
     */
    private void initList() {
        NavBean navBean=new NavBean();
        navBean.setPic(R.mipmap.music_icon);
        navBean.setText(getResources().getString(R.string.my_music));
        navBeanList.add(navBean);
        navBean=new NavBean();
        navBean.setPic(R.mipmap.favourite_icon);
        navBean.setText(getResources().getString(R.string.my_favourite));
        navBeanList.add(navBean);
        navBean=new NavBean();
        navBean.setPic(R.mipmap.list_icon);
        navBean.setText(getResources().getString(R.string.my_list));
        navBeanList.add(navBean);
        navBean=new NavBean();
        navBean.setPic(R.mipmap.history_icon);
        navBean.setText(getResources().getString(R.string.my_history));
        navBeanList.add(navBean);
    }

    /**
     * 通过内容提供者获取系统中的音乐未文件信息,并初始化导航RecyclerView
     * @return
     */
    private void findSongsAndInitList(){
        ContentResolver contentResolver=getContentResolver();
        Cursor cursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        for (int i = 0; i < cursor.getCount(); i++) {
            Mp3Info mp3Info = new Mp3Info();                               //新建一个歌曲对象,将从cursor里读出的信息存放进去,直到取完cursor里面的内容为止.
            cursor.moveToNext();


            long id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));   //音乐id

            String title = cursor.getString((cursor
                    .getColumnIndex(MediaStore.Audio.Media.TITLE)));//音乐标题

            String artist = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ARTIST));//艺术家

            long duration = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DURATION));//时长

            long size = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.SIZE));  //文件大小

            String url = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.DATA));  //文件路径

            String album = cursor.getString(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM)); //唱片图片

            long album_id = cursor.getLong(cursor
                    .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)); //唱片图片ID

            int isMusic = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));//是否为音乐

            int song_id=cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Audio.Media._ID));

            if (isMusic != 0 && duration/(1000 * 60) >= 1) {     //只把1分钟以上的音乐添加到集合当中
                mp3Info.setId(id);
                mp3Info.setTitle(title);
                mp3Info.setArtist(artist);
                mp3Info.setDuration(duration);
                mp3Info.setSize(size);
                mp3Info.setUrl(url);
                mp3Info.setAlbum(album);
                mp3Info.setAlbum_id(album_id);
                mp3Info.setSong_id(song_id);
                Mp3Util.getInstance().add(mp3Info);
            }
        }

        initMusicList();
    }

    /**
     * 初始化音乐RecyclerView列表
     */
    private void initMusicList() {
        musicListAdapter=new MusicListAdapter(this,Mp3Util.getInstance());
        RecyclerView.LayoutManager manager=new LinearLayoutManager(this);
        musicList.setAdapter(musicListAdapter);
        musicList.setLayoutManager(manager);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 0:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    findSongsAndInitList();
                }
        }
    }

    /**
     * 广播，用于接收服务切换歌曲后改变UI显示
     */
    class ChangeUIReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            setBottomNav(Mp3Util.currentMusic);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * 在主界面按返回键不是关闭应用程序而是返回桌面
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MusicService.mediaPlayer.isPlaying()){
            playImage.setImageDrawable(getResources().getDrawable(R.mipmap.pause_icon));
        }else {
            playImage.setImageDrawable(getResources().getDrawable(R.mipmap.play_icon));
        }
    }
}
