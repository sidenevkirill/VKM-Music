package com.music.vkm;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.multidex.MultiDex;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Audio_main_activity extends AppCompatActivity implements BillingProcessor.IBillingHandler {
    private FirebaseAnalytics mFirebaseAnalytics;
    private BroadcastReceiver sheet, seek;
    static public BottomSheetBehavior mBottomSheetBehavior;
    private SharedPreferences sPref;
    public InterstitialAd mInterstitialAd;
    private int peekHeight;
    int size = 0;
    public static MusicService mBoundService;
    private static final int PERMISSION_REQUEST_CODE = 123;
    public static String path = "/.vkaudio/";
    static SearchView searchView;
    static MenuItem searchMenuItem;
    private AdView mAdView;

    private BottomNavigationView mMainNav;
    private DrawerLayout mDrawerLayout;
    BillingProcessor bp;


    String TAG = "Main_Activity";

    private int typePager = 0, lastTypePager = 0;
    private profileFragment profileFragment;


    private List<String> getInternalMusic() {

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

    private List<String> getListMusic(String localpath) {

        List<String> filespath = new ArrayList<>();

        String path = localpath;

        File file = new File(path);

        File[] files = file.listFiles();

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                filespath.add(files[i].getName());
            }
        }

        return filespath;

    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (loadText("advertisiment").equals(""))
            saveText("advertisiment", "true");

        if (loadText("watch_ad").equals("")) {
            saveText("watch_ad", "true");
        }


        if (loadText("pathCache").equals("")) {
            Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + path);
            Log.d("TestSavingMusic", "PathCache: " + destinationUri.getEncodedPath());
            saveText("pathCache", destinationUri.getEncodedPath());
        }

        if (loadText("pathFull").equals("")) {
            Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/Music/");
            Log.d("TestSavingMusic", "pathFull: " + destinationUri.getEncodedPath());
            saveText("pathFull", destinationUri.getEncodedPath());
        }


        if (!loadText("pathCache").contains("/storage/")) {
            Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + path);
            Log.d("TestSavingMusic", "PathCache: " + destinationUri.getEncodedPath());
            saveText("pathCache", destinationUri.getEncodedPath());
        }

        if (!loadText("pathFull").contains("/storage/")) {
            Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/Music/");
            Log.d("TestSavingMusic", "pathFull: " + destinationUri.getEncodedPath());
            saveText("pathFull", destinationUri.getEncodedPath());
        }

        if (loadText("userAgent").equals(""))
            saveText("userAgent", getString(R.string.useragentApple));


        Log.d("ownerid", loadText("ownerid"));
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.ITEM_ID, "id" + loadText("ownerid"));
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, params);


        if (loadText("sid").equals("")) {
            Intent newIntent = new Intent(Audio_main_activity.this, Log_In.class);
            startActivity(newIntent);
            finish();
        } else {

            setContentView(R.layout.audio_activity);


            MobileAds.initialize(this,
                    "");

            mAdView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);





/*

            mMainNav = findViewById(R.id.viewpager_main);

            profileFragment = new ProfileFragment();
            notificationFragment = new NotificationFragment();
            settingFragment = new SettingFragment();


            mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.recents:
                            mMainNav.setItemBackgroundResource(R.color.colorPrimary);
                            setFragment(profileFragment);
                            return true;

                        case R.id.favourites:
                            setFragment(notificationFragment);
                            mMainNav.setItemBackgroundResource(R.color.colorAccent);
                            return true;

                        case R.id.nearby:
                            setFragment(settingFragment);
                            mMainNav.setItemBackgroundResource(R.color.colorPrimaryDark);
                            return true;


                        default:
                            return false;
                    }
                }

                private void setFragment(Fragment fragment) {

                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.viewpager_main, fragment);
                    fragmentTransaction.commit();
                }

            });
        }

*/


            FrameLayout bottomSheetLayout = findViewById(R.id.bottom_sheet_full);

            mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            mBottomSheetBehavior.setHideable(false);


            peekHeight = mBottomSheetBehavior.getPeekHeight();

            mBottomSheetBehavior.setPeekHeight(0);

            Intent intentbehavior = getIntent();

            if (MusicService.mediaPlayer != null) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }


            final FloatingActionButton fab = findViewById(R.id.fab);

            View.OnClickListener AdClick = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Audio_main_activity.this, Ad.class);
                    startActivity(intent);

                }


            };


            fab.setOnClickListener(AdClick);

            final RelativeLayout new_player = findViewById(R.id.new_player);
            final FrameLayout layout_bottom_sheet = findViewById(R.id.layout_bottom_sheet);

            new_player.setVisibility(View.INVISIBLE);
            layout_bottom_sheet.setVisibility(View.VISIBLE);
            final AppBarLayout appbar = findViewById(R.id.appbar);

            mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {

                    Log.d("StateSheet", "onStateChanged: " + newState);


                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    fab.animate().scaleX(1 - slideOffset).scaleY(1 - slideOffset).setDuration(0).start();
                    layout_bottom_sheet.animate().alpha(1 - slideOffset).setDuration(0).start();
                    new_player.animate().alpha(0 + slideOffset).setDuration(0).start();
                    appbar.animate().alpha(1 - slideOffset).setDuration(0).start();


                    if (slideOffset > 0 && slideOffset < 1) {
                        new_player.setVisibility(View.VISIBLE);
                        layout_bottom_sheet.setVisibility(View.VISIBLE);
                        appbar.setVisibility(View.VISIBLE);
                    }

                    if (slideOffset == 1) {
                        new_player.setVisibility(View.VISIBLE);
                        layout_bottom_sheet.setVisibility(View.INVISIBLE);
                        appbar.setVisibility(View.INVISIBLE);

                    }
                    if (slideOffset == 0) {
                        new_player.setVisibility(View.INVISIBLE);
                        layout_bottom_sheet.setVisibility(View.VISIBLE);
                        appbar.setVisibility(View.VISIBLE);
                    }


                }
            });


            if (loadText("advertisiment").equals("false")) {
                fab.setVisibility(View.INVISIBLE);
            }


            Toolbar toolbar = findViewById(R.id.toolbar_main);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(getResources().getString(R.string.audio));
            toolbar.setTitleTextColor(Color.parseColor("#000000"));


            TabLayout tab_main = findViewById(R.id.tab_main);
            ViewPager viewpager_main = findViewById(R.id.viewpager_main);
            setupViewPagerDef(viewpager_main);
            tab_main.setupWithViewPager(viewpager_main);

            bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvOmr2A4lvT2q7aemS7HSmLEiNBgIzn4GtSzkydS8fuop1Ia3C0gxK22HyNQml2vD3FS+4pa5fjQTlQnTt2ptlb3GCP8I0lBQsh0rxsZwySiMZG0Oa6APqPsOGiLpyj0CBGRiXGjYM9e1YIwFHOcZsLuHOpWUCJvs7+SIB+BjN0vqcjlKQaiO3hZ/YyqOZhuNfsdqVE0w0NVtwl896igSIE/7tG5OtgBJCFykU3LluJEH6smeef7AA064CUurpW2k7dUTTveVM2HXeo+CjBY/eRciP1ZXmZ4G1NUNTyihSagzbj+ZJKowXQOKpLr5qmrPHzlkcMYGMdXso0+rVyE56QIDAQAB", this);
            bp.initialize();

            Listeners();
            getInfo();
            setListenersforSheet();

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

            Intent notificationIntent = new Intent(this, NotificationService.class);
            PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 5);


            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_HALF_DAY, broadcast);
            saveText("timeNotify", Long.toString(cal.getTimeInMillis()));


        }


    }


    void Listeners() {

/*
        drawdrawer();

*/
        listenSheet();

        ImageView audio_panel_play = findViewById(R.id.audio_panel_play);

        View.OnClickListener ActionPlayPause = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBoundService.MusicPlayPause();

            }
        };

        audio_panel_play.setOnClickListener(ActionPlayPause);

        ImageView audio_panel_prev = findViewById(R.id.audio_panel_prev);

        View.OnClickListener ActionPrev = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBoundService.MusicPrev();

            }
        };

        audio_panel_prev.setOnClickListener(ActionPrev);

        ImageView audio_panel_next = findViewById(R.id.audio_panel_next);

        View.OnClickListener ActionNext = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mBoundService.MusicNext();

            }
        };

        audio_panel_next.setOnClickListener(ActionNext);

        final FrameLayout bottomSheetLayout = findViewById(R.id.layout_bottom_sheet);

        View.OnClickListener OpenPlayer = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            }
        };

        bottomSheetLayout.setOnClickListener(OpenPlayer);


    }

    void listenSheet() {

        sheet = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.mascotworld.vkaudiomanager.send")) {

                    ImageView start = findViewById(R.id.audio_panel_play);

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


                    Log.d("ListenSheet", "onReceive: " + mBottomSheetBehavior.getState());

                    final ImageButton new_shuffle = findViewById(R.id.new_shuffle);

                    if (MusicService.getSchedule().isShuffle()) {

                        new_shuffle.setColorFilter(getResources().getColor(R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
                    } else {
                        new_shuffle.setColorFilter(getResources().getColor(R.color.colorGray), android.graphics.PorterDuff.Mode.MULTIPLY);
                    }


                    if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                        if (MusicService.getSchedule().getCurrentMusic() != null)
                            openSheet();
                    } else if (mBottomSheetBehavior.getPeekHeight() == 0) {
                        if (MusicService.getSchedule().getCurrentMusic() != null)
                            openSheet();
                    } else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {

                    } else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                    }


                }
            }
        };

        IntentFilter intFilt3 = new IntentFilter("com.mascotworld.vkaudiomanager.send");
        registerReceiver(sheet, intFilt3);

        seek = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.mascotworld.vkaudiomanager.sendseek")) {

                    ProgressBar audio_panel_progress = findViewById(R.id.audio_panel_progress);

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


    public void closeSheet() {
        ViewPager viewpager_main = findViewById(R.id.viewpager_main);
        viewpager_main.setPadding(0, 0, 0, 0);

        mBottomSheetBehavior.setPeekHeight(0);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {


            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }
    }

    void openSheet() {
        ViewPager viewpager_main = findViewById(R.id.viewpager_main);
        viewpager_main.setPadding(0, 0, 0, peekHeight - 4);

        if (mBottomSheetBehavior != null) {
            mBottomSheetBehavior.setPeekHeight(peekHeight);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void setupViewPagerPlayer(ViewPager viewPager) {
        Audio_FragmentAdapter adapter = new Audio_FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new AP_Lyrics(), getResources().getString(R.string.mmusic));
        adapter.addFragment(new AP_MusicPlayer(), getResources().getString(R.string.savedmusic));
        adapter.addFragment(new AP_PlayList(), getResources().getString(R.string.popular));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
    }

    private void setListenersforSheet() {

        RelativeLayout close_ap_act = findViewById(R.id.close_ap_act);

        View.OnClickListener close = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        };

        close_ap_act.setOnClickListener(close);

        ViewPager pager = findViewById(R.id.AudioPager);
        //pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
        setupViewPagerPlayer(pager);


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


                mBoundService.MusicLoop();
                if (MusicService.loop) {
                    new_repeat.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
                } else {
                    new_repeat.setColorFilter(getResources().getColor(R.color.colorGray), android.graphics.PorterDuff.Mode.MULTIPLY);
                }


            }
        };


        new_repeat.setOnClickListener(ActionRepeat);


    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.test:
                Intent intent = new Intent(Audio_main_activity.this, Download_Activity.class);
                intent.putExtra("typedownload", "full");
                intent.putExtra("newPlayList", (Serializable) Audio_list_fragment_my.schedule.getPlaylist());
                startActivity(intent);
                return false;


            case R.id.ad:
                startActivity(new Intent(this, Ad.class));
                return true;


            case R.id.ads:
                bp.purchase(Audio_main_activity.this, "15_coin");
                bp.consumePurchase("15_coin");
                return true;


        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menus; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);


        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_view, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                setupViewPagerSearch(viewpager_main, query);

                return false;
            }


            @Override
            public boolean onQueryTextChange(String newText) {

                if (typePager == 0) {
                    AdapterMusic adapter = Audio_list_fragment_my.adapter;
                    adapter.getFilter().filter(newText);
                    adapter = Audio_list_fragment_save.adapter;
                    adapter.getFilter().filter(newText);
                } else if (typePager == 1) {
                    AdapterMusic adapter = Audio_list_fragment_recommendations.adapter;
                    adapter.getFilter().filter(newText);
                    adapter = Audio_list_fragment_popular.adapter;
                    adapter.getFilter().filter(newText);
                }


                return false;
            }
        });
        searchMenuItem = menu.findItem(R.id.search);

        searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                if (lastTypePager == 0) {
                    ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                    setupViewPagerDef(viewpager_main);
                } else if (lastTypePager == 1) {
                    ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                    setupViewPagerRec(viewpager_main);
                }

                return true;
            }
        });


        return true;

    }


    private void setupViewPagerDef(ViewPager viewPager) {
        Audio_FragmentAdapter adapter = new Audio_FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new Audio_list_fragment_my(), getResources().getString(R.string.mmusic));
        adapter.addFragment(new Audio_list_fragment_save(), getResources().getString(R.string.savedmusic));
        adapter.addFragment(new Audio_list_fragment_recommendations(), getResources().getString(R.string.popular));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
        typePager = 0;
        lastTypePager = 0;
    }


    public void setupViewPagerRec(ViewPager viewPager) {
        Audio_FragmentAdapter adapter = new Audio_FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new Audio_list_fragment_popular(), getResources().getString(R.string.mmusic));
        adapter.addFragment(new Audio_list_fragment_save(), getResources().getString(R.string.mmusic));
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        typePager = 1;
        lastTypePager = 1;
    }


    public void setupViewPagerSearch(ViewPager viewPager, String query) {
        Audio_search_fragment.query = query;
        Audio_FragmentAdapter adapter = new Audio_FragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new Audio_search_fragment(), getResources().getString(R.string.mmusic));
        viewPager.setAdapter(adapter);
        typePager = 2;
    }


/*    void drawdrawer() {

        String versionName = "null";

        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName("VKM").withEmail("Музыка ВКонтакте").withIcon(getResources().getDrawable(R.drawable.logos))
                )
                .withSelectionListEnabledForSingleProfile(false)
                .withTextColor(Color.BLACK)
                .build();

        final Toolbar toolbar = findViewById(R.id.toolbar_main);

        final Drawer drawer = new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBar(true)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(1)
                .addDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.mymusic).withTextColor(Color.BLACK).withIcon(R.drawable.music_note),
                        new SecondaryDrawerItem().withName(R.string.download_cache).withTextColor(Color.BLACK).withIcon(R.drawable.check_decagram),
                        new SecondaryDrawerItem().withName(R.string.popular).withTextColor(Color.BLACK).withIcon(R.drawable.star),
                        new SecondaryDrawerItem().withName(R.string.saving).withTextColor(Color.BLACK).withIcon(R.drawable.ic_queue_music_black_24dp),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.help).withTextColor(Color.BLACK).withIcon(R.drawable.ic_help_outline_black_24dp),
                        new SecondaryDrawerItem().withName(R.string.action_settings).withTextColor(Color.BLACK).withIcon(R.drawable.ic_settings_black_24dp),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.ad).withTextColor(Color.BLACK).withIcon(R.drawable.ic_euro_symbol_black_24dp)

                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        //  Toast.makeText(getApplicationContext(), Integer.toString(position), Toast.LENGTH_SHORT).show();
                        drawerItem.withSelectable(false);
                        switch (position) {
                            case 1: {
                                if (typePager == 0) {
                                    ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                                    viewpager_main.setCurrentItem(0);
                                } else {
                                    ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                                    setupViewPagerDef(viewpager_main);
                                }
                                return false;

                            }

                            case 2: {
                                Intent intent = new Intent(Audio_main_activity.this, Download_Activity.class);
                                intent.putExtra("typedownload", "cache");
                                intent.putExtra("newPlayList", (Serializable) Audio_list_fragment_my.schedule.getPlaylist());
                                startActivity(intent);

                                return false;
                            }

                            case 3: {


                                if (typePager == 1) {
                                    ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                                    viewpager_main.setCurrentItem(0);
                                } else {
                                    ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                                    setupViewPagerRec(viewpager_main);
                                    viewpager_main.setCurrentItem(0);
                                }


                                return false;

                            }
                            case 4: {
                                if (typePager == 1) {
                                    ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                                    viewpager_main.setCurrentItem(1);
                                } else {
                                    ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                                    setupViewPagerRec(viewpager_main);
                                    viewpager_main.setCurrentItem(1);
                                }


                                return false;

                            }


                            case 6: {
                                Intent intent = new Intent(Audio_main_activity.this, Info.class);
                                startActivity(intent);
                                return false;
                            }
                            case 7: {
                                Intent intent = new Intent(Audio_main_activity.this, Settings.class);
                                startActivity(intent);
                                return false;
                            }
                            case 9: {
                                Intent intent = new Intent(Audio_main_activity.this, Ad.class);
                                startActivity(intent);
                                return false;
                            }

                        }

                        return true;
                    }
                })

                .build();

    }

    */

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


    void update() {
        mBoundService.SendInfo();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (MusicService.ServiceBinded) {
            unbindService(mServiceConnection);
        }

        if (sheet != null)
            unregisterReceiver(sheet);
        if (seek != null)
            unregisterReceiver(seek);
    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    public static ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MusicService.ServiceBinded = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
            mBoundService = myBinder.getService();
            MusicService.ServiceBinded = true;
            Log.d("Binder", "onServiceConnected: ");
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        if (!MusicService.ServiceBinded) {
            Intent intent = new Intent(this, MusicService.class);
            startService(intent);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "onStart: StartBinding");
        }


        if (MusicService.mediaPlayer != null) {
            openSheet();
        }


    }


    void getInfo() {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        final Request request = new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url("https://raw.githubusercontent.com/sidenevkirill/Sidenevkirill.github.io")
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();


                Audio_main_activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                        viewpager_main.setCurrentItem(1);
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {


                final String responseData = response.body().string();
                final int respcode = response.code();
                Log.d("AudioActivityOutput", "onResponse: " + responseData);

                final String version = wstr.pars("|VER|", responseData, "|");
                final String status = wstr.pars("|STATUS|", responseData, "|");
                final String url = wstr.pars("|URL|", responseData, "|");
                final String needhelp = wstr.pars("|NEEDHELP|", responseData, "|");
                final String updateinfo = wstr.pars("|UPDATEINFO|", responseData, "|");
                final String type_appodeal;
                final String typead;
                final String idAppodeal;
                final String addAppodeal = wstr.pars("|addAppodeal|", responseData, "|");


                String info = wstr.pars("|packageName|", responseData, "]|") + "]";

                String infoPackage = wstr.pars(getPackageName() + ":[", info, "]");
                if (infoPackage.contains("typeAd")) {
                    type_appodeal = wstr.pars("|typeappodeal|", infoPackage, "|");
                    typead = wstr.pars("|typeAd|", infoPackage, "|");
                    idAppodeal = wstr.pars("|idAppodeal|", infoPackage, "|");
                } else {
                    type_appodeal = wstr.pars("|type_appodeal|", responseData, "|");
                    typead = wstr.pars("|typead|", responseData, "|");
                    idAppodeal = wstr.pars("|idAppodeal|", responseData, "|");
                }
                Log.i("AudioActivityOutput", "onResponse: " + type_appodeal + "!!!!" + idAppodeal);


                if (addAppodeal.equals("null")) {
                    saveText("type_appodeal", type_appodeal);
                } else {
                    saveText("type_appodeal", addAppodeal);
                }

                saveText("needhelp", needhelp);
                saveText("typead", typead);
                saveText("idAppodeal", idAppodeal);


                saveText("url", url);

                Audio_main_activity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!responseData.contains("|VER|")) {
                            Audio_main_activity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ViewPager viewpager_main = findViewById(R.id.viewpager_main);
                                    viewpager_main.setCurrentItem(1);
                                }
                            });
                        }
                        String versionName = "null";
                        int versionCode = 0;

                        try {
                            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }

                        if (respcode == 200) {


                            int versionCodeint = 0;

                            try {
                                versionCodeint = Integer.parseInt(version);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }

                            Log.d("TestCheckVersion", "run: " + versionCodeint);
                            if (versionCodeint > versionCode && (versionCodeint != 0)) {
                                if (!url.equals("none")) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Audio_main_activity.this, R.style.AlertDialog);
                                    builder.setCancelable(false);
                                    builder.setTitle(getResources().getString(R.string.update));
                                    if (updateinfo.equals("none")) {
                                        builder.setMessage(getResources().getString(R.string.newBuild));
                                    } else {
                                        builder.setMessage(updateinfo);
                                    }
                                    builder.setPositiveButton(getResources().getString(R.string.open), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            downloadUpdate();

                                        }
                                    });
                                    builder.setNegativeButton(getResources().getString(R.string.close), null);
                                    builder.show();
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Audio_main_activity.this, R.style.AlertDialog);
                                    builder.setCancelable(false);
                                    builder.setTitle(getResources().getString(R.string.errorStatus));
                                    if (updateinfo.equals("none")) {
                                        builder.setMessage(getResources().getString(R.string.newBuild));
                                    } else {
                                        builder.setMessage(updateinfo);
                                    }
                                    builder.setNegativeButton(getResources().getString(R.string.close), null);
                                    builder.show();
                                }
                            } else {
                                if (!status.equals("ok")) {
                                    if (!loadText("status").equals(status)) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(Audio_main_activity.this, R.style.AlertDialog);
                                        builder.setCancelable(false);
                                        builder.setTitle(getResources().getString(R.string.errorStatus));
                                        builder.setMessage(status);
                                        builder.setPositiveButton("OK", null);
                                        builder.setNegativeButton("Больше не показывать", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                saveText("status", status);
                                            }
                                        });
                                        builder.show();
                                    }
                                }
                            }
                        }
                        getUsers();
                    }


                });


            }
        });


    }

    public void getUsers() {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        final Request request = new Request.Builder()
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .url("https://raw.githubusercontent.com/sidenevkirill/Sidenevkirill.github.io/master/donate_users.txt")
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();


            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {


                final String responseData = response.body().string();
                //   Log.d("Users_Without_Ad", responseData);


                if (loadText("ownerid").length() > 0) {
                    boolean test2 = Character.isDigit(loadText("ownerid").charAt(0));
                    if (test2)
                        if (responseData.contains("|" + loadText("ownerid") + "|")) {
                            saveText("advertisiment", "false");
                            Log.d("Check_ad", "false");
                        } else {
                            saveText("advertisiment", "true");
                            Log.d("Check_ad", "true");
                        }
                    else {
                        saveText("advertisiment", "true");
                        Log.d("Check_ad", "true");
                    }
                }


            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    void downloadUpdate() {
        if (loadText("url").contains("play.google.com")) {
            Uri address = Uri.parse(loadText("url"));
            Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
            startActivity(openlinkIntent);
        } else {
            DownloadManager downloadManager = (DownloadManager) getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
            Uri music_uri = Uri.parse(loadText("url"));
            DownloadManager.Request request = new DownloadManager.Request(music_uri);
            request.setTitle(getString(R.string.downloadUpdate));
            request.setDescription(loadText("url"));

            final Uri destinationUri = Uri.parse("/store/apps/details?id=com.music.vkm");
            Log.d("TestSavingApk", "TestSavingApk: " + destinationUri.toString());
            request.setDestinationUri(destinationUri);


            final Long reference = downloadManager.enqueue(request);

            BroadcastReceiver download = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (reference == referenceId) {
                        installUpdate(destinationUri.toString());

                    }


                }
            };

            IntentFilter intFilt = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            getApplicationContext().registerReceiver(download, intFilt);
        }
    }


    void installUpdate(String path) {
        Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        installIntent.setData(Uri.parse(path));
        //  installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(installIntent);
        // ((Activity) context).startActivityForResult(installIntent, Constants.APP_INSTALL_REQUEST);
    }


    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {

    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {

    }

    @Override
    public void onBillingInitialized() {

    }
}



