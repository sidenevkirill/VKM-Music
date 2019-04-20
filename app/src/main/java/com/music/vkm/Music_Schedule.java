package com.music.vkm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by halez on 04.07.2018.
 */

public class Music_Schedule {


    List<Music> playlist;
    private boolean stateShuffle = false;
    private String namePlaylist = "default";
    private String systemNamePlaylist = "default";
    int position = -1;

    private List<Music> shuffledMusic = new ArrayList<>();

    Music currentMusic = new Music();


    public void swapMusic(int oldPos, int newPos) {
        Music tmp = playlist.get(oldPos);

        playlist.set(oldPos, playlist.get(newPos));
        playlist.set(newPos, tmp);

        if (currentMusic.equals(tmp)) {
            setMusic(tmp);
        }

        if (!systemNamePlaylist.contains("changedPlaylistSystem")) {
            systemNamePlaylist = systemNamePlaylist + "changedPlaylistSystem";
        }


    }

    public void insertNextMusic(Music nextMusic) {

        playlist.add(position + 1, nextMusic);

        if (!systemNamePlaylist.contains("changedSchedule"))
            systemNamePlaylist = systemNamePlaylist + "changedSchedule";
    }


    Music_Schedule() {
        playlist = new ArrayList<>();
    }

    public String getNamePlaylist() {
        return namePlaylist;
    }

    public String getSystemNamePlaylist() {
        return systemNamePlaylist;
    }

    public void setNamePlaylist(String namePlaylist) {
        this.namePlaylist = namePlaylist;
    }

    public void setSystemNamePlaylist(String systemNamePlaylist) {
        this.systemNamePlaylist = systemNamePlaylist;
    }


    public void add(Music music) {
        playlist.add(music);
    }

    public List<Music> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(List<Music> playlist) {

        if (!systemNamePlaylist.equals(playlist.get(0).getType())) {
            position = 0;
            this.playlist = playlist;
            this.systemNamePlaylist = playlist.get(0).getType();
            this.namePlaylist = playlist.get(0).getType();
            stateShuffle = false;
        }
    }


    public void setMusic(Music music) {

        position = playlist.indexOf(music);

        if (position == -1) {
            position = 0;
        }
        currentMusic = music;

    }

    public void delLastMusic() {
        playlist.remove(playlist.size() - 1);
    }


    public void nextMusic() {
        if (!(position >= playlist.size() - 1)) {
            position++;

        } else {
            position = 0;
        }
        currentMusic = playlist.get(position);
    }

    public void prevMusic() {
        if (position != 0) {
            position--;
        } else {
            position = playlist.size() - 1;
        }
        currentMusic = playlist.get(position);
    }

    public Music getCurrentMusic() {
        return currentMusic;
    }

    public void shuffle() {
        if (stateShuffle) {
            Music music = currentMusic;
            playlist.clear();
            playlist.addAll(shuffledMusic);
            shuffledMusic.clear();
            position = playlist.indexOf(music);
            stateShuffle = false;
            systemNamePlaylist = systemNamePlaylist.replaceAll("shuffledPlaylist", "");
            currentMusic = playlist.get(position);
        } else {

            shuffledMusic.clear();
            shuffledMusic.addAll(playlist);
            Collections.shuffle(playlist);
            position = 0;
            stateShuffle = true;
            systemNamePlaylist += "shuffledPlaylist";
            currentMusic = playlist.get(position);
        }
    }

    public boolean isShuffle() {
        return stateShuffle;
    }

    public void updatePlaylist(List<Music> musicList) {
        if (musicList.size() != 0) {

            playlist = musicList;

            systemNamePlaylist = musicList.get(0).getType();
            namePlaylist = musicList.get(0).getType();
            position = playlist.indexOf(currentMusic);

            if (position == -1)
                position = 0;

        }
    }

    public void updatePlaylist(Music_Schedule newSchedule) {
        if (newSchedule.size() != 0) {

            playlist = newSchedule.getPlaylist();

            systemNamePlaylist = newSchedule.getSystemNamePlaylist();
            namePlaylist = newSchedule.getNamePlaylist();
            position = playlist.indexOf(currentMusic);

            if (position == -1)
                position = 0;

        }
    }

    public int size() {
        return playlist.size();
    }

    public void clear() {
        playlist.clear();
        stateShuffle = false;
        systemNamePlaylist = "default";
        namePlaylist = "default";
        position = -1;
    }

    public int getPosition() {
        return position;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
