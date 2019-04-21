package com.music.vkm;

import java.io.Serializable;

/**
 * Created by halez on 06.01.2018.
 */

public class PlayList implements Serializable {

    private String url;
    private String itemTitle;
    private String itemCover;

    PlayList() {
    }

    PlayList(String url, String itemTitle, String itemCover) {
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

    String getUrl() {
        return url;
    }

    String getItemTitle() {
        return itemTitle;
    }

    String getItemCover() {
        return itemCover;
    }


}
