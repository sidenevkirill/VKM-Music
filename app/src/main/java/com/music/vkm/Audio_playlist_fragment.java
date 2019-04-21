package com.music.vkm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.music.vkm.Audio_main_activity.mBoundService;
import static com.music.vkm.Audio_main_activity.searchMenuItem;
import static com.music.vkm.Audio_main_activity.searchView;
import static com.music.vkm.Log_In.PERMISSION_REQUEST_CODE;

public class Audio_playlist_fragment extends Fragment {
    private ProgressDialog progressDialog;
    LinearLayoutManager llm;

    Music_Schedule schedule;
    String hashadd, hashdel, hashres;

    String TAG = "playlist_fragment";
    public static PlayList playList;
    private SharedPreferences sPref;
    String sizeMusic;
    boolean isLoading = false;
    String title = "default";
    Music tmpmusic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.audio_list_fragment, container, false);


        progressDialog = new ProgressDialog(getContext(), R.style.progressbart);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();


        RecyclerView playlist = v.findViewById(R.id.playlist_rc);
        llm = new LinearLayoutManager(getContext());
        playlist.setLayoutManager(llm);
        playlist.setHasFixedSize(false);
        playlist.setNestedScrollingEnabled(false);
        final AdapterMusic adapter = new AdapterMusic(schedule.getPlaylist(), new ArrayList<PlayList>(), 2);
        playlist.setAdapter(adapter);


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

        adapter.setOnShuffeClickListener(new AdapterMusic.OnShuffeClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {


                if (schedule.size() != 0) {

                    mBoundService.setMusicList(schedule);
                    mBoundService.shuffle();


                } else {
                    Snackbar.make(itemView, getResources().getString(R.string.notavailable), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }

            }
        });

        adapter.setOnMenuClickListener(new AdapterMusic.OnMenuClickListener() {
            @Override
            public void onItemClick(View itemView, final int position) {


                final PopupMenu popup = new PopupMenu(getContext(), itemView);
                popup.getMenuInflater().inflate(R.menu.audio, popup.getMenu());


                popup.show();
                if (adapter.getList().size() >= position) {
                    if (mBoundService.findSavedMusic(adapter.getList().get(position))) {
                        popup.getMenu().getItem(0).setChecked(true);
                    } else {
                        popup.getMenu().getItem(0).setChecked(false);
                    }
                    if (adapter.getList().get(position).getYour().equals("true")) {
                        popup.getMenu().getItem(2).setTitle(getResources().getString(R.string.delete));
                    }
                }


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
//todo
                        if (item.getTitle().toString().equals(getResources().getString(R.string.save))) {
                           /* if (hasPermissions()) {
                                mBoundService.cacheMusic(MusicServiceOld.getSchedule().getCurrentMusic());
                            } else {
                                Toast.makeText(getContext(), "Для сохранения, нужно разрешение на чтение и запись в память.", Toast.LENGTH_SHORT).show();
                                tmpmusic = MusicServiceOld.getSchedule().getCurrentMusic();
                                requestPerms();
                            }*/
                            return true;
                        }
                        if (item.getTitle().toString().equals(getResources().getString(R.string.delete))) {
                            item.setTitle(getResources().getString(R.string.add));
                            // mBoundService.controlMusic(MusicServiceOld.getSchedule().getCurrentMusic());
                            return true;
                        }
                        if (item.getTitle().toString().equals(getResources().getString(R.string.add))) {
                            item.setTitle(getResources().getString(R.string.delete));
                            //  mBoundService.controlMusic(MusicServiceOld.getSchedule().getCurrentMusic());
                            return true;
                        }
                        if (item.getTitle().toString().equals(getResources().getString(R.string.findartist))) {


                            searchMenuItem.expandActionView();
                            searchView.setQuery(MusicService.getSchedule().getCurrentMusic().getArtist(), true);

                            return true;

                        }
                        if (item.getTitle().toString().equals(getResources().getString(R.string.downloadmusic))) {
                            // mBoundService.downloadMusic(MusicServiceOld.getSchedule().getCurrentMusic());
                        }
                        return true;
                    }
                });


            }
        });


        playlist.setAdapter(adapter);

        getInfo();


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
//todo
        if (allowed) {
            //   mBoundService.cacheMusic(tmpmusic);
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

    public static Audio_playlist_fragment newInstance(String text) {


        Audio_playlist_fragment f = new Audio_playlist_fragment();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);

        return f;
    }


    void loadFullMusic() {
        if (!getActivity().isFinishing()) {


            Message message = loadFull.obtainMessage(1, "Asd");
            message.sendToTarget();
        }

    }


    Handler loadFull = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {

            if (llm != null)
                while (llm.getItemCount() < Integer.parseInt(sizeMusic) - 20) {


                    int totalItemCount = llm.getItemCount();


                    if (!isLoading) {
                        Log.d("ParsPlaylist", "onScrollChange: " + totalItemCount);

                        isLoading = true;
                        getFullMusic();

                    }
                }

            progressDialog.dismiss();

        }
    };


    void getInfo() {
        OkHttpClient client = new OkHttpClient();


        Request request = new Request.Builder()
                .addHeader("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("upgrade-insecure-requests", "1")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("Cookie", loadText("sid") + "; remixmdevice=1366/768/1/!!-!!!!")
                .url(playList.getUrl())
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                final String responseData = response.body().string();
                schedule.clear();


                String respons = responseData;
                String tmp1 = "";
                if (respons.contains("<div class=\"audioPlaylist__footer\">")) {
                    tmp1 = wstr.pars("<div class=\"audioPlaylist__footer\">", respons, "</div>");

                    if (tmp1.contains("divider")) {
                        sizeMusic = tmp1;

                        sizeMusic = sizeMusic.substring(0, sizeMusic.indexOf("<span class=\"divider\">"));
                        sizeMusic = sizeMusic.replaceAll("\\D+", "");
                        Log.d("sizemusic", "onResponse: " + sizeMusic);

                        tmp1 = tmp1.replaceAll("<span class=\"divider\">", "");
                        tmp1 = tmp1.replaceAll("</span>", "");
                        tmp1 = tmp1.replaceAll("<span class=\"num_delim\">", "");
                    }

                } else {
                    sizeMusic = "0";
                }
                final String stats = tmp1;
                title = wstr.pars("<div class=\"audioPlaylist__title\">", respons, "</div>");
                String tmpf = wstr.pars("<div class=\"audioPlaylist__subtitle\">", respons, "</div>");
                if (tmpf.contains("<a href=\"")) {
                    tmpf = wstr.pars(">", tmpf, "</a>");
                }
                final String subtitle = tmpf;
                String genr = "";
                if (respons.contains("<div class=\"audioPlaylist__genres\"")) {
                    genr = wstr.pars("<div class=\"audioPlaylist__genres\">", respons, "</div>");
                    genr = genr.replaceAll("<span class=\"divider\">", "");
                    genr = genr.replaceAll("</span>", "");
                }
                final String genres = genr;

                if (respons.contains("<div class=\"audioPlaylist__desc\">")) {
                    tmpf = wstr.pars("<div class=\"audioPlaylist__desc\">", respons, "</div>");
                } else {
                    tmpf = "";
                }
                final String desc = tmpf;

                hashadd = wstr.pars("\"add_hash\":\"", respons, "\"");
                hashdel = wstr.pars("\"del_hash\":\"", respons, "\"");
                hashres = wstr.pars("\"res_hash\":\"", respons, "\"");

                while (respons.contains("<div id=\"audio")) {
                    String tmp = "<div id=\"audio" + wstr.pars("<div id=\"audio", respons, "<td class=\"aic_progress_wrap\">");

                    respons = respons.substring(respons.indexOf("<div id=\"audio") + 14);


                    String dataid = wstr.pars("<div id=\"", tmp, "\"");
                    String artist = wstr.pars("<span class=\"ai_artist\">", tmp, "</span>");
                    String mustitle = wstr.pars("<span class=\"ai_title\">", tmp, "</span>");
                    String url = wstr.pars("<input type=\"hidden\" value=\"", tmp, "\">");
                    String img;
                    if (tmp.contains("background-image:url(")) {
                        img = wstr.pars("background-image:url(", tmp, ")\"");
                    } else {
                        img = "null";
                    }
                    schedule.add(new Music(artist, "none", url, mustitle, "null", img, dataid, "false", "false", title, hashadd, hashdel, hashres));


                }

                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            RecyclerView playlist = getActivity().findViewById(R.id.playlist_rc);

                            if (MusicService.getSchedule().getSystemNamePlaylist().equals(title)) {
                                MusicService.getSchedule().setPlaylist(schedule.getPlaylist());
                            }


                            ImageView cover = getActivity().findViewById(R.id.cover);
                            Picasso.with(getActivity())
                                    .load(wstr.pars("url('", playList.getItemCover(), "')"))
                                    .placeholder(R.drawable.placeholder_playlist)
                                    .error(R.drawable.placeholder_playlist)
                                    .into(cover);

                            TextView name_playlist = getActivity().findViewById(R.id.name_playlist);
                            TextView name_sub = getActivity().findViewById(R.id.name_sub);

                            TextView time_update = getActivity().findViewById(R.id.time_update);
                            TextView info_title = getActivity().findViewById(R.id.info_title);

                            TextView name_genres = getActivity().findViewById(R.id.name_genres);

                            name_playlist.setText(wstr.normalizeString(title));
                            name_sub.setText(wstr.normalizeString(subtitle));
                            time_update.setText(wstr.normalizeString(stats));
                            info_title.setText(wstr.normalizeString(desc));
                            name_genres.setText(wstr.normalizeString(genres));

                            View.OnClickListener shuffle = new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {


                                    if (schedule.size() != 0) {

                                        mBoundService.setMusicList(schedule);
                                        MusicService.getSchedule().shuffle();


                                    } else {
                                        Snackbar.make(view, getResources().getString(R.string.notavailable), Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                    }


                                }
                            };

                            CardView shuffle_card = getActivity().findViewById(R.id.shuffle_all);
                            shuffle_card.setOnClickListener(shuffle);

                            loadFullMusic();


                            playlist.setVisibility(View.VISIBLE);

                        }
                    });

            }
        });
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

    private void getFullMusic() {

        if (llm.getItemCount() < Integer.parseInt(sizeMusic)) {

            OkHttpClient client = new OkHttpClient();


            Request request = new Request.Builder()
                    .addHeader("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                    .addHeader("upgrade-insecure-requests", "1")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .addHeader("User-Agent", loadText("userAgent"))
                    .addHeader("Cookie", loadText("sid") + "; remixmdevice=1366/768/1/!!-!!!!")
                    .url(playList.getUrl() + "&offset=" + llm.getItemCount())
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {

                    final String responseData = response.body().string();

                    String respons = responseData;

                    while (respons.contains("<div id=\"audio")) {
                        String tmp = "<div id=\"audio" + wstr.pars("<div id=\"audio", respons, "<td class=\"aic_progress_wrap\">");

                        respons = respons.substring(respons.indexOf("<div id=\"audio") + 14);


                        String dataid = wstr.pars("<div id=\"", tmp, "\"");
                        String artist = wstr.pars("<span class=\"ai_artist\">", tmp, "</span>");
                        String mustitle = wstr.pars("<span class=\"ai_title\">", tmp, "</span>");
                        String url = wstr.pars("<input type=\"hidden\" value=\"", tmp, "\">");
                        String img;
                        if (tmp.contains("background-image:url(")) {
                            img = wstr.pars("background-image:url(", tmp, ")\"");
                        } else {
                            img = "null";
                        }
                        schedule.add(new Music(artist, "none", url, mustitle, "null", img, dataid, "false", "false", title, hashadd, hashdel, hashres));


                    }

                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView playlist = getActivity().findViewById(R.id.playlist_rc);
                                playlist.getAdapter().notifyDataSetChanged();
                                isLoading = false;
                            }
                        });

                }
            });
        }
    }
}
