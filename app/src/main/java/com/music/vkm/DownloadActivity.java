package com.music.vkm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.music.vkm.adapter.DownloadAdapter;
import com.music.vkm.item.Music;
import com.music.vkm.widget.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mascot on 06.10.2017.
 */

public class DownloadActivity extends AppCompatActivity {
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
        list.addAll(AudioListFragmentMy.schedule.getPlaylist());

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


        final DownloadAdapter adapter = new DownloadAdapter(list);
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
                        AudioMainActivity.mBoundService.downloadListMusic(adapter.getDownloadList(), 2);
                    } else {
                        AudioMainActivity.mBoundService.downloadListMusic(adapter.getDownloadList(), 1);
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
        sPref = getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(saved_text, save);
        ed.commit();
    }

    String loadText(String saved_text) {
        sPref = getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }


}
