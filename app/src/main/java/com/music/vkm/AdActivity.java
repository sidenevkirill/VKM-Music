package com.music.vkm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by halez on 07.11.2017.
 */

public class AdActivity extends AppCompatActivity {

    public static InterstitialAd mInterstitialAd;

    private FirebaseAnalytics mFirebaseAnalytics;

    public static String SPreferences = "SettingsGeneralActivity";
    SharedPreferences sPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ad);


        switch (loadText("typead")) {
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
