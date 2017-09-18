package com.yyp.musicplayer.server;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.yyp.musicplayer.interfaces.OnPlayCompleteListener;
import com.yyp.musicplayer.interfaces.OnMusicBufferingListener;

import java.io.IOException;

public class MusicPlayerService extends Service {

    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = ""; // 播放行为类型
        String musicUrl = ""; // 音乐Url
        // 获取意图传递的信息
        if(intent != null){
            action = intent.getStringExtra("action");
            musicUrl = intent.getStringExtra("musicUrl");
        }

        switch (action) {
            case "init":
                if(mediaPlayer == null){
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                }
                break;
            case "play":
                if (mediaPlayer != null) {
                    try{
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(musicUrl);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    }catch (IllegalArgumentException | IllegalStateException | IOException e){
                        e.printStackTrace();
                    }
                }
                break;
            case "rePlay":
                if(mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    try {
                        mediaPlayer.start();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "pause":
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    try {
                        mediaPlayer.pause();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case "stop":
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    try{
                        mediaPlayer.stop();
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                }
                break;
            case "release":
                if (mediaPlayer != null) {
                    try{
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                }
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public class MyBinder extends Binder {
        // 获取歌曲长度
        public int getMusicDuration(){
            int rtn = 0;
            if (mediaPlayer != null) {
                rtn = mediaPlayer.getDuration();
            }

            return rtn;
        }

        // 获取当前播放进度
        public int getMusicCurrentPosition(){
            int rtn = 0;
            if (mediaPlayer != null) {
                rtn = mediaPlayer.getCurrentPosition();
            }

            return rtn;
        }

        // 设置播放进度
        public void seekTo(int position) {
            if (mediaPlayer != null) {
                try{
                    mediaPlayer.seekTo(position);
                }catch (IllegalStateException e){
                    e.printStackTrace();
                }
            }
        }

        /**
         * 设置播放完成的监听事件
         */
        public void setOnCompleteListener(final OnPlayCompleteListener listener){
            // 完成后回调接口
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    listener.onPlayComplete();
                }
            });
        }

        /**
         * 设置歌曲缓存的监听事件
         */
        public void setOnMusicBufferingListener(final OnMusicBufferingListener listener){
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
                    listener.onMusicBuffering(i);
                }
            });
        }
    }
}
