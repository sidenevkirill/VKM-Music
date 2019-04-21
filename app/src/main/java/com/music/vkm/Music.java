package com.music.vkm;

import java.io.Serializable;

/**
 * Created by halez on 22.05.2017.
 */

public class Music implements Serializable {
    private String artist;
    private String url;
    private String title;
    private String lyrics_id;
    private String pic;
    private String data_id;
    private String save;
    private String time;
    private String your;
    private String type;
    private String add_hash;
    private String del_hash;
    private String res_hash;
    private String name_cache;

    public String getName_cache() {
        return name_cache;
    }

    public void setName_cache(String name_cache) {
        name_cache = name_cache.replaceAll("Info", "mp3");
        this.name_cache = name_cache;
    }

    Music() {
        artist = "null";
        url = "null";
        title = "null";
        lyrics_id = "null";
        pic = "null";
        data_id = "null";
        save = "null";
        time = "null";
        your = "null";
        type = "null";
        add_hash = "null";
        del_hash = "null";
        res_hash = "null";
        name_cache = "null";
    }

    Music(String artist, String time, String url, String title, String lyrics_id, String pic, String data_id, String save, String your, String type, String add_hash, String del_hash, String res_hash) {
        this.artist = artist;
        this.url = url;
        this.title = title;
        this.lyrics_id = lyrics_id;
        this.pic = pic;
        this.data_id = data_id;
        this.save = save;
        this.your = your;
        this.time = time;
        this.type = type;
        this.add_hash = add_hash;
        this.del_hash = del_hash;
        this.res_hash = res_hash;
    }


    void unSave() {
        this.save = "false";
    }

    void setSave() {
        this.save = "true";
    }

    String getInString() {
        return "|artist|" + artist + "|time|" + time + "|url|" + url + "|title|" + title + "|lyrics_id|" + lyrics_id + "|pic|" + pic + "|id|" + data_id + "|save|" + save + "||";
    }

    void setFromString(String mus) {
        artist = wstr.pars("|artist|", mus, "|");
        time = wstr.pars("|time|", mus, "|");
        url = wstr.pars("|url|", mus, "|");
        title = wstr.pars("|title|", mus, "|");
        lyrics_id = wstr.pars("|lyrics_id|", mus, "|");
        pic = wstr.pars("|pic|", mus, "|");
        data_id = wstr.pars("|id|", mus, "|");
        save = wstr.pars("|save|", mus, "|");
    }

    public void setUrl(String tmp) {
        url = tmp;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String tmp) {
        title = tmp;
    }

    public String getType() {
        return type;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setData_id(String id) {
        this.data_id = id;
    }

    public void setLyrics_id(String lyrics_id) {
        this.lyrics_id = lyrics_id;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public void setSave(String save) {
        this.save = save;
    }

    public void setAdd_hash(String add_hash) {
        this.add_hash = add_hash;
    }

    public void setDel_hash(String del_hash) {
        this.del_hash = del_hash;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setYour(String your) {
        this.your = your;
    }

    public void setRes_hash(String res_hash) {
        this.res_hash = res_hash;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getArtist() {
        return artist;
    }

    public String getData_id() {
        return data_id;
    }

    public String getLyrics_id() {
        return lyrics_id;
    }

    public String getPic() {
        return pic;
    }

    public String getAdd_hash() {
        return add_hash;
    }

    public String getSave() {
        return save;
    }

    public String getTime() {
        return time;
    }

    public String getYour() {
        return your;
    }

    public String getDel_hash() {
        return del_hash;
    }

    public String getRes_hash() {
        return res_hash;
    }


    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

}
