package com.yyp.musicplayer.bean;

public class MusicList {

    private String name;
    private String bg_img;
    private String record_img;
    private String audio_url;

    public MusicList(String name, String bg_img, String record_img, String audio_url) {
        this.name = name;
        this.bg_img = bg_img;
        this.record_img = record_img;
        this.audio_url = audio_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBg_img() {
        return bg_img;
    }

    public void setBg_img(String bg_img) {
        this.bg_img = bg_img;
    }

    public String getRecord_img() {
        return record_img;
    }

    public void setRecord_img(String record_img) {
        this.record_img = record_img;
    }

    public String getAudio_url() {
        return audio_url;
    }

    public void setAudio_url(String audio_url) {
        this.audio_url = audio_url;
    }
}
