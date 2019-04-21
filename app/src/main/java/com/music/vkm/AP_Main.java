package com.music.vkm;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.bluzwong.swipeback.SwipeBackActivityHelper;
import com.google.firebase.analytics.FirebaseAnalytics;

import static com.music.vkm.Audio_main_activity.mBoundService;

/**
 * Created by mascot on 04.10.2017.
 */

public class AP_Main extends AppCompatActivity {
    SwipeBackActivityHelper helper;
    private FirebaseAnalytics mFirebaseAnalytics;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_audio_player_activity);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("OpenPlayer", "new");
        mFirebaseAnalytics.logEvent("OpenActivities", params);

        helper = new SwipeBackActivityHelper();
        helper.setEdgeMode(true)
                .setParallaxMode(true)
                .setParallaxRatio(3)
                .setNeedBackgroundShadow(true)
                .init(this);

        setListners();


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onBackPressed() {
        helper.finish();
    }

    private void setupViewPager(ViewPager viewPager) {
        Audio_FragmentAdapter adapter = new Audio_FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new AP_Lyrics(), getResources().getString(R.string.mmusic));
        adapter.addFragment(new AP_MusicPlayer(), getResources().getString(R.string.mmusic));
        adapter.addFragment(new AP_PlayList(), getResources().getString(R.string.mmusic));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    private void setListners() {

        RelativeLayout close_ap_act = findViewById(R.id.close_ap_act);

        View.OnClickListener close = v -> finish();

        close_ap_act.setOnClickListener(close);

        ViewPager pager = findViewById(R.id.AudioPager);
        //pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        setupViewPager(pager);


        final ImageView dot1 = findViewById(R.id.dot1);
        final ImageView dot2 = findViewById(R.id.dot2);
        final ImageView dot3 = findViewById(R.id.dot3);


        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {


            }

            @Override
            public void onPageSelected(int position) {

                switch (position) {
                    case 0: {
                        dot1.setColorFilter(getResources().getColor(R.color.black));
                        dot2.setColorFilter(getResources().getColor(R.color.colorGray));
                        dot3.setColorFilter(getResources().getColor(R.color.colorGray));
                        break;
                    }
                    case 1: {
                        dot1.setColorFilter(getResources().getColor(R.color.colorGray));
                        dot2.setColorFilter(getResources().getColor(R.color.black));
                        dot3.setColorFilter(getResources().getColor(R.color.colorGray));
                        break;
                    }
                    case 2: {
                        dot1.setColorFilter(getResources().getColor(R.color.colorGray));
                        dot2.setColorFilter(getResources().getColor(R.color.colorGray));
                        dot3.setColorFilter(getResources().getColor(R.color.black));
                        break;
                    }

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pager.setCurrentItem(1);

        final ImageButton new_shuffle = findViewById(R.id.new_shuffle);

        if (MusicService.getSchedule().isShuffle()) {

            new_shuffle.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            new_shuffle.setColorFilter(getResources().getColor(R.color.colorGray), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        View.OnClickListener Actionshuffle = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MusicService.getSchedule().shuffle();
                if (MusicService.getSchedule().isShuffle()) {
                    new_shuffle.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
                } else {
                    new_shuffle.setColorFilter(getResources().getColor(R.color.colorGray), android.graphics.PorterDuff.Mode.MULTIPLY);
                }

            }
        };

        new_shuffle.setOnClickListener(Actionshuffle);


        final ImageButton new_repeat = findViewById(R.id.new_repeat);
        if (MusicService.loop) {
            new_repeat.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
        } else {
            new_repeat.setColorFilter(getResources().getColor(R.color.colorGray), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        View.OnClickListener ActionRepeat = new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                MusicLoop();
                if (MusicService.loop) {
                    new_repeat.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
                } else {
                    new_repeat.setColorFilter(getResources().getColor(R.color.colorGray), android.graphics.PorterDuff.Mode.MULTIPLY);
                }


            }
        };


        new_repeat.setOnClickListener(ActionRepeat);
    }


    void MusicLoop() {

        mBoundService.MusicLoop();
    }


}
