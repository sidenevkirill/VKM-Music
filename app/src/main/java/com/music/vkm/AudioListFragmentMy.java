package com.music.vkm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.music.vkm.adapter.MusicAdapter;
import com.music.vkm.item.Music;
import com.music.vkm.item.PlayList;
import com.music.vkm.util.AudioUtil;
import com.music.vkm.util.Cookie;
import com.music.vkm.util.MusicSchedule;
import com.music.vkm.util.MusicService;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.music.vkm.AudioMainActivity.mBoundService;
import static com.music.vkm.AudioMainActivity.searchMenuItem;
import static com.music.vkm.AudioMainActivity.searchView;
import static com.music.vkm.LoginActivity.PERMISSION_REQUEST_CODE;

/**
 * Created by mascot on 13.10.2017.
 */

public class AudioListFragmentMy extends Fragment {
    String tmp, tmp1;
    int size = 0;
    SharedPreferences sPref;
    public static String ownerId;
    private View view;


    public static MusicSchedule schedule = new MusicSchedule();

    public static List<PlayList> playLists = new ArrayList<>();
    private LinearLayoutManager llm;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isLoading = false;
    private String sizeMusic = "0";
    private ProgressDialog progressDialog;
    public static int CountPage = 0;
    private Cookie cookie;
    private String hashadd, hashdel, hashres;
    String TAG = "TestGetMusic";
    Music tmpmusic;
    public boolean Update = false;
    public static MusicAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.audio_list_fragment, container, false);

        view = v;
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Update = true;
                schedule.clear();
                playLists.clear();
                mBoundService.updateCachedMusic();
                getPlayList();

            }
        });


        setRVAdapter();


        return v;
    }


    private void setRVAdapter() {
        final RecyclerView recMusic = view.findViewById(R.id.rv_fragment);

        llm = new LinearLayoutManager(view.getContext());
        recMusic.setLayoutManager(llm);
        recMusic.setHasFixedSize(false);
        recMusic.setNestedScrollingEnabled(true);


        int type = 1;
        if (playLists.size() > 0) {
            type = 4;
        }
        adapter = new MusicAdapter(schedule.getPlaylist(), playLists, type);

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


                final PopupMenu popup = new PopupMenu(view.getContext(), itemView);
                popup.getMenuInflater().inflate(R.menu.audio, popup.getMenu());


                popup.show();
                if (adapter.getList().size() >= position) {
                    if (mBoundService.findSavedMusic(adapter.getList().get(position))) {
                        popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);
                    } else {
                        popup.getMenu().getItem(0).setTitle(R.string.saveToCache);
                    }
                    if (adapter.getList().get(position).getYour().equals("true")) {
                        popup.getMenu().getItem(2).setTitle(view.getResources().getString(R.string.delete));
                    }
                }


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.saveToCache))) {
                            if (hasPermissions()) {
                                mBoundService.cacheMusic(adapter.getList().get(position));
                                popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);
                            } else {
                                Toast.makeText(getContext(), "Для сохранения, нужно разрешение на чтение и запись в память.", Toast.LENGTH_SHORT).show();
                                tmpmusic = adapter.getList().get(position);
                                requestPerms();
                            }

                            return true;
                        }
                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.deleteFromCache))) {

                            if (hasPermissions()) {
                                mBoundService.cacheMusic(adapter.getList().get(position));
                                popup.getMenu().getItem(0).setTitle(R.string.saveToCache);
                            } else {
                                Toast.makeText(getContext(), "Для сохранения, нужно разрешение на чтение и запись в память.", Toast.LENGTH_SHORT).show();
                                tmpmusic = adapter.getList().get(position);
                                requestPerms();
                            }

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

                // recMusic.getAdapter().notifyDataSetChanged();
            }

        });


        adapter.setOnShuffeClickListener(new MusicAdapter.OnShuffeClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {


                progressDialog = new ProgressDialog(getActivity(), R.style.progressbart);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setCancelable(false);
                progressDialog.show();


                Thread myThread = new Thread(new Runnable() {
                    @Override
                    public void run() {


                        while (schedule.size() < Integer.parseInt(sizeMusic)) {
                            if (!isLoading) {
                                isLoading = true;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        getMusicNew();
                                    }
                                });

                            }

                        }

                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                    mBoundService.setMusicList(schedule);
                                    mBoundService.shuffle();

                                    recMusic.getAdapter().notifyDataSetChanged();
                                    progressDialog.cancel();
                                }
                            });


                    }
                });

                myThread.start();

            }
        });

        recMusic.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (llm.findLastVisibleItemPosition() == schedule.size() - 1) {
                    if (!isLoading) {

                        if (schedule.size() < Integer.parseInt(sizeMusic)) {
                            isLoading = true;
                            mSwipeRefreshLayout.setRefreshing(true);

                            getMusicNew();
                        }

                    }
                }
            }


        });

        recMusic.setAdapter(adapter);


    }


    public static AudioListFragmentMy newInstance(String text) {

        AudioListFragmentMy f = new AudioListFragmentMy();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }


    private void getcookies2(Response response) {
        String cookie = response.headers().toString();
        String tmp1, tmp2, tmp3;


        Log.d("AuthCookie", cookie);
        if (response.header("location").contains("_q_hash=")) {
            this.cookie.setCookie(1, loadText("l"));
            this.cookie.setCookie(2, loadText("p"));
            this.cookie.setCookie(3, ("remixq" + AudioUtil.pars("remixq", cookie, ".com") + ".com").replace(" ", ""));
        }


    }

    private void getSid(String url) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        final Request request = new Request.Builder()

                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Origin", "https://m.vk.com/login")
                .addHeader("cookie", loadText("sid") + ";" + cookie.getCookieinLine() + ";" + loadText("l") + ";" + loadText("p"))
                .url(url)


                .build();


        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {


                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();


                Log.d("TestAuth", responseHead);

                getcookies2(response);

                //  cookie.addCookie("remixq_" + AudioUtil.pars("remixq_", responseHead, ";"));
                if (responseHead.contains("remixq_")) {
                    getremixsid(response.header("location"));
                } else {
                    playLists.clear();
                    schedule.clear();
                    newTask(1);

                }

            }
        });

    }

    private void getremixsid(String url) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        final Request request = new Request.Builder()

                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("referer", "https://m.vk.com/")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("cookie", loadText("sid") + ";" + cookie.getCookieinLine())
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {


                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();

                Log.d("GetNewSid", responseHead);

                saveText("sid", "remixsid" + AudioUtil.pars("remixsid", responseHead, ".com") + ".com");

                getPlayList();


            }
        });
    }

    private void getPlayList() {

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setRefreshing(true);


        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        Request request = new Request.Builder()
                .addHeader("accept-language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4")
                .addHeader("upgrade-insecure-requests", "1")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("cookie", loadText("sid") + "; remixmdevice=1366/768/1/!!-!!!!")
                .url("https://m.vk.com/audio?act=audio_playlists")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                newTask(4);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();
                Log.d("TryToGetSidOff", responseHead);
                if (response.header("location") != null) {
                    if (response.header("location").contains("https://login.vk.com/?role=pda&_origin=")) {
                        cookie = new Cookie();
                        Log.d("TryToGetSidOff", "start getting sid");
                        getSid(response.header("location"));
                    }
                } else {

                    ownerId = AudioUtil.pars("<a class=\"al_u", responseData, "\"");
                    saveText("ownerid", ownerId);

                    String respons = responseData;

                    respons = AudioUtil.pars("<div class=\"audioPlaylistsPage__list _si_container\" id=\"au_search_playlist_items\">", respons, "<div class=\"basis__footer mfoot\" id=\"mfoot\"><div class=\"pfoot\">");
                    playLists = new ArrayList<>();

                    if (respons.contains("<div class=\"audioPlaylistsPage")) {

                        tmp = respons;


                        while (tmp.contains("<div class=\"audioPlaylistsPage")) {
                            tmp1 = AudioUtil.pars("<div class=\"audioPlaylistsPage__cell audioPlaylistsPage__cell_link\">", tmp, "<div class=\"wi_actions_wrap\">");
                            Log.i("TestParsPlaylists", "onResponse: " + tmp1);
                            String url, itemTitle, itemCover;

                            url = "https://m.vk.com" + AudioUtil.pars("<a href=\"", tmp1, "\"");
                            itemTitle = AudioUtil.pars("<span class=\"audioPlaylistsPage__title\">", tmp1, "</span>");
                            itemCover = AudioUtil.pars("<span class=\"audioPlaylistsPage__cover\" style=\"", tmp1, "</span>");


                            playLists.add(new PlayList(url, itemTitle, itemCover));


                            tmp = tmp.substring(tmp.indexOf("<div class=\"wi_actions_wrap\">") + "<div class=\"wi_actions_wrap\">".length());

                        }


                    }
                    hashadd = AudioUtil.pars("\"add_hash\":\"", respons, "\"");
                    hashdel = AudioUtil.pars("\"del_hash\":\"", respons, "\"");
                    hashres = AudioUtil.pars("\"res_hash\":\"", respons, "\"");


                    getMusicNew();

                }
            }

        });
    }

    private void getMusicNew() {

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);


        Log.d(TAG, "getMusicNew: " + schedule.size());

        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        RequestBody formBody = new FormBody.Builder()
                //&owner_id=63532428&playlist_id=-1&type=playlist
                .add("access_hash", "")
                .add("claim", "0")
                .add("offset", Integer.toString(schedule.size()))
                .add("owner_id", loadText("ownerid"))
                .add("playlist_id", "-1")
                .add("act", "load_section")
                .add("al", "1")
                .add("type", "playlist")
                .build();

        final Request request = new Request.Builder()

                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "*/*")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("referer", "https://vk.com/audios" + loadText("ownerid"))
                .addHeader("Origin", "https://vk.com")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("cookie", loadText("sid"))
                .post(formBody)
                .url("https://vk.com/al_audio.php")


                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                newTask(4);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();
                Log.d(TAG, "responseHead: " + responseHead);
                Log.d(TAG, "responseData: " + responseData);


                boolean isUpdate = true;
                if (schedule.size() != 0) {
                    isUpdate = false;
                }

                String massive = AudioUtil.pars("<!json>", responseData, "<!>");


                if (!responseData.contains("<!json>")) {
                    //  getMusicNew();
                } else {

                    Object obj = null;

                    try {
                        obj = new JSONParser().parse(massive);
                        Log.d(TAG, "obj create");
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "after obj create");


                    JSONObject jo = (JSONObject) obj;


                    if (jo != null) {

                        String nextOffset = String.valueOf(jo.get("nextOffset"));
                        Log.d(TAG, "onResponse: " + nextOffset);

                        String size = (String) jo.get("infoLine1");

                        sizeMusic = size.replaceAll("\\D+", "");

                        Log.d(TAG, "sizeMusic: " + sizeMusic);

                        String ownerId = String.valueOf(jo.get("ownerId"));
                        saveText("ownerid", ownerId);
                        Log.d("ownerid", "ownerid: " + loadText("ownerid"));


                        JSONArray listMusic = (JSONArray) jo.get("list");
                        Iterator musicItr = listMusic.iterator();
                        String artist = "null",
                                time = "null",
                                url = "null",
                                title = "null",
                                lyrics_id = "null",
                                pic = "null",
                                data_id = "null",
                                save = "null",
                                your = "null",
                                type = "null",
                                add_hash = "null",
                                del_hash = "null",
                                res_hash = "null";


                        while (musicItr.hasNext()) {
                            JSONArray music = (JSONArray) musicItr.next();


                            artist = music.get(4).toString();
                            title = music.get(3).toString();
                            time = music.get(5).toString();
                            url = music.get(2).toString();
                            if (!url.contains("https://"))
                                url = "null";
                            lyrics_id = music.get(9).toString();
                            if (lyrics_id.equals("0"))
                                lyrics_id = "null";
                            pic = music.get(14).toString();
                            if (pic.contains("https://")) {
                                pic = pic.substring(pic.lastIndexOf("https://"));
                            } else {
                                pic = "null";
                            }
                            data_id = music.get(1).toString() + "_" + music.get(0).toString();
                            String savedMusic = "false";
                            String path = "null";

                            if (findSavedMusic(data_id))
                                savedMusic = "true";


                            add_hash = music.get(13).toString();

                            Log.d(TAG, "GetURL music: " + add_hash);

                            res_hash = add_hash;
                            add_hash = add_hash.substring(0, add_hash.indexOf("/"));
                            del_hash = music.get(13).toString();
                            del_hash = del_hash.substring(del_hash.indexOf("/") + 2, del_hash.length() - 2);
                            String tmp = music.get(13).toString();
                            Log.d(TAG, "SUKA" + tmp);
                           /* if (tmp.contains("//")) {
                                tmp = tmp.replaceAll("//", "/");
                            }
                            tmp = tmp.substring(0, tmp.length());
                            tmp = tmp.substring(tmp.lastIndexOf("/") + 1);*/
                            int countThe = StringUtils.countMatches(tmp, "//");
                            String str = tmp;
                            if (countThe > 1) {
                                int position = str.indexOf("//");
                                str = AudioUtil.removeCharAt(str, position);
                            }
                            del_hash = AudioUtil.pars("//", str, "/");

                            res_hash = res_hash.substring(add_hash.length() + 1);
                            res_hash = AudioUtil.pars("/", res_hash, "/");


                            Log.d(TAG, artist + " " + title + " " + time + " " + url + " " + lyrics_id + " " + pic + " " + data_id + " " + add_hash + " " + del_hash + " res_hash:" + res_hash);
                            if (getContext() != null)
                                schedule.add(new Music(artist, time, url, title, lyrics_id, pic, data_id, savedMusic, "true", AudioListFragmentMy.this.getResources().getString(R.string.mymusic), add_hash, del_hash, res_hash));

                        }
                    }

                    if (Update || isUpdate) {
                        newTask(3);
                    } else
                        newTask(2);
                    Update = false;
                    if (getContext() != null)
                        if (getResources().getString(R.string.mymusic).equals(MusicService.getSchedule().getSystemNamePlaylist())) {
                            updateMusic();
                        }
                    isLoading = false;


                }
            }
        });
    }

    public boolean findSavedMusic(String dataid) {
        int i = 0;
        List<String> musicList = getInternalMusic();

        while ((i < musicList.size())) {
            if (musicList.get(i).equals(dataid + ".mp3")) {
                return true;
            }
            i++;
        }

        return false;
    }

    public List<String> getInternalMusic() {

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

    private void newTask(final int type) {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("UpdateRV", "list: " + schedule.size() + "playLists: " + playLists.size());
                    switch (type) {
                        case 1: {
                            mSwipeRefreshLayout.setRefreshing(false);

                            RecyclerView recyclerView = view.findViewById(R.id.rv_playlist);
                            RecyclerView recMusic = view.findViewById(R.id.rv_fragment);

                            if (recMusic != null && recyclerView != null) {
                                Log.d("UpdateRV", "update 1");

                                int curSize = recMusic.getAdapter().getItemCount();
                                recMusic.getAdapter().notifyItemRangeInserted(curSize, schedule.size());


                                recyclerView.getAdapter().notifyDataSetChanged();


                            } else {
                                Log.d("UpdateRV", "update with set rv");
                                setRVAdapter();
                            }

                            TextView info = view.findViewById(R.id.info);
                            if (schedule.size() > 0) {
                                recMusic.setVisibility(View.VISIBLE);
                                info.setVisibility(View.INVISIBLE);
                            } else {
                                recMusic.setVisibility(View.INVISIBLE);
                                info.setVisibility(View.VISIBLE);
                            }


                            break;
                        }
                        case 2: {
                            mSwipeRefreshLayout.setRefreshing(false);
                            RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
                            int curSize = recMusic.getAdapter().getItemCount();
                            recMusic.getAdapter().notifyDataSetChanged();
                            TextView info = view.findViewById(R.id.info);


                            if (schedule.size() > 0) {
                                recMusic.setVisibility(View.VISIBLE);
                                info.setVisibility(View.INVISIBLE);
                            } else {
                                recMusic.setVisibility(View.INVISIBLE);
                                info.setVisibility(View.VISIBLE);
                            }

                            /*
                            RecyclerView recyclerView = getActivity().findViewById(R.id.rv_playlist);
                            recyclerView.getAdapter().notifyDataSetChanged();*/

                            break;
                        }

                        case 3: {
                            mSwipeRefreshLayout.setRefreshing(false);
                            setRVAdapter();
                            RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
                            TextView info = view.findViewById(R.id.info);
                            if (schedule.size() > 0) {
                                recMusic.setVisibility(View.VISIBLE);
                                info.setVisibility(View.INVISIBLE);
                            } else {
                                recMusic.setVisibility(View.INVISIBLE);
                                info.setVisibility(View.VISIBLE);
                            }
                            break;
                        }

                        case 4: {
                            mSwipeRefreshLayout.setRefreshing(false);
                            RecyclerView recyclerView = view.findViewById(R.id.rv_playlist);
                            RecyclerView recMusic = view.findViewById(R.id.rv_fragment);

                            if (recMusic != null && recyclerView != null) {
                                Log.d("UpdateRV", "update 1");

                                int curSize = recMusic.getAdapter().getItemCount();
                                recMusic.getAdapter().notifyItemRangeInserted(curSize, schedule.size());


                                recyclerView.getAdapter().notifyDataSetChanged();


                            } else {
                                Log.d("UpdateRV", "update with set rv");
                                setRVAdapter();
                            }
                            TextView info = view.findViewById(R.id.info);
                            if (schedule.size() > 0) {
                                recMusic.setVisibility(View.VISIBLE);
                                info.setVisibility(View.INVISIBLE);
                            } else {
                                recMusic.setVisibility(View.INVISIBLE);
                                info.setVisibility(View.VISIBLE);
                            }
                            info.setText(view.getResources().getText(R.string.internet_error));
                            break;
                        }
                    }
                }
            });

    }

    @Override
    public void onStart() {
        super.onStart();
        final RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
        if (schedule.size() == 0) {

            recMusic.setVisibility(View.INVISIBLE);
            getPlayList();
        } else {
            recMusic.setVisibility(View.VISIBLE);
        }

    }

    private void saveText(String saved_text, String save) {
        if (getContext() != null) {
            sPref = view.getContext().getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString(saved_text, save);
            ed.commit();
        }
    }

    private String loadText(String saved_text) {
        SharedPreferences sPref = view.getContext().getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }

    private void updateMusic() {
        MusicService.getSchedule().updatePlaylist(schedule);
    }


    private boolean hasPermissions() {
        //string array of permissions,
        String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            if (!(getContext().checkCallingOrSelfPermission(perms) == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:

                for (int res : grantResults) {
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }

                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }

        if (allowed) {
            mBoundService.cacheMusic(tmpmusic);
        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
                builder.setCancelable(false);
                builder.setTitle(getResources().getString(R.string.needperm));
                builder.setMessage("Разрешение необходимо для сохранения музыки");
                builder.setPositiveButton(getResources().getString(R.string.give), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPerms();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.finish), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getContext(), "Аудиозапись не будет сохранена.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        }

    }


}
