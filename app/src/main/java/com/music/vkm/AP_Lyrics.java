package com.music.vkm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by mascot on 04.10.2017.
 */

public class AP_Lyrics extends android.support.v4.app.Fragment {

    BroadcastReceiver service;
    static View view;
    SharedPreferences sPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_audio_player_lyrics, container, false);


        view = v;
        getInfo();

        SetInfo(MusicService.getSchedule().getCurrentMusic().getArtist(), MusicService.getSchedule().getCurrentMusic().getTitle());


        return v;
    }

    public static AP_Lyrics newInstance(String text) {

        AP_Lyrics f = new AP_Lyrics();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }




    void SetInfo(String art, String titl) {
        TextView artist = view.findViewById(R.id.artist_lyrics);

        TextView title = view.findViewById(R.id.new_title_lyrics);


        if (art.equals(null)) {
            art = "null";
        }

        if (titl.equals(null)) {
            titl = "null";
        }

        artist.setText(wstr.normalizeString(art));

        title.setText(wstr.normalizeString(titl));

        TextView new_lyrics = view.findViewById(R.id.new_lyrics);

        //


        Log.d("TestLyrics", MusicService.getSchedule().getCurrentMusic().getArtist());
        if (!MusicService.getSchedule().getCurrentMusic().getLyrics_id().equals("null")) {
            new_lyrics.setText(getResources().getText(R.string.loading));
            getLyrics(MusicService.getSchedule().getCurrentMusic().getLyrics_id(), MusicService.getSchedule().getCurrentMusic().getData_id());

        }
        else new_lyrics.setText("No lyrics");
    }


    void getInfo() {
        service = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.mascotworld.vkaudiomanager.send")) {

                    SetInfo(intent.getStringExtra("artist"), intent.getStringExtra("title"));

                }
            }
        };

        IntentFilter intFilt3 = new IntentFilter("com.mascotworld.vkaudiomanager.send");
        getActivity().registerReceiver(service, intFilt3);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(service);
    }


    void getLyrics(String lyrics_id,String data_id) {
        Log.d("TestLyrics", "getLyrics: ");
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();



        RequestBody formBody = new FormBody.Builder()
                .add("act", "get_lyrics")
                .add("aid", data_id)
                .add("al", "1")
                .add("lid", lyrics_id)
                .build();

        Request request = new Request.Builder()
                .addHeader("accept-language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("referer", "https://vk.com/audios" + loadText("ownerid"))
                .addHeader("Accept", "*/*")
                .addHeader("User-Agent", getString(R.string.useragentAndroid))
                .addHeader("Cookie", loadText("sid") + "; remixmdevice=1366/768/1/!!-!!!!")
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

                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = view.findViewById(R.id.new_lyrics);
                            // textView.setText(wstr.pars("<!>0<!>",responseData,""));
                            String lyrics = responseData.substring(responseData.lastIndexOf("<!>"));
                            textView.setText(Html.fromHtml(lyrics).toString());
                        }
                    });
            }
        });

    }

    void saveText(String saved_text, String save) {
        if (getContext() != null) {
            sPref = getContext().getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString(saved_text, save);
            ed.commit();
        }
    }

    private String loadText(String saved_text) {
        SharedPreferences sPref = getContext().getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }
}