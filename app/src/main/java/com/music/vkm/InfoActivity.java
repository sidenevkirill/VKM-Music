package com.music.vkm;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.anjlab.android.iab.v3.BillingProcessor;


public class InfoActivity extends AppCompatActivity {

    BillingProcessor bp;
    Button purchaseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_view);


        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.about));
        toolbar.setTitleTextColor(Color.parseColor("#000000"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }


    public void Click(View view) {
        Uri address = Uri.parse("https://vk.com/sidenev_kirill");
        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
        startActivity(openlinkIntent);

    }

    public void Desinger(View view) {
        Uri address = Uri.parse("https://vk.com/fullx0");
        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
        startActivity(openlinkIntent);

    }

    public void ab(View view) {
        Uri address = Uri.parse("https://vk.com/wtfmu");
        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
        startActivity(openlinkIntent);

    }

    public void telegram(View view) {
        Uri address = Uri.parse("https://t.me/vkmusic_new");
        Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
        startActivity(openlinkIntent);

    }

}