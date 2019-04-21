package com.music.vkm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.music.vkm.Audio_main_activity.mBoundService;
import static com.music.vkm.Audio_main_activity.searchMenuItem;
import static com.music.vkm.Audio_main_activity.searchView;

public class Audio_list_fragment_groups extends Fragment {
    private View view;
    private String TAG = "TestGroups";
    public List<Music> list = new ArrayList<>();
    private LinearLayoutManager llm;
    SwipeRefreshLayout mSwipeRefreshLayout;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.audio_list_fragment, container, false);
        view = v;


        RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
        llm = new LinearLayoutManager(getContext());
        recMusic.setLayoutManager(llm);
        recMusic.setHasFixedSize(false);
        recMusic.setNestedScrollingEnabled(true);


        final AdapterMusic adapter = new AdapterMusic(list, new ArrayList<PlayList>(), 3);
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
                    if (mBoundService.findSavedMusic(adapter.getList().get(position))) {
                        popup.getMenu().getItem(0).setChecked(true);
                    } else {
                        popup.getMenu().getItem(0).setChecked(false);
                    }
                    if (adapter.getList().get(position).getYour().equals("true")) {
                        popup.getMenu().getItem(2).setTitle(getActivity().getResources().getString(R.string.delete));
                    }
                }


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getTitle().toString().equals(getActivity().getResources().getString(R.string.save))) {
                            //  mBoundService.cacheMusic(adapter.getList().get(position));
                        }
                        if (item.getTitle().toString().equals(getActivity().getResources().getString(R.string.delete))) {
                            item.setTitle(getActivity().getResources().getString(R.string.add));
                            //  mBoundService.controlMusic(adapter.getList().get(position));
                        }
                        if (item.getTitle().toString().equals(getActivity().getResources().getString(R.string.add))) {
                            item.setTitle(getActivity().getResources().getString(R.string.delete));
                            //    mBoundService.controlMusic(adapter.getList().get(position));
                        }
                        if (item.getTitle().toString().equals(getActivity().getResources().getString(R.string.findartist))) {
                            searchMenuItem.expandActionView();
                            searchView.setQuery(adapter.getList().get(position).getArtist(), true);
                        }
                        if (getActivity() != null)
                            if (item.getTitle().toString().equals(getActivity().getResources().getString(R.string.downloadmusic))) {
                                //   mBoundService.downloadMusic(adapter.getList().get(position));
                            }
                        return true;
                    }
                });


            }
        });

        adapter.setOnShuffeClickListener(new AdapterMusic.OnShuffeClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {


                if (list.size() != 0) {

                    mBoundService.setMusicList(list);
                    MusicService.getSchedule().shuffle();


                } else {
                    Snackbar.make(view, getResources().getString(R.string.notavailable), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });
        // recMusic.setAdapter(adapter);




       /*TextView empty = view.findViewById(R.id.empty);
        if (list.size()>0)
        {
            empty.setVisibility(View.INVISIBLE);
            shuffle_all.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.VISIBLE);
            shuffle_all.setVisibility(View.INVISIBLE);
        }*/


        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mBoundService.updateCachedMusic();
                mSwipeRefreshLayout.setRefreshing(false);

            }
        });


        return v;


    }


    public static Audio_list_fragment_groups newInstance(String text) {

        Audio_list_fragment_groups f = new Audio_list_fragment_groups();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
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