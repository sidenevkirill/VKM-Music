package com.music.vkm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.music.vkm.Audio_main_activity.mBoundService;
import static com.music.vkm.Audio_main_activity.searchMenuItem;
import static com.music.vkm.Audio_main_activity.searchView;

/**
 * Created by mascot on 13.10.2017.
 */

public class Audio_list_fragment_save extends Fragment {
    String tmp, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7, tmp8;
    private View view;
    public static Music_Schedule scheduleSaved = new Music_Schedule();
    SwipeRefreshLayout mSwipeRefreshLayout;
    String TAG = "Audio_list_save";
    public static AdapterMusic adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.audio_list_fragment, container, false);

        view = v;
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scheduleSaved.clear();
                mBoundService.updateCachedMusic();
                getMusic();
            }
        });

        setRVAdapter();
        return v;
    }

    void setRVAdapter() {
        final RecyclerView recMusic = view.findViewById(R.id.rv_fragment);

        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());
        recMusic.setLayoutManager(llm);
        recMusic.setHasFixedSize(false);
        recMusic.setNestedScrollingEnabled(true);

        //schedule = getMusicInfo();


        adapter = new AdapterMusic(scheduleSaved, new ArrayList<PlayList>(), 3);

        adapter.setOnItemClickListener(new AdapterMusic.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Log.d(TAG, "onItemClick: ");

                if (adapter.getList().size() > 0) {
                    mBoundService.setMusicList(adapter.getList());
                    mBoundService.setPosition(adapter.getList().get(position));
                }
            }
        });

        adapter.setOnMenuClickListener(new AdapterMusic.OnMenuClickListener() {
            @Override
            public void onItemClick(View itemView, final int position) {


                final PopupMenu popup = new PopupMenu(getActivity(), itemView);
                popup.getMenuInflater().inflate(R.menu.audio, popup.getMenu());

                popup.show();
                if (adapter.getList().size() >= position) {
                    popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);
                    popup.getMenu().getItem(2).setVisible(false);
                    popup.getMenu().getItem(1).setVisible(false);
                }


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.saveToCache))) {
                            mBoundService.cacheMusic(adapter.getList().get(position));
                            popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);
                            return true;
                        }
                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.deleteFromCache))) {
                            mBoundService.cacheMusic(adapter.getList().get(position));
                            popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);

                            return true;
                        }


                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.delete))) {
                            item.setTitle(view.getResources().getString(R.string.add));
                            mBoundService.addMusic(adapter.getList().get(position));
                            return true;
                        }
                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.add))) {
                            item.setTitle(view.getResources().getString(R.string.delete));
                            mBoundService.addMusic(adapter.getList().get(position));
                            return true;
                        }
                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.findartist))) {


                            searchMenuItem.expandActionView();
                            searchView.setQuery(adapter.getList().get(position).getArtist(), true);
                            Audio_main_activity.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            return true;

                        }

                        if (item.getTitle().equals(view.getResources().getString(R.string.playNext))) {

                            MusicService.getSchedule().insertNextMusic(adapter.getList().get(position));
                            return true;
                        }
                        if (view != null)
                            if (item.getTitle().toString().equals(view.getResources().getString(R.string.downloadmusic))) {
                                mBoundService.saveMusicFull(adapter.getList().get(position));
                            }
                        return true;
                    }
                });
            }
        });

        adapter.setOnShuffeClickListener(new AdapterMusic.OnShuffeClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {


                if (scheduleSaved.size() != 0) {

                    mBoundService.setMusicList(scheduleSaved);
                    mBoundService.shuffle();


                } else {
                    Snackbar.make(view, getResources().getString(R.string.notavailable), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });

        recMusic.setAdapter(adapter);
    }

    public static Audio_list_fragment_save newInstance(String text) {

        Audio_list_fragment_save f = new Audio_list_fragment_save();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    @Override
    public void onStart() {
        super.onStart();


        final RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
        if (scheduleSaved.size() == 0) {

            recMusic.setVisibility(View.INVISIBLE);
            getMusic();
        } else {
            recMusic.setVisibility(View.VISIBLE);
        }

    }


    void RefreshRV() {
        Collections.reverse(scheduleSaved.getPlaylist());
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Log.d("UpdateRV", "run: " + scheduleSaved.size());
                    RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
                    setRVAdapter();
                    // recMusic.getAdapter().notifyDataSetChanged();

                    TextView info = view.findViewById(R.id.info);
                    if (scheduleSaved.size() > 0) {
                        recMusic.setVisibility(View.VISIBLE);
                        info.setVisibility(View.INVISIBLE);
                    } else {
                        recMusic.setVisibility(View.INVISIBLE);
                        info.setVisibility(View.VISIBLE);
                    }

                    // recMusic.getAdapter().notifyDataSetChanged();
                    // adapter.notifyDataSetChanged();

                    // Toast.makeText(getContext(), Integer.toString(scheduleSaved.getPlaylist().size()), Toast.LENGTH_SHORT).show();
                    mSwipeRefreshLayout.setRefreshing(false);


                }
            });


    }

    private List<String> getInternalMusic() {

        List<String> filespath = new ArrayList<>();

        String path = loadText("pathCache");

        File file = new File(path);

        File[] files = file.listFiles();

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                filespath.add(files[i].getName());
            }
        }

        return filespath;

    }


    public Music_Schedule getMusicInfo() {
        final List<String> listsavemusic = getInternalMusic();


        Music_Schedule music_schedule = new Music_Schedule();
        String add_hash = "null", del_hash = "null", tmp, tmp1, tmp2, tmp3, tmp4, tmp6, dataid;
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(loadText("pathCache") + "SavedMusicList.info"));
            String str = "";
            // читаем содержимое
            int i = 0;
            while ((str = br.readLine()) != null) {

                while (str.contains("|" + i + "|")) {
                    String music = wstr.pars("|" + i + "|", str, "|" + i + "|");

                    i++;
                    if (music.contains("ADD_HASH")) {
                        add_hash = wstr.pars("|ADD_HASH|", music, "|");
                        del_hash = wstr.pars("|DEL_HASH|", music, "|");
                    }
                    tmp = wstr.pars("|ARTIST|", music, "|");
                    tmp1 = wstr.pars("|TITLE|", music, "|");
                    tmp2 = wstr.pars("|PIC|", music, "|");
                    tmp3 = wstr.pars("|URL|", music, "|");
                    tmp4 = wstr.pars("|YOUR|", music, "|");
                    tmp6 = wstr.pars("|LID|", music, "|");
                    dataid = wstr.pars("|ID|", music, "|");
                    if (listsavemusic.contains(dataid + ".mp3"))
                        music_schedule.add(new Music(tmp, "null", tmp3, tmp1, tmp6, tmp2, dataid, "true", tmp4, getResources().getString(R.string.savedmusic), add_hash, del_hash, "null"));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return music_schedule;
    }

    void getMusic() {

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setRefreshing(true);

        scheduleSaved = getMusicInfo();

        Log.i("TestViewRVSaveMusic", "getMusic: " + scheduleSaved.getPlaylist().size());

        RefreshRV();

        if (getResources().getString(R.string.savedmusic).equals(MusicService.getSchedule().getSystemNamePlaylist())) {
            updateMusic();
        }


    }


    void updateMusic() {
        MusicService.getSchedule().updatePlaylist(scheduleSaved);
    }

    private String loadText(String saved_text) {
        SharedPreferences sPref = getContext().getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }

    void saveText(String saved_text, String save) {
        SharedPreferences sPref = getActivity().getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(saved_text, save);
        ed.commit();
    }
}