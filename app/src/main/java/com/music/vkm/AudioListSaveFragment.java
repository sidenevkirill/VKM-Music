package com.music.vkm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.music.vkm.adapter.MusicAdapter;
import com.music.vkm.item.Music;
import com.music.vkm.item.PlayList;
import com.music.vkm.util.AudioUtil;
import com.music.vkm.util.MusicSchedule;
import com.music.vkm.util.MusicService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.music.vkm.AudioMainActivity.mBoundService;
import static com.music.vkm.AudioMainActivity.searchMenuItem;
import static com.music.vkm.AudioMainActivity.searchView;

/**
 * Created by mascot on 13.10.2017.
 */

public class AudioListSaveFragment extends Fragment {
    String tmp;
    private View view;
    public static MusicSchedule scheduleSaved = new MusicSchedule();
    SwipeRefreshLayout mSwipeRefreshLayout;
    String TAG = "Audio_list_save";
    public static MusicAdapter adapter;

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


        adapter = new MusicAdapter(scheduleSaved, new ArrayList<PlayList>(), 3);

        adapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Log.d(TAG, "onItemClick: ");

                if (adapter.getList().size() > 0) {
                    mBoundService.setMusicList(adapter.getList());
                    mBoundService.setPosition(adapter.getList().get(position));
                }
            }
        });

        adapter.setOnMenuClickListener(new MusicAdapter.OnMenuClickListener() {
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
                            AudioMainActivity.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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

        adapter.setOnShuffeClickListener(new MusicAdapter.OnShuffeClickListener() {
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

    public static AudioListSaveFragment newInstance(String text) {

        AudioListSaveFragment f = new AudioListSaveFragment();
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


    public MusicSchedule getMusicInfo() {
        final List<String> listsavemusic = getInternalMusic();


        MusicSchedule music_schedule = new MusicSchedule();
        String add_hash = "null", del_hash = "null", tmp, tmp1, tmp2, tmp3, tmp4, tmp6, dataid;
        try {
            // открываем поток для чтения
            BufferedReader br = new BufferedReader(new FileReader(loadText("pathCache") + "SavedMusicList.info"));
            String str = "";
            // читаем содержимое
            int i = 0;
            while ((str = br.readLine()) != null) {

                while (str.contains("|" + i + "|")) {
                    String music = AudioUtil.pars("|" + i + "|", str, "|" + i + "|");

                    i++;
                    if (music.contains("ADD_HASH")) {
                        add_hash = AudioUtil.pars("|ADD_HASH|", music, "|");
                        del_hash = AudioUtil.pars("|DEL_HASH|", music, "|");
                    }
                    tmp = AudioUtil.pars("|ARTIST|", music, "|");
                    tmp1 = AudioUtil.pars("|TITLE|", music, "|");
                    tmp2 = AudioUtil.pars("|PIC|", music, "|");
                    tmp3 = AudioUtil.pars("|URL|", music, "|");
                    tmp4 = AudioUtil.pars("|YOUR|", music, "|");
                    tmp6 = AudioUtil.pars("|LID|", music, "|");
                    dataid = AudioUtil.pars("|ID|", music, "|");
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
        SharedPreferences sPref = getContext().getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }

    void saveText(String saved_text, String save) {
        SharedPreferences sPref = getActivity().getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(saved_text, save);
        ed.commit();
    }
}