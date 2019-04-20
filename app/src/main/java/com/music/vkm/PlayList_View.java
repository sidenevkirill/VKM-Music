package com.music.vkm;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bluzwong.swipeback.SwipeBackActivityHelper;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.music.vkm.Audio_main_activity.mBoundService;
import static com.music.vkm.Audio_main_activity.searchMenuItem;
import static com.music.vkm.Audio_main_activity.searchView;
import static com.music.vkm.Log_In.PERMISSION_REQUEST_CODE;

/**
 * Created by halez on 12.01.2018.
 */

public class PlayList_View extends AppCompatActivity {
    PlayList playList;
    String hashadd, hashdel, hashres;
    SwipeBackActivityHelper helper;
    private SharedPreferences sPref;
    public static Music_Schedule schedule = new Music_Schedule();
    String sizeMusic;
    LinearLayoutManager llm;
    String title = "default";
    boolean isLoading = false;
    private ProgressDialog progressDialog;
    private BottomSheetBehavior mBottomSheetBehavior;
    private int peekHeight;
    private BroadcastReceiver sheet, seek;
    String TAG = "playlist_view";
    Music tmpmusic;

    Handler handler = new Handler();
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            if (MusicService.mediaPlayer != null) {
                setSeek(MusicService.mediaPlayer.getCurrentPosition(), MusicService.secondary);
                handler.postDelayed(this, 1000);
            }
        }
    };


    //TODO: сделать нормальный нижний бар(лучше придумать что-то другое)

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.playlist_view);

        schedule.clear();

        FrameLayout bottomSheetLayout = findViewById(R.id.layout_bottom_sheet);

        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        mBottomSheetBehavior.setHideable(false);

        peekHeight = mBottomSheetBehavior.getPeekHeight();

        mBottomSheetBehavior.setPeekHeight(0);

        progressDialog = new ProgressDialog(PlayList_View.this, R.style.progressbart);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();


      /*  RecyclerView playlist = findViewById(R.id.playlist_rc);
        playlist.v*/

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        Intent intent = getIntent();
        playList = (PlayList) intent.getSerializableExtra("playlist");

        helper = new SwipeBackActivityHelper();
        helper.setEdgeMode(true)
                .setParallaxMode(true)
                .setParallaxRatio(3)
                .setNeedBackgroundShadow(true)
                .init(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(playList.getItemTitle());
        toolbar.setTitleTextColor(Color.parseColor("#000000"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.finish();
            }
        });

        getInfo();


        listenSheet();


        RecyclerView playlist = findViewById(R.id.playlist_rc);
        llm = new LinearLayoutManager(PlayList_View.this);
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


                final PopupMenu popup = new PopupMenu(PlayList_View.this, itemView);
                popup.getMenuInflater().inflate(R.menu.audio, popup.getMenu());


                popup.show();
                if (adapter.getList().size() >= position) {
                    if (mBoundService.findSavedMusic(adapter.getList().get(position))) {
                        popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);
                    } else {
                        popup.getMenu().getItem(0).setTitle(R.string.saveToCache);
                    }
                    if (adapter.getList().get(position).getYour().equals("true")) {
                        popup.getMenu().getItem(2).setTitle(getResources().getString(R.string.delete));
                    }
                }


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getTitle().toString().equals(getResources().getString(R.string.saveToCache))) {
                            if (hasPermissions()) {
                                mBoundService.cacheMusic(adapter.getList().get(position));
                                popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);
                            } else {
                                Toast.makeText(PlayList_View.this, "Для сохранения, нужно разрешение на чтение и запись в память.", Toast.LENGTH_SHORT).show();
                                tmpmusic = adapter.getList().get(position);
                                requestPerms();
                            }

                            return true;
                        }
                        if (item.getTitle().toString().equals(getResources().getString(R.string.deleteFromCache))) {

                            if (hasPermissions()) {
                                mBoundService.cacheMusic(adapter.getList().get(position));
                                popup.getMenu().getItem(0).setTitle(R.string.saveToCache);
                            } else {
                                Toast.makeText(PlayList_View.this, "Для сохранения, нужно разрешение на чтение и запись в память.", Toast.LENGTH_SHORT).show();
                                tmpmusic = adapter.getList().get(position);
                                requestPerms();
                            }

                            return true;
                        }


                        if (item.getTitle().toString().equals(getResources().getString(R.string.delete))) {
                            item.setTitle(getResources().getString(R.string.add));
                            mBoundService.addMusic(adapter.getList().get(position));
                            return true;
                        }
                        if (item.getTitle().toString().equals(getResources().getString(R.string.add))) {
                            item.setTitle(getResources().getString(R.string.delete));
                            mBoundService.addMusic(adapter.getList().get(position));
                            return true;
                        }
                        if (item.getTitle().toString().equals(getResources().getString(R.string.findartist))) {
                            searchMenuItem.expandActionView();
                            searchView.setQuery(adapter.getList().get(position).getArtist(), true);
                            Audio_main_activity.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            return true;

                        }
                        if (item.getTitle().equals(getResources().getString(R.string.playNext))) {

                            MusicService.getSchedule().insertNextMusic(adapter.getList().get(position));
                            return true;
                        }
                        if (item.getTitle().toString().equals(getResources().getString(R.string.downloadmusic))) {
                            mBoundService.saveMusicFull(adapter.getList().get(position));
                        }
                        return true;
                    }
                });

                // recMusic.getAdapter().notifyDataSetChanged();
            }

        });


        playlist.setAdapter(adapter);


        Listners();


    }


    void setSeek(int seek, int sseek) {
        final ProgressBar aplayer_progress = findViewById(R.id.audio_panel_progress);
        aplayer_progress.setProgress(seek);
        aplayer_progress.setSecondaryProgress(sseek);
    }

    void listenSheet() {

        sheet = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.mascotworld.vkaudiomanager.send")) {

                    ImageView start = (ImageView) findViewById(R.id.audio_panel_play);
                    if (MusicService.mediaPlayer != null)
                        if (MusicService.isPlaying) {
                            start.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));

                        } else {
                            start.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_24dp));
                        }


                    ProgressBar audio_panel_progress = findViewById(R.id.audio_panel_progress);
                    audio_panel_progress.setMax(Integer.parseInt(intent.getStringExtra("duration")));


                    TextView audio_panel_title = findViewById(R.id.audio_panel_title);
                    TextView audio_panel_artist = findViewById(R.id.audio_panel_artist);

                    audio_panel_title.setText(intent.getStringExtra("title"));
                    audio_panel_artist.setText(intent.getStringExtra("artist"));

                   /* if (intent.getStringExtra("work").equals("false")) {
                        start.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_24dp));
                    } else {
                        start.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_24dp));
                    }*/

                    ImageView audio_panel_cover = findViewById(R.id.audio_panel_cover);

                    String pic = intent.getStringExtra("pic");
                    if (!pic.equals("null")) {
                        Picasso.with(getApplicationContext())
                                .load(pic)
                                .placeholder(R.drawable.placeholder_albumart_56dp)
                                .error(R.drawable.placeholder_albumart_56dp)
                                .into(audio_panel_cover);
                    } else
                        audio_panel_cover.setImageDrawable(getResources().getDrawable(R.drawable.placeholder_albumart_56dp));

                    serviceRunnable.run();

                    if (MusicService.mediaPlayer != null)
                        openSheet();


                }
            }
        };

        IntentFilter intFilt3 = new IntentFilter("com.mascotworld.vkaudiomanager.send");
        registerReceiver(sheet, intFilt3);

        seek = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.mascotworld.vkaudiomanager.sendseek")) {

                    ProgressBar audio_panel_progress = (ProgressBar) findViewById(R.id.audio_panel_progress);

                    audio_panel_progress.setProgress(Integer.parseInt(intent.getStringExtra("seek")));
                    audio_panel_progress.setSecondaryProgress(Integer.parseInt(intent.getStringExtra("sseek")));

                    if (MusicService.mediaPlayer == null) {
                        closeSheet();
                    }

                }
            }
        };

        IntentFilter intFilt = new IntentFilter("com.mascotworld.vkaudiomanager.sendseek");
        registerReceiver(seek, intFilt);


    }


    void closeSheet() {
        RecyclerView recyclerView = findViewById(R.id.playlist_rc);
        recyclerView.setPadding(0, 0, 0, 0);

        mBottomSheetBehavior.setPeekHeight(0);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


    }

    void openSheet() {
        RecyclerView recyclerView = findViewById(R.id.playlist_rc);
        recyclerView.setPadding(0, 0, 0, peekHeight - 4);

        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setPeekHeight(peekHeight);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }


    void Listners() {


        listenSheet();

        ImageView audio_panel_play = (ImageView) findViewById(R.id.audio_panel_play);

        View.OnClickListener ActionPlayPause = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBoundService.MusicPlayPause();

            }
        };

        audio_panel_play.setOnClickListener(ActionPlayPause);

        ImageView audio_panel_prev = (ImageView) findViewById(R.id.audio_panel_prev);

        View.OnClickListener ActionPrev = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBoundService.MusicPrev();

            }
        };

        audio_panel_prev.setOnClickListener(ActionPrev);

        ImageView audio_panel_next = (ImageView) findViewById(R.id.audio_panel_next);

        View.OnClickListener ActionNext = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBoundService.MusicNext();

            }
        };

        audio_panel_next.setOnClickListener(ActionNext);

        FrameLayout bottomSheetLayout = (FrameLayout) findViewById(R.id.layout_bottom_sheet);

        View.OnClickListener OpenPlayer = new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        };

        bottomSheetLayout.setOnClickListener(OpenPlayer);


    }


    void loadFullMusic() {
        if (!((Activity) PlayList_View.this).isFinishing()) {


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
                        Log.d("ParsPlaylist", "onScrollChange: " + Integer.toString(totalItemCount));

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
                if (tmpf.contains("audio_pl_snippet__artist_link")) {
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
                    if (mustitle.contains("ai_explicit")) {
                        mustitle = mustitle.substring(0, mustitle.indexOf("<div class=\"ai_explicit"));
                        mustitle += " Ⓔ";
                    }
                    String url = wstr.pars("<input type=\"hidden\" value=\"", tmp, "\">");
                    String img;
                    if (tmp.contains("background-image:url(")) {
                        img = wstr.pars("background-image:url(", tmp, ")\"");
                    } else {
                        img = "none";
                    }
                    schedule.add(new Music(artist, "none", url, mustitle, "null", img, dataid, "false", "false", title, hashadd, hashdel, hashres));


                }


                PlayList_View.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        RecyclerView playlist = findViewById(R.id.playlist_rc);

                        if (MusicService.getSchedule().getSystemNamePlaylist().equals(title)) {
                            MusicService.getSchedule().setPlaylist(schedule.getPlaylist());
                        }


                        ImageView cover = findViewById(R.id.cover);
                        Picasso.with(PlayList_View.this)
                                .load(wstr.pars("url('", playList.getItemCover(), "')"))
                                .placeholder(R.drawable.placeholder_playlist)
                                .error(R.drawable.placeholder_playlist)
                                .into(cover);

                        TextView name_playlist = findViewById(R.id.name_playlist);
                        TextView name_sub = findViewById(R.id.name_sub);

                        TextView time_update = findViewById(R.id.time_update);
                        TextView info_title = findViewById(R.id.info_title);

                        TextView name_genres = findViewById(R.id.name_genres);

                        name_playlist.setText(wstr.normalizeString(title));
                        name_sub.setText(wstr.normalizeString(subtitle));
                        time_update.setText(wstr.normalizeString(stats));
                        info_title.setText(wstr.normalizeString(desc));
                        name_genres.setText(wstr.normalizeString(genres));


                        loadFullMusic();


                        playlist.setVisibility(View.VISIBLE);

                        ProgressBar progressBar = findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });

            }
        });
    }

    void saveText(String saved_text, String save) {
        sPref = getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(saved_text, save);
        ed.commit();
    }

    String loadText(String saved_text) {
        sPref = getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_download, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_download) {

            AlertDialog.Builder builder = new AlertDialog.Builder(PlayList_View.this, R.style.AlertDialog);
            builder.setCancelable(true);
            builder.setTitle(getResources().getString(R.string.download));
            builder.setMessage(getResources().getString(R.string.download_path));
            builder.setPositiveButton(getResources().getString(R.string.download_full), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(PlayList_View.this, Download_Activity.class);
                    intent.putExtra("newPlayList", (Serializable) schedule.getPlaylist());
                    intent.putExtra("typedownload", "full");
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.download_cache), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent intent = new Intent(PlayList_View.this, Download_Activity.class);
                    intent.putExtra("newPlayList", (Serializable) schedule.getPlaylist());
                    intent.putExtra("typedownload", "cache");
                    startActivity(intent);
                }
            });
            builder.show();


        }
        return super.onOptionsItemSelected(item);
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
                    .url(playList.getUrl() + "&offset=" + Integer.toString(llm.getItemCount()))
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {

                    PlayList_View.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
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
                            img = "none";
                        }
                        schedule.add(new Music(artist, "none", url, mustitle, "null", img, dataid, "false", "false", title, hashadd, hashdel, hashres));


                    }


                    PlayList_View.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RecyclerView playlist = findViewById(R.id.playlist_rc);
                            playlist.getAdapter().notifyDataSetChanged();
                            isLoading = false;
                        }
                    });

                }
            });
        }
    }

    private boolean hasPermissions() {
        //string array of permissions,
        String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            if (!(checkCallingOrSelfPermission(perms) == PackageManager.PERMISSION_GRANTED)) {
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

                AlertDialog.Builder builder = new AlertDialog.Builder(PlayList_View.this, R.style.AlertDialog);
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
                        Toast.makeText(PlayList_View.this, "Аудиозапись не будет сохранена.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        }

    }
}
