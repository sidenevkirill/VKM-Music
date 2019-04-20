package com.music.vkm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.NonSkippableVideoCallbacks;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by halez on 07.11.2017.
 */

public class Ad extends AppCompatActivity {

    public static InterstitialAd mInterstitialAd;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static String SPreferences = "Settings";
    SharedPreferences sPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad);


        switch (loadText("typead")) {
            case "appodeal": {
                String appKey;
                if (loadText("idAppodeal").equals("")) {
                    appKey = "9515c8d41c8b539184ae2f90aa31f9619e37f5ed0c51f968";
                } else {
                    appKey = loadText("idAppodeal");
                }
                Appodeal.disableLocationPermissionCheck();
                if (loadText("type_appodeal").equals("non_skippable")) {
                    Appodeal.initialize(this, appKey, Appodeal.NON_SKIPPABLE_VIDEO);


                    Appodeal.setNonSkippableVideoCallbacks(new NonSkippableVideoCallbacks() {
                        @Override
                        public void onNonSkippableVideoLoaded() {
                            Appodeal.show(Ad.this, Appodeal.NON_SKIPPABLE_VIDEO);
                            Bundle params = new Bundle();
                            params.putString("Advert", "showed");
                            mFirebaseAnalytics.logEvent("OpenActivities", params);
                            Log.d("ADMusic", "load");
                        }

                        @Override
                        public void onNonSkippableVideoFailedToLoad() {
                            Toast.makeText(getApplicationContext(), "Error load video", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onNonSkippableVideoShown() {

                        }

                        @Override
                        public void onNonSkippableVideoFinished() {

                            Toast.makeText(getApplicationContext(), "Thank you for watching advertisiment", Toast.LENGTH_SHORT).show();
                            finish();

                        }

                        @Override
                        public void onNonSkippableVideoClosed(boolean b) {

                        }
                    });
                } else if (loadText("type_appodeal").equals("interstitial")) {
                    Appodeal.initialize(this, appKey, Appodeal.INTERSTITIAL);

                    Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
                        @Override
                        public void onInterstitialLoaded(boolean isPrecache) {
                            Log.d("Appodeal", "onInterstitialLoaded");
                            Appodeal.show(Ad.this, Appodeal.INTERSTITIAL);
                            Bundle params = new Bundle();
                            params.putString("Advert", "showed");
                            mFirebaseAnalytics.logEvent("OpenActivities", params);
                            Log.d("ADMusic", "load");
                        }

                        @Override
                        public void onInterstitialFailedToLoad() {
                            Log.d("Appodeal", "onInterstitialFailedToLoad");
                            Toast.makeText(getApplicationContext(), "Error load ad", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onInterstitialShown() {
                            Log.d("Appodeal", "onInterstitialShown");
                        }

                        @Override
                        public void onInterstitialClicked() {
                            Log.d("Appodeal", "onInterstitialClicked");
                        }

                        @Override
                        public void onInterstitialClosed() {
                            Log.d("Appodeal", "onInterstitialClosed");
                            Toast.makeText(getApplicationContext(), "Thank you for watching advertisiment", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                } else if (loadText("type_appodeal").equals("MREC")) {
                    Appodeal.initialize(this, appKey, Appodeal.MREC);


                } else{
                    Appodeal.initialize(this, appKey, Appodeal.REWARDED_VIDEO);

                    Appodeal.setRewardedVideoCallbacks(new RewardedVideoCallbacks() {
                        @Override
                        public void onRewardedVideoLoaded() {
                            Appodeal.show(Ad.this, Appodeal.REWARDED_VIDEO);
                            Bundle params = new Bundle();
                            params.putString("Advert", "showed");
                            mFirebaseAnalytics.logEvent("OpenActivities", params);
                            Log.d("ADMusic", "load");
                        }

                        @Override
                        public void onRewardedVideoFailedToLoad() {
                            Toast.makeText(getApplicationContext(), "Error load video", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onRewardedVideoShown() {

                        }

                        @Override
                        public void onRewardedVideoFinished(int i, String s) {
                            Toast.makeText(getApplicationContext(), "Thank you for watching advertisiment", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onRewardedVideoClosed(boolean b) {

                        }
                    });
                }
                break;
            }


            case "admob": {

                if (mInterstitialAd == null) {
                    mInterstitialAd = new InterstitialAd(this);
                    mInterstitialAd.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id));
                    mInterstitialAd.loadAd(new AdRequest.Builder().build());
                }

                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdClosed() {
                        Bundle params = new Bundle();
                        params.putString("Advert", "close");
                        mFirebaseAnalytics.logEvent("OpenActivities", params);
                        Log.d("ADMusic", "closed");
                        Toast.makeText(getApplicationContext(), "Спасибо за просмотр рекламы.", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onAdLoaded() {
                        super.onAdLoaded();
                        mInterstitialAd.show();
                        Bundle params = new Bundle();
                        params.putString("Advert", "showed");
                        mFirebaseAnalytics.logEvent("OpenActivities", params);
                        Log.d("ADMusic", "load");
                    }

                });


                break;
            }
            default:
                Toast.makeText(getApplicationContext(), "Рекламы на сегодня нет. Спасибо что смотрите ее:)", Toast.LENGTH_LONG).show();
                finish();
                break;
        }
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("Advert", "create");
        mFirebaseAnalytics.logEvent("OpenActivities", params);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();




        /*
        if (mInterstitialAd.isLoading()) {
            mInterstitialAd.setAdListener(null);
            Bundle params = new Bundle();
            params.putString("Advert", "destroyed");
            mFirebaseAnalytics.logEvent("OpenActivities", params);
            finish();
        }*/
    }

    void saveText(String saved_text, String save) {
        sPref = getSharedPreferences(SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(saved_text, save);
        ed.apply();
    }

    String loadText(String saved_text) {
        sPref = getSharedPreferences(SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }
}
