package com.music.vkm.util;

import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import androidx.media.session.MediaButtonReceiver;

import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;
import com.music.vkm.MainActivity;
import com.music.vkm.NotificationGenerator;
import com.music.vkm.R;
import com.music.vkm.item.Music;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;

public class MusicService extends Service {

    public static MediaPlayer mediaPlayer;

    private final IBinder mBinder = new MusicService.MyBinder();

    private static AudioManager am;
    public static String ownerId = "default";
    private MediaSessionCompat mMediaSessionCompat;
    public static String SPreferences = "SettingsGeneralActivity";
    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    SharedPreferences sPref;
    String TAG = "TestGetMusic";
    BroadcastReceiver brPlay, brNext, brClose, brPrev, brStopEx, brMediaButtons;
    MediaMetadataCompat metadata;
    public static boolean ServiceBinded = false;
    private boolean ReceiverState = false;

    public static MusicSchedule cachedMusic = new MusicSchedule();

    public static int secondary = 0;

    private static MusicSchedule schedule = new MusicSchedule();
    private Music tmpmusic;

    public static boolean loop = false;


    public static boolean isPlaying = false;
    public static boolean isBuffering = true;

    public static boolean isNotificationListenersCreated = false;

    List<Music> downloadListMusic;
    int typeDownload;

    Music deleteMusic;
    Music saveMusic;


    final int UPDATE_CURRENT = 0;
    final int UPDATE_SAVE_FULL = 1;
    final int UPDATE_SAVE_CACHE = 2;
    final int ADD_GOOD = 3;
    final int ADD_BAD = 4;
    final int DELETE_GOOD = 5;
    final int DELETE_BAD = 6;


    //overrides
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        releaseMP();
        isPlaying = false;
        if (isNotificationListenersCreated) {
            unregisterReceiver(brNext);
            unregisterReceiver(brPlay);
            unregisterReceiver(brPrev);
            unregisterReceiver(brStopEx);
            unregisterReceiver(brMediaButtons);
        }
        ReceiverState = false;
        isNotificationListenersCreated = false;

        am.abandonAudioFocus(mOnAudioFocusChangeListener);
        mMediaSessionCompat.setActive(false);
        stopForeground(true);
        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();


    }

    @Override
    public void onCreate() {
        super.onCreate();

        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "Tag", mediaButtonReceiver, null);
        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSessionCompat.setCallback(mediaSessionCallback);
        Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
        mMediaSessionCompat.setSessionActivity(
                PendingIntent.getActivity(getApplicationContext(), 0, activityIntent, 0));

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);
    }

    //binders
    @Override
    public IBinder onBind(Intent intent) {
        ServiceBinded = true;
        MusicService.ServiceBinded = true;
        Log.d("Binder", "onBind: binded");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("Binder", "onUnbind: unbinded");
        ServiceBinded = false;
        MusicService.ServiceBinded = false;
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d("Binder", "onRebind: rebinded");
        ServiceBinded = true;
        MusicService.ServiceBinded = true;
        super.onRebind(intent);
    }

    public class MyBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }


    //player
    private void releaseMP() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
                isPlaying = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void PrepareMP() {


        Log.d("PlaySavedMusic", "start");

        releaseMP();


        Log.i("PlaySavedMusic", "current " + schedule.getCurrentMusic().getName_cache());
        Log.i(TAG, "reloadNames " + schedule.getCurrentMusic().getUrl());
        if (findSavedMusic(getSchedule().getCurrentMusic())) {

            Uri destinationUri = Uri.parse(loadText("pathCache") + getSchedule().getCurrentMusic().getData_id() + ".mp3");

            Log.i("PlaySavedMusic", "true");
            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(getApplicationContext(), destinationUri);
                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

            try {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setDataSource(schedule.getCurrentMusic().getUrl());
                mediaPlayer.prepareAsync();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        if (loop) {
            MusicLoop();
        }

        isBuffering = true;

        MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                if (what != -38) {
                    Toast.makeText(getApplicationContext(), "Error " + what + " " + extra, Toast.LENGTH_LONG).show();
                    // MusicNext();
                }
                return true;
            }
        };

        mediaPlayer.setOnErrorListener(errorListener);

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {


                isBuffering = false;

                MusicPlayPause(true);


                secondary = 0;

                mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                    @Override
                    public void onBufferingUpdate(MediaPlayer mp, int percent) {


                        if (!findSavedMusic(getSchedule().getCurrentMusic())) {
                            secondary = (mediaPlayer.getDuration() / 100) * percent;
                        } else {
                            secondary = 0;
                        }

                        if (percent == 100) {
                            if (loadText("smAll").equals("true")) {
                                saveMusicNormal(schedule.getCurrentMusic(), 1);
                            }
                        }
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        MusicNext();
                    }
                });

            }
        });


    }


    //decodeurls
    private void decodeUrl() {
        releaseMP();
        SendInfo();
        updateNotification();
        if (schedule.getPlaylist().size() != 0) {


            Log.d("Decode_start", "start checking");


            if (getInternalMusic().contains(schedule.getCurrentMusic().getData_id() + ".mp3")) {
                Log.d("Decode_start", "not needed");

                PrepareMP();
            } else {
                if (schedule.getCurrentMusic().getUrl().equals("null")) {
                    getMusicUrl(schedule.getCurrentMusic(), 1);
                } else if (schedule.getCurrentMusic().getUrl().equals("blocked")) {
                    Toast.makeText(getApplicationContext(), "Music blocked", Toast.LENGTH_SHORT).show();
                    MusicNext();
                } else if (schedule.getCurrentMusic().getUrl().contains("audio_api_unavailable")) {
                    Log.d("Decode_start", "start decoding");
                    if (schedule.getCurrentMusic().getUrl().contains("\\")) {
                        schedule.getCurrentMusic().setUrl(schedule.getCurrentMusic().getUrl().replaceAll("\\\\", ""));
                    }

                    String code = "var r = \"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN0PQRSTUVWXYZO123456789+/=\"\n" +
                            "          , l = {\n" +
                            "            v: function(t) {\n" +
                            "                return t.split(\"\").reverse().join(\"\")\n" +
                            "            },\n" +
                            "            r: function(t, e) {\n" +
                            "                t = t.split(\"\");\n" +
                            "                for (var i, o = r + r, a = t.length; a--; )\n" +
                            "                    i = o.indexOf(t[a]),\n" +
                            "                    ~i && (t[a] = o.substr(i - e, 1));\n" +
                            "                return t.join(\"\")\n" +
                            "            },\n" +
                            "            s: function(t, e) {\n" +
                            "                var i = t.length;\n" +
                            "                if (i) {\n" +
                            "                    var o = s(t, e)\n" +
                            "                      , a = 0;\n" +
                            "                    for (t = t.split(\"\"); ++a < i; )\n" +
                            "                        t[a] = t.splice(o[i - 1 - a], 1, t[a])[0];\n" +
                            "                    t = t.join(\"\")\n" +
                            "                }\n" +
                            "                return t\n" +
                            "            },\n" +
                            "            i: function(t, e) {\n" +
                            "                return l.s(t, e ^ " + loadText("ownerid") + ")\n" +
                            "            },\n" +
                            "            x: function(t, e) {\n" +
                            "                var i = [];\n" +
                            "                return e = e.charCodeAt(0),\n" +
                            "                each(t.split(\"\"), function(t, o) {\n" +
                            "                    i.push(String.fromCharCode(o.charCodeAt(0) ^ e))\n" +
                            "                }),\n" +
                            "                i.join(\"\")\n" +
                            "            }\n" +
                            "        }\n" +
                            "\t\tfunction i() {\n" +
                            "            return \"\"\n" +
                            "        }\n" +
                            "\t\tfunction o(t) {\n" +
                            "            if (!i() && ~t.indexOf(\"audio_api_unavailable\")) {\n" +
                            "                var e = t.split(\"?extra=\")[1].split(\"#\")\n" +
                            "                  , o = \"\" === e[1] ? \"\" : a(e[1]);\n" +
                            "                if (e = a(e[0]),\n" +
                            "                \"string\" != typeof o || !e)\n" +
                            "                    return t;\n" +
                            "                o = o ? o.split(String.fromCharCode(9)) : [];\n" +
                            "                for (var s, r, n = o.length; n--; ) {\n" +
                            "                    if (r = o[n].split(String.fromCharCode(11)),\n" +
                            "                    s = r.splice(0, 1, e)[0],\n" +
                            "                    !l[s])\n" +
                            "                        return t;\n" +
                            "                    e = l[s].apply(null, r)\n" +
                            "                }\n" +
                            "                if (e && \"http\" === e.substr(0, 4))\n" +
                            "                    return e\n" +
                            "            }\n" +
                            "            return t\n" +
                            "        }\n" +
                            "        function a(t) {\n" +
                            "            if (!t || t.length % 4 == 1)\n" +
                            "                return !1;\n" +
                            "            for (var e, i, o = 0, a = 0, s = \"\"; i = t.charAt(a++); )\n" +
                            "                i = r.indexOf(i),\n" +
                            "                ~i && (e = o % 4 ? 64 * e + i : i,\n" +
                            "                o++ % 4) && (s += String.fromCharCode(255 & e >> (-2 * o & 6)));\n" +
                            "            return s\n" +
                            "        }\n" +
                            "        function s(t, e) {\n" +
                            "            var i = t.length\n" +
                            "              , o = [];\n" +
                            "            if (i) {\n" +
                            "                var a = i;\n" +
                            "                for (e = Math.abs(e); a--; )\n" +
                            "                    e = (i * (a + 1) ^ e + a) % i,\n" +
                            "                    o[a] = e\n" +
                            "            }\n" +
                            "            return o\n" +
                            "        }";


                    JsEvaluator jsEvaluator = new JsEvaluator(getApplicationContext());
                    jsEvaluator.callFunction(code,
                            new JsCallback() {

                                @Override
                                public void onResult(String result) {

                                    if (result.contains("audio_api_unavailable")) {
                                        Log.d("Decode_error", "result: " + result + " original: " + schedule.getCurrentMusic().getUrl());
                                    }


                                    schedule.getCurrentMusic().setUrl(result);

                                    Log.d("Decode_good", "result: " + result + " original: " + schedule.getCurrentMusic().getUrl());
                                    //  Notifications(schedule.getCurrentMusic().getPic(), schedule.getCurrentMusic().getArtist(), schedule.getCurrentMusic().getTitle(), "play", true);
                                    PrepareMP();


                                }


                                @Override
                                public void onError(String errorMessage) {
                                    Log.d("Decode_error", errorMessage);
                                }
                            }, "o", schedule.getCurrentMusic().getUrl());
                } else {
                    //  Notifications(schedule.getCurrentMusic().getPic(), schedule.getCurrentMusic().getArtist(), schedule.getCurrentMusic().getTitle(), "play", true);
                    PrepareMP();
                }
            }
        }
    }

    private void decodeUrlForSave(final Music music, final int type) {
        if (music.getUrl().equals("null")) {
            if (type == 1) {
                getMusicUrl(music, 2);
            } else {
                getMusicUrl(music, 3);
            }
        } else if (music.getUrl().contains("audio_api_unavailable")) {

            if (music.getUrl().contains("\\")) {
                music.setUrl(music.getUrl().replaceAll("\\\\", ""));
            }

            String code = "var r = \"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMN0PQRSTUVWXYZO123456789+/=\"\n" +
                    "          , l = {\n" +
                    "            v: function(t) {\n" +
                    "                return t.split(\"\").reverse().join(\"\")\n" +
                    "            },\n" +
                    "            r: function(t, e) {\n" +
                    "                t = t.split(\"\");\n" +
                    "                for (var i, o = r + r, a = t.length; a--; )\n" +
                    "                    i = o.indexOf(t[a]),\n" +
                    "                    ~i && (t[a] = o.substr(i - e, 1));\n" +
                    "                return t.join(\"\")\n" +
                    "            },\n" +
                    "            s: function(t, e) {\n" +
                    "                var i = t.length;\n" +
                    "                if (i) {\n" +
                    "                    var o = s(t, e)\n" +
                    "                      , a = 0;\n" +
                    "                    for (t = t.split(\"\"); ++a < i; )\n" +
                    "                        t[a] = t.splice(o[i - 1 - a], 1, t[a])[0];\n" +
                    "                    t = t.join(\"\")\n" +
                    "                }\n" +
                    "                return t\n" +
                    "            },\n" +
                    "            i: function(t, e) {\n" +
                    "                return l.s(t, e ^ " + loadText("ownerid") + ")\n" +
                    "            },\n" +
                    "            x: function(t, e) {\n" +
                    "                var i = [];\n" +
                    "                return e = e.charCodeAt(0),\n" +
                    "                each(t.split(\"\"), function(t, o) {\n" +
                    "                    i.push(String.fromCharCode(o.charCodeAt(0) ^ e))\n" +
                    "                }),\n" +
                    "                i.join(\"\")\n" +
                    "            }\n" +
                    "        }\n" +
                    "\t\tfunction i() {\n" +
                    "            return \"\"\n" +
                    "        }\n" +
                    "\t\tfunction o(t) {\n" +
                    "            if (!i() && ~t.indexOf(\"audio_api_unavailable\")) {\n" +
                    "                var e = t.split(\"?extra=\")[1].split(\"#\")\n" +
                    "                  , o = \"\" === e[1] ? \"\" : a(e[1]);\n" +
                    "                if (e = a(e[0]),\n" +
                    "                \"string\" != typeof o || !e)\n" +
                    "                    return t;\n" +
                    "                o = o ? o.split(String.fromCharCode(9)) : [];\n" +
                    "                for (var s, r, n = o.length; n--; ) {\n" +
                    "                    if (r = o[n].split(String.fromCharCode(11)),\n" +
                    "                    s = r.splice(0, 1, e)[0],\n" +
                    "                    !l[s])\n" +
                    "                        return t;\n" +
                    "                    e = l[s].apply(null, r)\n" +
                    "                }\n" +
                    "                if (e && \"http\" === e.substr(0, 4))\n" +
                    "                    return e\n" +
                    "            }\n" +
                    "            return t\n" +
                    "        }\n" +
                    "        function a(t) {\n" +
                    "            if (!t || t.length % 4 == 1)\n" +
                    "                return !1;\n" +
                    "            for (var e, i, o = 0, a = 0, s = \"\"; i = t.charAt(a++); )\n" +
                    "                i = r.indexOf(i),\n" +
                    "                ~i && (e = o % 4 ? 64 * e + i : i,\n" +
                    "                o++ % 4) && (s += String.fromCharCode(255 & e >> (-2 * o & 6)));\n" +
                    "            return s\n" +
                    "        }\n" +
                    "        function s(t, e) {\n" +
                    "            var i = t.length\n" +
                    "              , o = [];\n" +
                    "            if (i) {\n" +
                    "                var a = i;\n" +
                    "                for (e = Math.abs(e); a--; )\n" +
                    "                    e = (i * (a + 1) ^ e + a) % i,\n" +
                    "                    o[a] = e\n" +
                    "            }\n" +
                    "            return o\n" +
                    "        }";


            JsEvaluator jsEvaluator = new JsEvaluator(this);
            jsEvaluator.callFunction(code,
                    new JsCallback() {

                        @Override
                        public void onResult(String result) {
                            music.setUrl(result);
                            saveMusicNormal(music, type);
                        }


                        @Override
                        public void onError(String errorMessage) {
                            Log.d("Decode_error", errorMessage);
                        }
                    }, "o", music.getUrl());

        } else {
            saveMusicNormal(music, type);
        }
    }


    //control save music

    public void cacheMusic(Music music) {
        // Toast.makeText(getApplicationContext(), music.getData_id(), Toast.LENGTH_SHORT).show();
        if (findSavedMusic(music)) {
            delInternalMusic(music.getData_id() + ".mp3");
            deleteMusic(music);
        } else {
            saveMusic = music;
            saveThread.sendEmptyMessage(ADD_GOOD);
        }
    }


    Handler saveThread = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            decodeUrlForSave(saveMusic, 1);

        }
    };


    public void saveMusicFull(Music music) {
        decodeUrlForSave(music, 2);
    }

    public void addMusic(final Music music) {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        String audio_id = music.getData_id().substring(music.getData_id().indexOf("_") + 1);
        String audio_owner_id = music.getData_id().substring(0, music.getData_id().indexOf("_"));
        Log.d("audio_owner_id", "audio_id: " + audio_id + "  audio_owner_id: " + audio_owner_id);


        RequestBody formBody;

        if (music.getYour().equals("false")) {
            formBody = new FormBody.Builder()
                    .add("al", "1")
                    .add("act", "add")
                    .add("audio_id", audio_id)
                    .add("audio_owner_id", audio_owner_id)
                    .add("from", "search:external")
                    .add("group_id", "0")
                    .add("hash", music.getAdd_hash())
                    .build();
        } else {
            Log.d("delete", "audio_id: " + audio_id + "  audio_owner_id: " + audio_owner_id);
            formBody = new FormBody.Builder()
                    .add("al", "1")
                    .add("act", "delete_audio")
                    .add("aid", audio_id)
                    .add("oid", audio_owner_id)
                    .add("hash", music.getDel_hash())
                    .add("restore", "1")
                    .build();
        }


        final Request request = new Request.Builder()

                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.101 Safari/537.36")
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
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();


                String massive = AudioUtil.pars("<!json>", responseData, "<!>");
                Log.d("Test_add_music", "onResponse: " + responseData);

                if (music.getYour().equals("false")) {
                    if (responseData.contains("<!>0<!><!json>[")) {
                        updateUrl.sendEmptyMessage(ADD_GOOD);
                    } else {
                        updateUrl.sendEmptyMessage(ADD_BAD);
                    }
                } else {
                    if (responseData.contains("<!>0<!><!><!null>") || responseData.contains("<!>0<!><!json>{\"object_id\":")) {
                        updateUrl.sendEmptyMessage(DELETE_GOOD);
                    } else {
                        updateUrl.sendEmptyMessage(DELETE_BAD);
                    }

                }


            }
        });


    }

    public void downloadListMusic(List<Music> downloadList, int type) {


        this.downloadListMusic = downloadList;
        typeDownload = type;
        downloadThread.sendEmptyMessage(ADD_GOOD);


    }

    Handler downloadThread = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            Log.i("TestDownloadListMusic", "handleMessage: ");

            // List<String> listsavedmusic = getInternalMusic();
            for (int i = 0; i < downloadListMusic.size(); i++) {
                if (!findSavedMusic(downloadListMusic.get(i)))
                    decodeUrlForSave(downloadListMusic.get(i), typeDownload);
            }


        }
    };


    //downloadmusic
    private void saveMusicNormal(final Music music, final int type) {
        if (music.getUrl().contains("index.m3u8")) {
            Toast.makeText(getApplicationContext(), "Песня не может быть сохранена в кэш в этой версии.", Toast.LENGTH_SHORT).show();
        } else {
            if (music.getUrl().equals("blocked")) {
                Toast.makeText(getApplicationContext(), "Песня не может быть сохранена в кэш.", Toast.LENGTH_SHORT).show();
            } else {
                File filesCache = new File(loadText("pathCache"));
                if (!filesCache.exists()) {
                    Log.d("TestSave", "Directory not exist");
                    filesCache.mkdirs();
                } else {
                    Log.d("TestSave", "Directory exist");
                }

                File filesFull = new File(loadText("pathFull"));
                if (!filesFull.exists()) {
                    Log.d("TestSave", "Directory not exist");
                    filesFull.mkdirs();
                } else {
                    Log.d("TestSave", "Directory exist");
                }
                if (findSavedMusic(music) && type == 1) {

                } else {

                    Log.i(TAG, "reloadNames " + music.getUrl());
                    DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri music_uri = Uri.parse(music.getUrl());
                    Log.d(TAG, "saveMusicNormal: " + music.getUrl());
                    DownloadManager.Request request = new DownloadManager.Request(music_uri);
                    request.setTitle(music.getArtist() + " - " + music.getTitle());
                    request.setDescription(getApplicationContext().getResources().getString(R.string.download));
                    if (type == 2) {
                        Uri destinationUri = Uri.parse("file://" + loadText("pathFull") + music.getArtist() + " - " + music.getTitle() + ".mp3");
                        Log.d("TestSavingMusic", "saveMusicFull: " + destinationUri.toString());
                        request.setDestinationUri(destinationUri);

                    } else if (type == 1) {
                        Uri destinationUri = Uri.parse("file://" + loadText("pathCache") + music.getData_id() + ".mp3");
                        Log.d("TestSavingMusic", "saveMusicCache: " + destinationUri.toString());
                        request.setDestinationUri(destinationUri);

                    }

                    if (type == 1) {
                        saveMusicInfo(music);

                    }

                    final Long reference = downloadManager.enqueue(request);

                    BroadcastReceiver download = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {

                            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                            if (reference == referenceId) {

                                if (type == 1) {
                                    Toast.makeText(getApplicationContext(), "Песня " + music.getArtist() + " - " + music.getTitle() + " сохранена в кэш.", Toast.LENGTH_SHORT).show();
                                } else if (type == 2) {
                                    Toast.makeText(getApplicationContext(), "Песня " + music.getArtist() + " - " + music.getTitle() + " загружена.", Toast.LENGTH_SHORT).show();

                                }


                            }


                        }
                    };

                    IntentFilter intFilt = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                    getApplicationContext().registerReceiver(download, intFilt);
                }
            }
        }
    }


    //controlplayer
    public void MusicPlayPause() {
        Log.d("TestingErrorFocus", "MusicPlayPause: without bool");

        if (mediaPlayer != null) {


            if (mediaPlayer.isPlaying()) {
                isPlaying = false;
                setFocus();
                mediaPlayer.pause();


            } else {
                isPlaying = true;
                setFocus();
                mediaPlayer.start();


            }

        } else {
            decodeUrl();
        }
        SendInfo();

    }

    public void MusicPlayPause(boolean work) {

        isPlaying = work;
        setFocus();
        if (mediaPlayer != null) {
            if (work) {
                Log.d("TestingErrorFocus", "MusicPlayPause: start");
                mediaPlayer.start();

            } else {
                Log.d("TestingErrorFocus", "MusicPlayPause: pause");
                mediaPlayer.pause();

            }
        }
        SendInfo();
    }

    public void MusicDestroy() {
        onDestroy();
    }

    public void MusicNext() {
        Log.d("TestingErrorFocus", "MusicNext: ");
        if (mediaPlayer == null) return;
        schedule.nextMusic();
        decodeUrl();
        SendInfo();
    }

    public void MusicPrev() {
        Log.d("TestingErrorFocus", "MusicPrev: ");
        if (mediaPlayer == null) return;
        if (mediaPlayer.getCurrentPosition() <= 5000) {
            schedule.prevMusic();
            decodeUrl();
        } else {
            MusicSeek(0);
        }
        SendInfo();
    }

    public void MusicLoop() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isLooping()) {
            mediaPlayer.setLooping(false);
            loop = false;
        } else {
            mediaPlayer.setLooping(true);
            loop = true;
        }
        SendInfo();
    }

    public void MusicSeek(int msek) {
        if (mediaPlayer == null) return;
        mediaPlayer.seekTo(msek);
    }

    //send info to all
    public void SendInfo() {
        updateNotification();

        Log.d("TestingErrorFocus", "onReceive: ");
        Intent in = new Intent("com.mascotworld.vkaudiomanager.send");
        if (mediaPlayer != null) {
            in.putExtra("duration", Integer.toString(mediaPlayer.getDuration()));
            Log.d("getInfoMP", "onReceive: " + mediaPlayer.getDuration());

        } else {
            in.putExtra("duration", "0");
        }


        in.putExtra("your", AudioUtil.normalizeString(schedule.getCurrentMusic().getYour()));
        in.putExtra("title", AudioUtil.normalizeString(schedule.getCurrentMusic().getTitle()));
        in.putExtra("artist", AudioUtil.normalizeString(schedule.getCurrentMusic().getArtist()));
        in.putExtra("pic", AudioUtil.normalizeString(schedule.getCurrentMusic().getPic()));


        if (mediaPlayer != null) {
            if (isPlaying) {
                in.putExtra("work", "false");

            } else {
                in.putExtra("work", "true");

            }
        } else {
            in.putExtra("work", "null");
        }


        sendBroadcast(in);

    }


    //control shedule
    public void setPosition(Music music) {

        Music tmp = schedule.getCurrentMusic();

        schedule.setMusic(music);
        if (!music.equals(tmp)) {
            decodeUrl();
        }

    }

    public void setMusicList(List<Music> newList) {
        schedule.setPlaylist(newList);
    }

    public void setMusicList(MusicSchedule newSchedule) {
        schedule = newSchedule;
    }

    public void shuffle() {
        schedule.shuffle();
        decodeUrl();
    }

    public static MusicSchedule getSchedule() {
        return schedule;
    }

    //save and get music info
    public void saveMusicInfo(Music music) {
        MusicSchedule music_schedule = getMusicInfo();

        int i = music_schedule.getPlaylist().size();
        // Toast.makeText(getApplicationContext(), Integer.toString(i), Toast.LENGTH_SHORT).show();

        File info = new File(loadText("pathCache") + "SavedMusicList.info");

        String wwrite = "|" + i + "|" + "|ARTIST|" + music.getArtist() + "|TITLE|" + music.getTitle() + "|PIC|" + music.getPic() + "|URL|" + music.getUrl() + "|YOUR|" + music.getYour() + "|LID|" + music.getLyrics_id() + "|ID|" + music.getData_id() + "|" + "|" + i + "|";


        try {
            FileWriter writer = new FileWriter(info.getPath(), true);
            BufferedWriter bufferWriter = new BufferedWriter(writer);
            bufferWriter.write(wwrite);
            bufferWriter.close();
        } catch (IOException e) {
            System.out.println(e);
        }
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

                    //                if (listsavemusic.contains(dataid + ".mp3"))
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

    public boolean findSavedMusic(Music music) {
        int i = 0;
        List<String> musicList = getInternalMusic();


        updateCachedMusic();
        while ((i < musicList.size())) {
            if (musicList.get(i).equals(music.getData_id() + ".mp3")) {
                return true;
            }
            i++;
        }

        return false;
    }

    public void deleteMusic(Music music) {

        deleteMusic = music;
        deleteThread.sendEmptyMessage(ADD_GOOD);


    }

    Handler deleteThread = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {

            schedule = getMusicInfo();

            int i = 0;
            while (i < schedule.size()) {
                if (schedule.getPlaylist().get(i).getData_id().equals(deleteMusic.getData_id())) {
                    schedule.getPlaylist().remove(i);
                    File info = new File(loadText("pathCache") + "SavedMusicList.info");
                    try {

                        FileOutputStream outputStream;
                        String wwrite = "";
                        outputStream = new FileOutputStream(info.getAbsolutePath());
                        outputStream.write(wwrite.getBytes());
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                i++;
            }


            for (i = 0; i < schedule.size(); i++) {
                saveMusicInfo(schedule.getPlaylist().get(i));
            }

        }
    };

    //getListsMusic

    public static MusicSchedule getCachedMusic() {
        return cachedMusic;
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

    public List<String> getSavedMusic() {

        List<String> filespath = new ArrayList<>();

        String path = loadText("pathFull");
        //Environment.DIRECTORY_MUSIC, music.artist + " - " + music.getTitle() + ".mp3"


        File file = new File(path);

        File[] files = file.listFiles();

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                filespath.add(files[i].getName());
            }
        }

        return filespath;

    }

    private void delInternalMusic(String id) {
        String path = loadText("pathCache");
        File filemp3 = new File(path, id);

        if (filemp3.delete()) {
            Toast.makeText(getApplicationContext(), getString(R.string.deleteMusic), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
        }


    }

    public void updateCachedMusic() {
        cachedMusic = getMusicInfo();
    }

    public static void setCachedMusic(MusicSchedule music) {
        cachedMusic = music;
    }


    //update url
    public void getMusicUrl(final Music music, final int type) {

        //act=reload_audio&al=1&ids=93024954_456239288


        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("al", "1")
                .add("act", "reload_audio")
                .add("ids", music.getData_id() + "_" + music.getRes_hash() + "_" + music.getDel_hash())
                .build();
        Log.d(TAG, "GetURL music: " + music.getData_id() + "_" + music.getRes_hash() + "_" + music.getDel_hash());
        final Request request = new Request.Builder()

                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("cookie", loadText("sid"))
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("origin", "https://vk.com")
                .addHeader("eferer", "https://vk.com/audios" + loadText("ownerId"))
                .post(formBody)
                .url("https://vk.com/al_audio.php")
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(getApplicationContext(), "Error get url of music", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();


                Log.d(TAG, "GetURL music: " + responseData);

                String musicurl = "";

                if (responseData.contains("<!json>[]<!>")) {
                    musicurl = "blocked";
                } else {
                    musicurl = "https://vk.com/mp3/audio_api_unavailable.mp3?extra=" + AudioUtil.pars("https:\\/\\/vk.com\\/mp3\\/audio_api_unavailable.mp3?extra=", responseData, "\"");
                }


                switch (type) {
                    case 1:
                        schedule.getCurrentMusic().setUrl(musicurl);

                        updateUrl.sendEmptyMessage(UPDATE_CURRENT);
                        break;
                    case 2: {

                        Music music1 = music;
                        music1.setUrl(musicurl);
                        tmpmusic = music1;
                        updateUrl.sendEmptyMessage(UPDATE_SAVE_CACHE);


                        break;
                    }
                    case 3: {
                        Music music1 = music;
                        music1.setUrl(musicurl);
                        tmpmusic = music1;
                        updateUrl.sendEmptyMessage(UPDATE_SAVE_FULL);


                        break;
                    }
                }


            }
        });


    }


    Handler updateUrl = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case UPDATE_CURRENT: {
                    decodeUrl();
                    break;
                }
                case UPDATE_SAVE_CACHE: {
                    decodeUrlForSave(tmpmusic, 1);

                    break;
                }
                case UPDATE_SAVE_FULL: {
                    decodeUrlForSave(tmpmusic, 2);

                    break;
                }
                case ADD_BAD: {
                    Toast.makeText(getApplicationContext(), "ADD bad", Toast.LENGTH_SHORT).show();
                    break;
                }
                case ADD_GOOD: {
                    Toast.makeText(getApplicationContext(), "ADD good", Toast.LENGTH_SHORT).show();
                    break;
                }
                case DELETE_BAD: {
                    Toast.makeText(getApplicationContext(), "DELETE bad", Toast.LENGTH_SHORT).show();
                    break;
                }
                case DELETE_GOOD: {
                    Toast.makeText(getApplicationContext(), "DELETE good", Toast.LENGTH_SHORT).show();
                    break;
                }
            }

        }
    };


    //work with notifications
    public void createNotificationsListeners() {

        ReceiverState = true;
        isNotificationListenersCreated = true;

        Log.d(TAG, "crNotification: setReceivers");

        brPrev = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(NotificationGenerator.NOTIFY_PREV)) {

                    MusicPrev();
                }


            }
        };

        IntentFilter intFilt3 = new IntentFilter("com.mascotworld.vkaudiomanager.prev");
        registerReceiver(brPrev, intFilt3);


        brPlay = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(NotificationGenerator.NOTIFY_PLAY)) {
                    MusicPlayPause();
                }


            }
        };

        IntentFilter intFilt = new IntentFilter("com.mascotworld.vkaudiomanager.play");
        registerReceiver(brPlay, intFilt);

        brClose = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                if (intent.getAction().equals(NotificationGenerator.NOTIFY_CLOSE)) {


                    MusicDestroy();


                }


            }
        };

        IntentFilter intFilt1 = new IntentFilter("com.mascotworld.vkaudiomanager.close");
        registerReceiver(brClose, intFilt1);


        brNext = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                if (intent.getAction().equals(NotificationGenerator.NOTIFY_NEXT)) {
                    MusicNext();
                }


            }
        };

        IntentFilter intFilt2 = new IntentFilter("com.mascotworld.vkaudiomanager.next");
        registerReceiver(brNext, intFilt2);


        brStopEx = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                    MusicPlayPause(false);
                }


            }
        };

        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(brStopEx, intentFilter);


        brMediaButtons = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {


                if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                    KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
                  /*  if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
                        MusicPlayPause(true);
                    }
                    if (KeyEvent.KEYCODE_MEDIA_PAUSE == event.getKeyCode()) {
                        MusicPlayPause(false);
                    }
                    if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == event.getKeyCode()) {
                        MusicPrev();
                    }
                    if (KeyEvent.KEYCODE_MEDIA_NEXT == event.getKeyCode()) {
                        MusicNext();
                    }*/
                }


            }
        };

        IntentFilter intentFilter4 = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        registerReceiver(brMediaButtons, intentFilter4);

    }

    private void Notifications(String picUrl, String artist, String title, final String work, boolean ongoing) {

//        SendInfo();

        if (!isNotificationListenersCreated) {
            createNotificationsListeners();
        }
        setMediaSession();

        NotificationGenerator.customBigNotification(getApplicationContext(), title, artist, picUrl, work, ongoing);
        startForeground(NotificationGenerator.NOTIFICATION_ID_CUSTOM_BIG, NotificationGenerator.notification);


    }

    private void updateNotification() {
        if (isPlaying)
            Notifications(schedule.getCurrentMusic().getPic(), schedule.getCurrentMusic().getArtist(), schedule.getCurrentMusic().getTitle(), "play", true);
        else
            Notifications(schedule.getCurrentMusic().getPic(), schedule.getCurrentMusic().getArtist(), schedule.getCurrentMusic().getTitle(), "pause", true);

    }


    //loadsettings
    private void saveText(String saved_text, String save) {
        sPref = getSharedPreferences(SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(saved_text, save);
        ed.apply();
    }

    private String loadText(String saved_text) {
        sPref = getSharedPreferences(SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }


    //mediacompat,focus,mediasession
    private MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public void onPlay() {
            super.onPlay();
            Log.d("TestingErrorFocus", "onPlay: ");
            MusicPlayPause(true);
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d("TestingErrorFocus", "onPause: ");
            MusicPlayPause(false);

        }

        @Override
        public void onStop() {
            super.onStop();
            Log.d("TestingErrorFocus", "onStop: ");
            MusicPlayPause(false);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            MusicNext();
            Log.d("TestingErrorFocus", "onSkipToNext: ");

        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            Log.d("TestingErrorFocus", "onSkipToPrevious: ");
            MusicPrev();
        }
    };

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AUDIOFOCUS_LOSS_TRANSIENT:
                    mediaSessionCallback.onPause();
                    Log.d("TestingErrorFocus", "loss_trans");
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    mediaSessionCallback.onPlay();
                    Log.d("TestingErrorFocus", "gain");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    mediaSessionCallback.onPause();
                    Log.d("TestingErrorFocus", "loss");
                    mMediaSessionCompat.setActive(false);
                    am.abandonAudioFocus(mOnAudioFocusChangeListener);
                    break;
            }

        }
    };

    private void setMediaSession() {

        final int playState;
        if (isPlaying) {
            playState = PlaybackStateCompat.STATE_PLAYING;
        } else {
            playState = PlaybackStateCompat.STATE_PAUSED;
        }


        final long playBackStateActions = PlaybackStateCompat.ACTION_PLAY |
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                PlaybackStateCompat.ACTION_PAUSE |
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                PlaybackStateCompat.ACTION_STOP;


        if (schedule.getCurrentMusic().getPic().equals("null") || schedule.getCurrentMusic().getPic().equals("none")) {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null);
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, schedule.getCurrentMusic().getTitle());
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, schedule.getCurrentMusic().getArtist());
            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, schedule.getNamePlaylist());
            if (mediaPlayer != null)
                metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration());
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, schedule.getPosition());
            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, schedule.size());
            metadata = metadataBuilder.build();
            mMediaSessionCompat.setMetadata(metadata);


            mMediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setActions(playBackStateActions)
                    .setActiveQueueItemId(schedule.getPosition())
                    .setState(playState, schedule.getPosition(), 1.0f).build());


        } else {

            Picasso.get()
                    .load(schedule.getCurrentMusic().getPic())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, schedule.getCurrentMusic().getTitle());
                            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, schedule.getCurrentMusic().getArtist());
                            metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, schedule.getNamePlaylist());
                            if (mediaPlayer != null)
                                metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration());
                            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, schedule.getPosition());
                            metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, schedule.size());
                            metadata = metadataBuilder.build();
                            mMediaSessionCompat.setMetadata(metadata);


                            mMediaSessionCompat.setPlaybackState(new PlaybackStateCompat.Builder()
                                    .setActions(playBackStateActions)
                                    .setActiveQueueItemId(schedule.getPosition())
                                    .setState(playState, schedule.getPosition(), 1.0f).build());
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        }

    }

    private void setFocus() {
        Log.d("TestingErrorFocus", "setFocus: ");

        if (!isPlaying) {
            // am.abandonAudioFocus(mOnAudioFocusChangeListener);
            stopForeground(false);
        } else {
            jsetFocus();
        }
        updateNotification();

    }

    boolean jsetFocus() {
        Log.d("TestingErrorFocus", "jsetFocus: ");
        int result = am.requestAudioFocus(mOnAudioFocusChangeListener,
                // Use the Music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d(TAG, "jsetFocus: ");

            mMediaSessionCompat.setActive(true);
            return true;
        }

        return false;
    }


}
