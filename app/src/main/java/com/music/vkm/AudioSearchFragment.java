package com.music.vkm;

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
import androidx.annotation.Nullable;
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
import com.music.vkm.util.MusicSchedule;
import com.music.vkm.util.MusicService;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

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
 * Created by halez on 08.07.2018.
 */

public class AudioSearchFragment extends Fragment {

    public static String query = null;
    MusicSchedule schedule = new MusicSchedule();
    private LinearLayoutManager llm;
    SwipeRefreshLayout mSwipeRefreshLayout;
    boolean isLoading = false;
    private ProgressDialog progressDialog;
    static MusicAdapter adapter;
    String TAG = "search_fragment";
    View view;
    Music tmpmusic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.audio_list_fragment, container, false);
        view = v;

        RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        llm = new LinearLayoutManager(getContext());
        recMusic.setLayoutManager(llm);
        recMusic.setHasFixedSize(false);
        recMusic.setNestedScrollingEnabled(true);


        adapter = new MusicAdapter(schedule.getPlaylist(), new ArrayList<PlayList>(), 3);
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


                progressDialog = new ProgressDialog(view.getContext(), R.style.progressbart);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage(getString(R.string.loading));
                progressDialog.setCancelable(false);
                progressDialog.show();


                Thread myThread = new Thread(new Runnable() {
                    @Override
                    public void run() {


                        while (schedule.size() < 150) {
                            if (!isLoading) {
                                isLoading = true;
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            getQueryMusic();
                                        }
                                    });
                                } else {
                                    break;
                                }
                            }

                        }

                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                    mBoundService.setMusicList(schedule);
                                    mBoundService.shuffle();


                                    progressDialog.cancel();
                                }
                            });


                    }
                });

                myThread.start();

            }
        });
        recMusic.setAdapter(adapter);

        recMusic.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (llm.findLastVisibleItemPosition() == schedule.size() - 1) {
                    if (!isLoading) {
                        if (schedule.size() < 150) {
                            isLoading = true;
                            mSwipeRefreshLayout.setRefreshing(true);
                            getQueryMusic();
                        }

                    }
                }

            }


        });

        getQueryMusic();


        return v;
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
        String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
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

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.AlertDialog);
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

    private void getQueryMusic() {
        mSwipeRefreshLayout.setRefreshing(true);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();


        RequestBody formBody = new FormBody.Builder()
                .add("access_hash", "")
                .add("al", "1")
                .add("act", "load_section")
                .add("claim", "0")
                .add("offset", Integer.toString(schedule.size()))
                .add("owner_id", loadText("ownerid"))
                .add("search_history", "0")
                .add("search_q", query)
                .add("type", "search")
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
                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);
                            RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
                            TextView info = view.findViewById(R.id.info);
                            if (schedule.size() > 0) {
                                recMusic.setVisibility(View.VISIBLE);
                                info.setVisibility(View.INVISIBLE);
                            } else {
                                recMusic.setVisibility(View.INVISIBLE);
                                info.setVisibility(View.VISIBLE);
                            }
                            info.setText(view.getResources().getText(R.string.internet_error));
                        }
                    });

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();


                String massive = AudioUtil.pars("<!json>", responseData, "<!>");
                Log.d(TAG, "onResponse: " + massive);


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
                            del_hash = AudioUtil.pars("///", str, "/");

                            res_hash = res_hash.substring(add_hash.length() + 1);
                            res_hash = AudioUtil.pars("/", res_hash, "/");


                            Log.d(TAG, artist + " " + title + " " + time + " " + url + " " + lyrics_id + " " + pic + " " + data_id + " " + add_hash + " " + del_hash + " res_hash:" + res_hash);
                            if (getActivity() != null)
                                schedule.add(new Music(artist, time, url, title, lyrics_id, pic, data_id, savedMusic, "false", getContext().getResources().getString(R.string.recommendations), add_hash, del_hash, res_hash));

                        }
                    }
                }
                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
                            recMusic.getAdapter().notifyDataSetChanged();

                            TextView info = view.findViewById(R.id.info);
                            if (schedule.size() > 0) {
                                recMusic.setVisibility(View.VISIBLE);
                                info.setVisibility(View.INVISIBLE);
                            } else {
                                recMusic.setVisibility(View.INVISIBLE);
                                info.setVisibility(View.VISIBLE);
                            }

                            mSwipeRefreshLayout.setRefreshing(false);

                            isLoading = false;

                        }
                    });


            }
        });


    }

    public static AudioSearchFragment newInstance(String text) {

        query = text;
        AudioSearchFragment f = new AudioSearchFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);

        return f;
    }


    @Override
    public void onStart() {
        super.onStart();

        if (schedule.size() == 0) {
            final RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
            recMusic.setVisibility(View.INVISIBLE);
            if (query != null) {
                getQueryMusic();
            }

        }
    }
}
