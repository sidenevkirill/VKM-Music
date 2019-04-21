package com.music.vkm;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;

public class SettingsActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler {

    BillingProcessor bp;
    Button purchaseBtn;

    private final static String SELECTED_THEME = "THEME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        toolbar.setTitleTextColor(Color.parseColor("#000000"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvOmr2A4lvT2q7aemS7HSmLEiNBgIzn4GtSzkydS8fuop1Ia3C0gxK22HyNQml2vD3FS+4pa5fjQTlQnTt2ptlb3GCP8I0lBQsh0rxsZwySiMZG0Oa6APqPsOGiLpyj0CBGRiXGjYM9e1YIwFHOcZsLuHOpWUCJvs7+SIB+BjN0vqcjlKQaiO3hZ/YyqOZhuNfsdqVE0w0NVtwl896igSIE/7tG5OtgBJCFykU3LluJEH6smeef7AA064CUurpW2k7dUTTveVM2HXeo+CjBY/eRciP1ZXmZ4G1NUNTyihSagzbj+ZJKowXQOKpLr5qmrPHzlkcMYGMdXso0+rVyE56QIDAQAB", this);
        bp.initialize();


    }

    public void bay(View view) {
        bp.purchase(SettingsActivity.this, "15_coin");
        bp.consumePurchase("15_coin");
    }

    public void Click(View view) {
        Intent intent = new Intent(SettingsActivity.this, SettingsGeneralActivity.class);
        startActivityForResult(intent, 1);

    }

    public void about(View view) {
        Intent intent = new Intent(SettingsActivity.this, InfoActivity.class);
        startActivityForResult(intent, 1);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.theme, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        switch (item.getItemId()) {
            case R.id.action_download: {
                bp.purchase(SettingsActivity.this, "15_coin");
                bp.consumePurchase("15_coin");
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
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


