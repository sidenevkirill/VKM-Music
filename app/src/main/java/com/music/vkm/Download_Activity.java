package com.music.vkm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mascot on 06.10.2017.
 */

public class Download_Activity extends AppCompatActivity {
    public static List<Music> list = new ArrayList<>();
    boolean checked = false;
    String type;
    private SharedPreferences sPref;
    private AdView mAdView;




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_activity);

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.download_music_list_cache));
        toolbar.setTitleTextColor(Color.parseColor("#000000"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //helper.finish();
                finish();
            }
        });

        list.clear();
        list.addAll(Audio_list_fragment_my.schedule.getPlaylist());

        Intent intent = getIntent();
        list = (ArrayList<Music>) intent.getSerializableExtra("newPlayList");
        type = intent.getStringExtra("typedownload");
        if (type.equals("full")) {
            getSupportActionBar().setTitle(getResources().getString(R.string.download_music_list_phone));
        }

        if (list.size() == 0) {
            Toast.makeText(getApplicationContext(), "Количество аудиозаписей равно нулю.", Toast.LENGTH_SHORT).show();
            finish();
        }

        final RecyclerView recMusic = findViewById(R.id.rv_download);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        recMusic.setLayoutManager(llm);
        recMusic.setHasFixedSize(true);
        recMusic.setNestedScrollingEnabled(false);
        recMusic.addItemDecoration(new SpacesItemDecoration(5));


        final AdapterDownload adapter = new AdapterDownload(list);
        recMusic.setAdapter(adapter);


        Button choose_all = findViewById(R.id.choose_all);

        View.OnClickListener choose = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checked) {
                    checked = true;
                    adapter.setChecked(checked);

                } else {
                    checked = false;
                    adapter.setChecked(checked);

                }

            }
        };

        choose_all.setOnClickListener(choose);


        Button download = findViewById(R.id.download);


        View.OnClickListener download_listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if (adapter.getCountChecked() > 0) {

                    if (type.equals("full")) {
                       Audio_main_activity.mBoundService.downloadListMusic(adapter.getDownloadList(), 2);
                    } else {
                        Audio_main_activity.mBoundService.downloadListMusic(adapter.getDownloadList(), 1);
                    }
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Не выбрано", Toast.LENGTH_SHORT).show();
                }


            }
        };

        download.setOnClickListener(download_listener);


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




}
