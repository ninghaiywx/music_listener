package com.example.yue.music_listener.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.example.yue.music_listener.bean.Mp3Info;
import com.example.yue.music_listener.utils.Mp3Util;

import java.io.IOException;

public class MusicService extends Service {
    public static final int NEXT=0;
    public static final int PERVIOUS=1;
    public static final int PLAY_PAUSE=2;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    private String previousUrl="";
    private Mp3Info music;
    private OperateReceiver receiver;
    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playNextByMode("next");
            }
        });

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction("com.yue.broadcast.OPERATE");
        receiver=new OperateReceiver();
        registerReceiver(receiver,intentFilter);
    }

    /**
     * 随机获取一首歌曲的位置
     * @return
     */
    private int getRandomPos(){
        int musicLength= Mp3Util.getInstance().size();
        int pos= Mp3Util.posInAll;
        while (pos==Mp3Util.posInAll){
            pos=(int) (Math.random()*musicLength);
        }
        Mp3Util.posInAll=pos;
        return pos;
    }
    /**
     * 播放下一首
     */
    private void playNext(int pos) {
        music=Mp3Util.getInstance().get(pos);
        initMediaPlayer();
    }

    /**
     * 发送广播通知UI线程改变UI信息
     */
    private void sendBroadCastToUI(Mp3Info music){
        Intent intent=new Intent("com.yue.broadcast.CHANGE_UI");
        Mp3Util.currentMusic=music;
        sendBroadcast(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        music= Mp3Util.currentMusic;
        if(music!=null) {
            initMediaPlayer();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 初始化音乐播放器
     */
    private void initMediaPlayer() {
        if(!previousUrl.equals(music.getUrl())) {
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(music.getUrl());
                mediaPlayer.prepare();
                previousUrl = music.getUrl();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        playMusic();
    }

    /**
     * 暂停音乐
     */
    private void pauseMusic(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    /**
     * 播放音乐
     */
    private void playMusic(){
        sendBroadCastToUI(music);
        if(!mediaPlayer.isPlaying()&&!Mp3Util.isFirstOpen){
            mediaPlayer.start();
        }else if(!mediaPlayer.isPlaying()&&Mp3Util.isFirstOpen){
            Mp3Util.isFirstOpen=false;
        }
    }

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    class OperateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int code=intent.getIntExtra("code",0);
                switch (code) {
                    case PLAY_PAUSE:
                        if (MusicService.mediaPlayer.isPlaying()) {
                            MusicService.mediaPlayer.pause();
                        } else {
                            MusicService.mediaPlayer.start();
                        }
                        break;
                    case NEXT:
                        playNextByMode("next");
                        break;
                    case PERVIOUS:
                        playNextByMode("previous");
                        break;
            }
        }
    }

    /**
     * 根据当前切歌模式改变歌曲
     * @param op 上一首或者下一首
     */
    private void playNextByMode(String op) {
        int pos=0;
        switch (Mp3Util.playModeCode){
            case Mp3Util.RANDOM_MODE:
                pos = getRandomPos();
                playNext(pos);
                break;
            case Mp3Util.CYCLE_MODE:
                if(op.equals("next")){
                    pos=getNextPos();
                }else if(op.equals("previous")){
                    pos=getPrevious();
                }
                playNext(pos);
                break;
            case Mp3Util.SINGLE_MODE:
                playNext(Mp3Util.posInAll);
                break;
        }
    }

    private int getPrevious() {
        int pos= Mp3Util.posInAll-1;
        if(pos<0){
            pos=Mp3Util.getInstance().size()-1;
        }
        Mp3Util.posInAll=pos;
        return pos;
    }

    /**
     * 获取下一首歌的位置
     * @return
     */
    private int getNextPos() {
        int pos= Mp3Util.posInAll+1;
        if(pos>=Mp3Util.getInstance().size()){
            pos=0;
        }
        Mp3Util.posInAll=pos;
        return pos;
    }
}
