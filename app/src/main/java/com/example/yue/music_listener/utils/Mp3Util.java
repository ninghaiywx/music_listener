package com.example.yue.music_listener.utils;

import com.example.yue.music_listener.bean.Mp3Info;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yue on 2017/9/11.
 */

public class Mp3Util {
    public static final int SINGLE_MODE=0x100;
    public static final int CYCLE_MODE=0x110;
    public static final int RANDOM_MODE=0x120;
    public static int playModeCode=RANDOM_MODE;
    public static Mp3Info currentMusic;
    public volatile static boolean isFirstOpen=true;
    public volatile static int posInAll=-1;
    private static List<Mp3Info> mp3Infos;
    public static List<Mp3Info> getInstance(){
        if(mp3Infos==null){
            synchronized (Mp3Util.class){
                if(mp3Infos==null){
                    mp3Infos=new ArrayList<>();
                }
            }
        }
        return mp3Infos;
    }
}
