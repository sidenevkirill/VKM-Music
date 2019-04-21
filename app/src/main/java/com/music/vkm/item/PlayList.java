package com.music.vkm.item;

import java.io.Serializable;

/**
 * Created by halez on 06.01.2018.
 */

public class PlayList implements Serializable {

    private String url;
    private String itemTitle;
    private String itemCover;

    public PlayList() {
    }

    public PlayList(String url, String itemTitle, String itemCover) {
        this.url = url;
        this.itemTitle = itemTitle;
        this.itemCover = itemCover;
    }

    void setUrl(String t) {
        url = t;
    }

    void setItemTitle(String t) {
        itemTitle = t;
    }

    void setItemCover(String t) {
        itemCover = t;
    }

    public String getUrl() {
        return url;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public String getItemCover() {
        return itemCover;
    }


}
