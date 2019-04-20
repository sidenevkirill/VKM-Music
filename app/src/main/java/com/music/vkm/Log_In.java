package com.music.vkm;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;


import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Log_In extends AppCompatActivity {
    private String posturl = null;
    public cookie Cookie;
    SharedPreferences sPref;
    private ProgressDialog progressDialog;
    public static final int PERMISSION_REQUEST_CODE = 123;
    boolean consumed = false;
    final String SAVED_TEXT = "sid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_acitivty);

        if (loadText("Use").equals("false") || loadText("Use").equals("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Log_In.this, R.style.AlertDialog);
            builder.setCancelable(false);
            builder.setTitle(getResources().getString(R.string.warning));
            builder.setMessage(getResources().getString(R.string.warninginfo));
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    saveText("Use", "true");
                }
            });
            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    saveText("Use", "false");
                    finish();
                }
            });
            builder.show();

        }
        if (!hasPermissions()) {
            requestPerms();
        } else {
            LogIn();
        }


    }

    void LogIn() {


        if (loadText().equals("")) {

            final CheckBox chbox_s = (CheckBox) findViewById(R.id.chbox_s);

            final TextInputEditText emailed = (TextInputEditText) findViewById(R.id.phonemail1);
            final TextInputEditText passed = (TextInputEditText) findViewById(R.id.password1);

            if (loadText("savedata").equals("true")) {
                emailed.setText(loadText("login"));
                passed.setText(loadText("pass"));
                chbox_s.setActivated(true);
            }


            if (loadText("design").equals("")) {
                saveText("design", "new");
            }


            View.OnClickListener chbox_s1 = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (chbox_s.isChecked()) {
                        saveText("savedata", "true");
                    } else {
                        saveText("savedata", "false");
                    }
                }
            };

            chbox_s.setOnClickListener(chbox_s1);

            Cookie = new cookie();

            final Button login = (Button) findViewById(R.id.login);

            View.OnClickListener logIn = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    if (chbox_s.isChecked()) {
                        saveText("savedata", "true");
                    } else {
                        saveText("savedata", "false");
                    }
                    getPostUrl();
                }
            };

            login.setOnClickListener(logIn);


            passed.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_ENTER:
                                Log.d("TestEnter", "1");
                                login.callOnClick();
                                return true;
                            default:
                                break;
                        }
                    }
                    return false;
                }
            });


        } else {

            Intent newIntent = new Intent(Log_In.this, Audio_main_activity.class);
            startActivity(newIntent);
            finish();

        }

    }

    void saveText(String save) {
        sPref = getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_TEXT, save);
        ed.commit();
    }

    void saveText(String saved_text, String save) {
        sPref = getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(saved_text, save);
        ed.commit();
    }

    String loadText() {
        sPref = getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(SAVED_TEXT, "");
        return savedText;
    }

    String loadText(String saved_text) {
        sPref = getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }

    void getcookies1(Response response) {
        String cookie = response.headers().toString();
        String tmp1, tmp2, tmp3;

        tmp1 = "remixlhk=" + wstr.pars("remixlhk=", cookie, ".com") + ".com";
        // tmp2 = "remixstid=" + wstr.pars("remixstid=", cookie, ".com") + ".com";
        tmp3 = "remixlang" + wstr.pars("remixlang", cookie, ".com") + ".com";

        Log.d("AuthCookie", cookie);
        Log.d("AuthCookie", "1" + tmp1);
        //Log.d("AuthCookie","2"+tmp2);
        Log.d("AuthCookie", "3" + tmp3);


        Cookie.addCookie(tmp1);
        //  Cookie.addCookie(tmp2);
        Cookie.addCookie(tmp3);

    }

    void getcookies2(Response response) {
        String cookie = response.headers().toString();
        String tmp1, tmp2, tmp3;


        Log.d("AuthCookie", cookie);
        if (response.header("location").contains("_q_hash=")) {
            String l = ("l=" + wstr.pars("l=", cookie, ".com") + ".com").replace(" ", "");
            String p = ("p=" + wstr.pars("p=", cookie, ".com") + ".com").replace(" ", "");

            Cookie.setCookie(1, l);
            Cookie.setCookie(2, p);
            saveText("l", l);
            saveText("p", p);
            Cookie.setCookie(3, ("remixq" + wstr.pars("remixq", cookie, ".com") + ".com").replace(" ", ""));
        }


    }

    void getPostUrl() {
        Log.d("TestEnter", "2");
        progressDialog = null;
        progressDialog = new ProgressDialog(Log_In.this, R.style.progressbart);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.logining));
        progressDialog.setCancelable(false);
        progressDialog.show();
        Cookie = new cookie();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .header("User-Agent", loadText("userAgent"))
                .url("https://m.vk.com")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {

                Log_In.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Log_In.this, R.style.AlertDialog);
                        builder.setTitle("Error");
                        builder.setMessage("No connection with m.vk.com");
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

                //Log.d("TestAuth", response.headers().toString());


                posturl = wstr.pars("<form method=\"post\" action=\"", responseData, "\"");
                getcookies1(response);
                LogIn(posturl);
            }
        });


    }

    void LogIn(String posturl) {
        TextInputEditText emailed = (TextInputEditText) findViewById(R.id.phonemail1);
        TextInputEditText passed = (TextInputEditText) findViewById(R.id.password1);
        final String email = emailed.getText().toString();
        final String pass = passed.getText().toString();

        if (loadText("savedata").equals("true")) {
            saveText("login", email);
            saveText("pass", pass);
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("email", email)
                .add("pass", pass)
                .build();

//String tmp = Cookie.getCookieinLine().replaceAll(" ", "");
        //tmp = tmp.substring(0,97);
        final Request request = new Request.Builder()

                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Origin", "https://m.vk.com/login")
                .addHeader("Cookie", Cookie.getCookieinLine().replaceAll(" ", ""))
                .post(formBody)
                .url(posturl)


                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

                Log_In.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Log_In.this, R.style.AlertDialog);
                        builder.setTitle(getResources().getString(R.string.error));
                        builder.setMessage("No connection with m.vk.com");
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


                Log.d("TestAuth", responseHead);

                getcookies2(response);

                Cookie.addCookie("remixq_" + wstr.pars("remixq_", responseHead, ";"));
                getremixsid(response.header("location"));

            }
        });
    }

    void getremixsid(final String location) {
        Log.d("TestAuth", location);
        if (location.contains("nullne")) {
            Log_In.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Log_In.this, R.style.AlertDialog);
                    builder.setTitle(getResources().getString(R.string.error));
                    builder.setMessage(getResources().getString(R.string.error));
                    builder.setPositiveButton("OK", null);
                    builder.setCancelable(false);
                    builder.show();
                    progressDialog.dismiss();
                }
            });
        } else if (location.contains("_q_hash")) {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .followRedirects(false)
                    .followSslRedirects(false)
                    .build();

            final Request request = new Request.Builder()

                    .addHeader("User-Agent", loadText("userAgent"))
                    .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("referer", "https://m.vk.com/")
                    .addHeader("Upgrade-Insecure-Requests", "1")
                    .addHeader("Cookie", Cookie.getCookieinLine())
                    .url(location)
                    .build();

            client.newCall(request).enqueue(new Callback() {


                @Override
                public void onFailure(Call call, IOException e) {
                    Log_In.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Log_In.this, R.style.AlertDialog);
                            builder.setTitle("Error");
                            builder.setMessage("No connection with m.vk.com");
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


                    if (responseData.contains("/captcha.php")) {
                        String captcha = "captcha";
                        Log.d("testauth", captcha);
                    }


                    Log_In.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("testauth", Cookie.getCookieinLine());
                            if (progressDialog.isShowing()) progressDialog.dismiss();
                            switch (response.header("location")) {
                                case "/login?act=authcheck": {

                                    Cookie.addCookie("remixauthcheck=" + wstr.pars("remixauthcheck=", responseHead, ";"));
                                    Intent newIntent = new Intent(Log_In.this, Log_In_2fa.class);
                                    newIntent.putExtra("remixauth", Cookie.getCookieinLine());
                                    newIntent.putExtra("location", location);
                                    startActivity(newIntent);
                                    finish();


                                    break;
                                }
                                case "/": {
                                    Intent newIntent = new Intent(Log_In.this, Audio_main_activity.class);
                                    String tmp;
                                    tmp = "remixsid=" + wstr.pars("remixsid=", responseHead, ".com") + ".com";
                                    saveText(tmp);
                                    newIntent.putExtra("remixsid", tmp);

                                    startActivity(newIntent);
                                    finish();
                                    break;
                                }
                                default:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Log_In.this, R.style.AlertDialog);
                                    builder.setTitle(getResources().getString(R.string.badlogin));
                                    builder.setCancelable(false);
                                    builder.setMessage(getResources().getString(R.string.badpassword) + "or bad ip address");
                                    builder.setPositiveButton("OK", null);
                                    builder.show();
                                    break;
                            }
                        }
                    });


                }
            });
        } else if (location.contains("https://m.vk.com/login?role=fast&to=&s=1&m=1&email=")) {
            Log_In.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Log_In.this, R.style.AlertDialog);
                    builder.setTitle(getResources().getString(R.string.badlogin));
                    builder.setCancelable(false);
                    builder.setMessage(getResources().getString(R.string.badpassword));
                    builder.setPositiveButton("OK", null);
                    builder.show();
                    progressDialog.dismiss();
                }
            });
        } else {
            Log_In.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Log_In.this, R.style.AlertDialog);
                    builder.setTitle("Error");
                    builder.setMessage("No connection with m.vk.com");
                    builder.setPositiveButton("OK", null);
                    builder.setCancelable(false);
                    builder.show();
                    progressDialog.dismiss();
                }
            });
        }

    }

    private boolean hasPermissions() {
        //string array of permissions,
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            if (!(checkCallingOrSelfPermission(perms) == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean allowed = true;

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:

                for (int res : grantResults) {
                    // if user granted all permissions.
                    allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
                }

                break;
            default:
                // if user not granted permissions.
                allowed = false;
                break;
        }

        if (allowed) {
            LogIn();
        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Log_In.this, R.style.AlertDialog);
                builder.setCancelable(false);
                builder.setTitle(getResources().getString(R.string.needperm));
                builder.setMessage(getResources().getString(R.string.needpermfull));
                builder.setPositiveButton(getResources().getString(R.string.give), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPerms();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.finish), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
                builder.show();
            }
        }

    }


}

