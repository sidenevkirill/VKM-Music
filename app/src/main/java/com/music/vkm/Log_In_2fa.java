package com.music.vkm;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by halez on 24.01.2018.
 */

public class Log_In_2fa extends AppCompatActivity {
    SharedPreferences sPref;
    private ProgressDialog progressDialog;
    String TAG = "Log_In_2fa";
    String posturl;

    String location = "null";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_2fa_activity);

        Intent intent = getIntent();
        final String cookie = intent.getStringExtra("remixauth");
        getCode(cookie);
        location = intent.getStringExtra("location");


        View.OnClickListener entercode = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendCode(cookie, posturl);
            }
        };

       final Button next = findViewById(R.id.next);
        next.setOnClickListener(entercode);

        TextInputEditText code1 = findViewById(R.id.code1);

        code1.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            Log.d("TestEnter", "1");
                            next.callOnClick();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
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


    void getCode(String cookie) {
        progressDialog = new ProgressDialog(Log_In_2fa.this, R.style.progressbart);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        Request request = new Request.Builder()
                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("referer", "https://m.vk.com/")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Cookie", cookie)
                .url("https://m.vk.com/login?act=authcheck")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                Log_In_2fa.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Log_In_2fa.this, R.style.AlertDialog);
                        builder.setTitle("Error");
                        builder.setMessage("No ethernet");
                        builder.setPositiveButton("OK", null);
                        builder.setCancelable(false);
                        builder.show();
                        progressDialog.dismiss();
                    }
                });


                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                final String responseData = response.body().string();

                if (progressDialog.isShowing()) progressDialog.dismiss();

                posturl = wstr.pars("<form method=\"post\" action=\"", responseData, "\"");


            }
        });
    }

    void sendCode(final String cookie, String posturl) {
        progressDialog = new ProgressDialog(Log_In_2fa.this, R.style.progressbart);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        TextInputEditText code1 = findViewById(R.id.code1);
        RequestBody formBody = new FormBody.Builder()
                .add("_ajax", "1")
                .add("code", code1.getText().toString())
                .add("remember", "1")
                .build();

        final Request request = new Request.Builder()

                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "*/*")
                .addHeader("Content-Type", "text/javascript; charset=utf-8")
                .addHeader("Cookie", cookie.replaceAll(" ", ""))
                .addHeader("x-requested-with", "XMLHttpRequest")
                .post(formBody)
                .url("https://m.vk.com" + posturl)


                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

                Log_In_2fa.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Log_In_2fa.this, R.style.AlertDialog);
                        builder.setTitle(getResources().getString(R.string.error));
                        builder.setMessage("No ethernet");
                        builder.setPositiveButton("OK", null);
                        builder.setCancelable(false);
                        builder.show();
                        progressDialog.dismiss();
                    }
                });

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {


                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();

                Log.d(TAG, "onResponse: " + responseHead);
//[1842,false,2,"\/login?act=authcheck_code&hash=1516823768_57e3d50b58c0a8dd72","\/captcha.php?sid=134509841382",{"captcha_sid":"134509841382","_ajax":"1","code":"801051","remember":"1"}]
                if (responseData.contains("login?role")) {
                    String tmpcookie;
                    Log.d(TAG, "onResponse: " + cookie);
                    tmpcookie = "remixttpid" + wstr.pars("remixttpid", responseHead, ".com") + ".com";
                    getToken(tmpcookie, "https://m.vk.com/login" + wstr.pars("login", responseData, "\""), "none");
                } else if (responseData.contains("login?act=authcheck_code")) {
                    Log_In_2fa.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextInputEditText code1 = findViewById(R.id.code1);
                            final String code = code1.getText().toString();
                            code1.setText("");
                            TextInputLayout codelayout = findViewById(R.id.code);
                            codelayout.setHint(getResources().getString(R.string.captcha));
                            progressDialog.dismiss();
                            Log.d(TAG, "run: " + cookie);
                            ImageView captcha = findViewById(R.id.captcha);
                            Picasso.with(getApplicationContext())
                                    .load("https://m.vk.com/captcha.php?sid=" + wstr.pars("\"captcha_sid\":\"", responseData, "\""))
                                    .into(captcha);
                            View.OnClickListener entercode = new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    TextInputEditText code1 = findViewById(R.id.code1);
                                    getContinue(cookie + " remixstid" + wstr.pars("remixstid", responseHead, ".com") + ".com", "https://m.vk.com/login" + wstr.pars("login", responseData, "\""), wstr.pars("\"captcha_sid\":\"", responseData, "\""), code, code1.getText().toString());
                                }
                            };

                            Button next = findViewById(R.id.next);
                            next.setOnClickListener(null);
                            next.setOnClickListener(entercode);


                        }
                    });
                } else {
                    Log_In_2fa.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Log_In_2fa.this, R.style.AlertDialog);
                            builder.setTitle(getResources().getString(R.string.error));
                            builder.setMessage("Bad code");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Log_In_2fa.this, Log_In.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                            progressDialog.dismiss();
                        }
                    });
                }


            }
        });
    }

    void getContinue(final String cookie, final String url, final String captcha, final String code, final String codecaptcha) {
        progressDialog = new ProgressDialog(Log_In_2fa.this, R.style.progressbart);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();


        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("_ajax", "1")
                .add("code", code)
                .add("remember", "1")
                .build();

        final Request request = new Request.Builder()

                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "*/*")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("x-requested-with", "XMLHttpRequest")
                .addHeader("Cookie", cookie)
                .post(formBody)
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

                Log_In_2fa.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Log_In_2fa.this, R.style.AlertDialog);
                        builder.setTitle(getResources().getString(R.string.error));
                        builder.setMessage("No ethernet");
                        builder.setPositiveButton("OK", null);
                        builder.setCancelable(false);
                        builder.show();
                        progressDialog.dismiss();
                    }
                });

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {


                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();

                Log.d(TAG, "onResponse: 4" + responseHead);
                Log.d(TAG, "onResponse: 4" + responseData);

                getTokenWithCaptcha(cookie, url + "&code=" + code, captcha, code, codecaptcha);

            }
        });


    }

    void getTokenWithCaptcha(final String cookie, final String url, String captcha, String code, String codecaptcha) {
//&code=068373


        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("captcha_sid", captcha)
                .add("captcha_key", codecaptcha)
                .build();

        final Request request = new Request.Builder()

                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("upgrade-insecure-requests", "1")
                .addHeader("origin", "https://m.vk.com")
                .addHeader("referer", url)
                .addHeader("Cookie", cookie)
                .post(formBody)
                .url(url)


                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

                Log_In_2fa.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Log_In_2fa.this, R.style.AlertDialog);
                        builder.setTitle(getResources().getString(R.string.error));
                        builder.setMessage("No ethernet");
                        builder.setPositiveButton("OK", null);
                        builder.setCancelable(false);
                        builder.show();
                        progressDialog.dismiss();
                    }
                });

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {


                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();

                Log.d(TAG, "onResponse: 3" + responseHead);
                Log.d(TAG, "onResponse: 3" + responseData);

                if (responseHead.contains("/login?act=authcheck&m=442")) {
                    Log_In_2fa.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Log_In_2fa.this, R.style.AlertDialog);
                            builder.setTitle(getResources().getString(R.string.error));
                            builder.setMessage("Bad captcha");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Log_In_2fa.this, Log_In.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                            progressDialog.dismiss();
                        }
                    });
                } else if (responseHead.contains("login?role")) {

                    String cooks = request.header("cookie");
                    cooks = cooks.substring(0, cooks.indexOf("remixauthcheck")) + wstr.pars(";", wstr.pars("remixauthcheck", cooks, ".com") + ".com", ".com") + ".com";
                    Log.d(TAG, "onResponse: 3123" + cooks);
                    getToken(cooks, "https://m.vk.com/login" + response.header("location"), url);

                }

            }
        });
    }


    void getToken(String cookie, String url, String urlfrom) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        if (urlfrom.equals("none")) {
            urlfrom = "https://m.vk.com";
        }

        Request request = new Request.Builder()
                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("referer", urlfrom)
                .addHeader("upgrade-insecure-requests", "1")
                .addHeader("Cookie", cookie + ";remixmdevice=1366/768/1/!!-!!!!;")
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                Log_In_2fa.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Log_In_2fa.this, R.style.AlertDialog);
                        builder.setTitle(getResources().getString(R.string.error));
                        builder.setMessage("No ethernet");
                        builder.setPositiveButton("OK", null);
                        builder.setCancelable(false);
                        builder.show();
                        progressDialog.dismiss();
                    }
                });


                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();


                if (progressDialog.isShowing()) progressDialog.dismiss();

                if (responseHead.contains("remixsid")) {
                    saveText("sid", "remixsid=" + wstr.pars("remixsid=", responseHead, ".com") + ".com");
                    Intent intent = new Intent(Log_In_2fa.this, Audio_main_activity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log_In_2fa.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Log_In_2fa.this, R.style.AlertDialog);
                            builder.setTitle(getResources().getString(R.string.error));
                            builder.setMessage("Bad login");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Log_In_2fa.this, Log_In.class);
                                    startActivity(intent);
                                    finish();

                                }
                            });
                            builder.setCancelable(false);
                            builder.show();
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        });
    }

}
