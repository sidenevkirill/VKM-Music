package com.music.vkm;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.bluzwong.swipeback.SwipeBackActivityHelper;

import net.rdrei.android.dirchooser.DirectoryChooserActivity;
import net.rdrei.android.dirchooser.DirectoryChooserConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mascot on 04.10.2017.
 */

public class SettingsGeneralActivity extends AppCompatActivity {

    public static String SPreferences = "SettingsGeneralActivity";
    SwipeBackActivityHelper helper;
    SharedPreferences sPref;

    final static int REQUEST_DIRECTORY_CACHE = 1;
    final static int REQUEST_DIRECTORY_FULL = 2;

    AlertDialog mainalert;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        helper = new SwipeBackActivityHelper();
        helper.setEdgeMode(true)
                .setParallaxMode(true)
                .setParallaxRatio(3)
                .setNeedBackgroundShadow(true)
                .init(this);
        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.settings));
        toolbar.setTitleTextColor(Color.parseColor("#000000"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.finish();
            }
        });


        final CheckBox chbox2 = findViewById(R.id.chbox2);
        if (loadText("design").equals("new")) {
            chbox2.setChecked(false);
        } else {
            chbox2.setChecked(true);
        }

        View.OnClickListener chbox2click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (loadText("design").equals("old")) {
                    saveText("design", "new");
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsGeneralActivity.this, R.style.AlertDialog);
                    builder.setCancelable(false);
                    builder.setTitle(getResources().getString(R.string.warning));
                    builder.setMessage(getResources().getString(R.string.notstable));
                    builder.setPositiveButton("OK", null);
                    builder.show();
                    saveText("design", "old");
                }

            }
        };

        chbox2.setOnClickListener(chbox2click);


        final CheckBox chbox1 = findViewById(R.id.chbox1);
        String smAll = loadText("smAll");
        if (smAll.equals("true")) {
            chbox1.setChecked(true);
        } else {
            chbox1.setChecked(false);
        }

        View.OnClickListener chbox1click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadText("smAll").equals("true")) {
                    saveText("smAll", "false");
                } else {
                    Toast.makeText(SettingsGeneralActivity.this, getResources().getString(R.string.smAllEx), Toast.LENGTH_LONG).show();
                    saveText("smAll", "true");
                }

            }
        };

        chbox1.setOnClickListener(chbox1click);


        final CheckBox chbox3 = findViewById(R.id.chbox3);
        if (loadText("savedata").equals("true")) {
            chbox3.setChecked(true);
        } else {
            chbox3.setChecked(false);
        }

        View.OnClickListener chbox3click = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadText("savedata").equals("true")) {
                    saveText("savedata", "false");
                } else {
                    saveText("savedata", "true");
                }

            }
        };


        chbox3.setOnClickListener(chbox3click);


        View.OnClickListener chooseUserAgent = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsGeneralActivity.this, R.style.AlertDialog);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.chooseUserAgentAttention));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String[] UserAgents = {"Android", "Apple", "Windows", "Macintosh"};


                        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsGeneralActivity.this, R.style.AlertDialog);
                        builder.setCancelable(false);
                        builder.setTitle(getResources().getString(R.string.chooseUserAgent));
                        builder.setItems(UserAgents, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                switch (item) {
                                    case 0: {
                                        saveText("userAgent", getString(R.string.useragentAndroid));
                                        Toast.makeText(getApplicationContext(), getString(R.string.selected) + "Android", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    case 1: {
                                        saveText("userAgent", getString(R.string.useragentApple));
                                        Toast.makeText(getApplicationContext(), getString(R.string.selected) + "Apple", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    case 2: {
                                        saveText("userAgent", getString(R.string.useragentWindows));
                                        Toast.makeText(getApplicationContext(), getString(R.string.selected) + "Windows", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                    case 3: {
                                        saveText("userAgent", getString(R.string.useragentMacintosh));
                                        Toast.makeText(getApplicationContext(), getString(R.string.selected) + "Macintosh", Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                            }
                        });
                        builder.setPositiveButton(getString(R.string.cancel), null);
                        builder.show();

                    }
                });
                builder.show();


            }
        };

        RelativeLayout rv8 = findViewById(R.id.rv8);
        rv8.setOnClickListener(chooseUserAgent);


        Button full = findViewById(R.id.full_music);
        full.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view = View.inflate(SettingsGeneralActivity.this, R.layout.setpath_dialog, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsGeneralActivity.this, R.style.AlertDialog);

                Button changepath = view.findViewById(R.id.changepath);
                changepath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent chooserIntent = new Intent(getApplicationContext(), DirectoryChooserActivity.class);

                        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                                .newDirectoryName("DirChooserSample")
                                .allowReadOnlyDirectory(true)
                                .allowNewDirectoryNameModification(true)
                                .build();

                        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);


                        startActivityForResult(chooserIntent, REQUEST_DIRECTORY_FULL);
                    }
                });

                final EditText path = view.findViewById(R.id.path);
                path.setText(loadText("pathFull"));


                builder.setView(view);

                builder.setTitle(getResources().getString(R.string.pathfull));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String test = path.getText().toString();
                        if (test.lastIndexOf("/") == test.length() - 1) {

                        } else {
                            test = test + "/";
                        }
                        saveText("pathFull", test);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), null);
                builder.setNeutralButton(getResources().getString(R.string.setdefault), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/Music/");
                        Log.d("TestSavingMusic", "pathFull: " + destinationUri.getEncodedPath());
                        saveText("pathFull", destinationUri.getEncodedPath());

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                mainalert = alert;


            }
        });

        Button cache = findViewById(R.id.cache_music);
        cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                View view = View.inflate(SettingsGeneralActivity.this, R.layout.setpath_dialog, null);
                final AlertDialog.Builder builder = new AlertDialog.Builder(SettingsGeneralActivity.this, R.style.AlertDialog);

                Button changepath = view.findViewById(R.id.changepath);
                changepath.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent chooserIntent = new Intent(getApplicationContext(), DirectoryChooserActivity.class);

                        final DirectoryChooserConfig config = DirectoryChooserConfig.builder()
                                .newDirectoryName("DirChooserSample")
                                .allowReadOnlyDirectory(true)
                                .allowNewDirectoryNameModification(true)
                                .build();

                        chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_CONFIG, config);


                        startActivityForResult(chooserIntent, REQUEST_DIRECTORY_CACHE);
                    }
                });

                final EditText path = view.findViewById(R.id.path);
                path.setText(loadText("pathCache"));


                builder.setView(view);

                builder.setTitle(getResources().getString(R.string.pathfull));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String test = path.getText().toString();
                        if (test.lastIndexOf("/") == test.length() - 1) {

                        } else {
                            test = test + "/";
                        }
                        saveText("pathCache", test);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), null);
                builder.setNeutralButton(getResources().getString(R.string.setdefault), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + AudioMainActivity.path);
                        Log.d("TestSavingMusic", "PathCache: " + destinationUri.getEncodedPath());
                        saveText("pathCache", destinationUri.getEncodedPath());

                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
                mainalert = alert;


            }
        });


        RelativeLayout deleteCache = findViewById(R.id.rv15);

        deleteCache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsGeneralActivity.this, R.style.AlertDialog);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.help));
                builder.setMessage(getString(R.string.deleteCacheInfo));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File output = new File(loadText("pathCache"));
                        for (File file : output.listFiles())
                            if (!file.isDirectory())
                                file.delete();

                        Toast.makeText(getApplicationContext(), getString(R.string.done), Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), null);
                builder.show();
            }
        });


        Button load_old = findViewById(R.id.audio_load);

        load_old.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               /* final Uri destinationUri = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.mascotworld.audiomanager/files/Music/");
                final Uri destinationUriOld = Uri.parse(Environment.getExternalStorageDirectory().toString() + "/Android/data/com.mascotworld.vkaudiomanager/files/Music/");
                if (getListMusic(destinationUri.getEncodedPath()).size() > 0 || getListMusic(destinationUriOld.getEncodedPath()).size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsGeneralActivity.this, R.style.AlertDialog);
                    builder.setCancelable(false);
                    builder.setTitle("Уведомление");
                    builder.setMessage("У вас есть сохраненная музыка из старой версии приложения. Перенести её? Будет перенесена музыка только из стандартного пути сохранения кэша.");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Uri output = Uri.parse(Environment.getExternalStorageDirectory().toString() + AudioMainActivity.path);
                            File filesCache = new File(output.getEncodedPath());
                            if (!filesCache.exists()) {
                                Log.d("TestSave", "Directory not exist");
                                filesCache.mkdirs();
                            } else {
                                Log.d("TestSave", "Directory exist");
                            }
                            File destFolder = new File(output.getEncodedPath());
                            if (getListMusic(destinationUri.getEncodedPath()).size() > 0) {
                                // это папка, в которую будем перемещать
                                File f1 = new File(destinationUri.getEncodedPath());
                                File[] files = f1.listFiles(); // получаем непосредственно файлы, не просто имена
                                for (File file : files) {
                                    file.renameTo(new File(destFolder, file.getName()));
                                }
                            }

                            if (getListMusic(destinationUriOld.getEncodedPath()).size() > 0) {
                                File f2 = new File(destinationUriOld.getEncodedPath());
                                File[] files1 = f2.listFiles(); // получаем непосредственно файлы, не просто имена
                                for (File file : files1) {
                                    file.renameTo(new File(destFolder, file.getName()));
                                }
                            }


                        }
                    });
                    builder.setNegativeButton("Отказаться", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    builder.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsGeneralActivity.this, R.style.AlertDialog);
                    builder.setCancelable(false);
                    builder.setTitle("Уведомление");
                    builder.setMessage("Музыки старых версий нет.");
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }

*/


                Toast.makeText(getApplicationContext(), getString(R.string.unavailable), Toast.LENGTH_SHORT).show();
            }
        });


        CheckBox watch_ad = findViewById(R.id.watch_ad);

        if (loadText("watch_ad").equals("true")) {
            watch_ad.setChecked(true);
        } else {
            watch_ad.setChecked(false);
        }

        watch_ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loadText("watch_ad").equals("true")) {
                    saveText("watch_ad", "false");
                } else {
                    saveText("watch_ad", "true");
                }

            }
        });

    }

    public void onSlide(View view) {
        Intent intent = new Intent(new Intent(Intent.ACTION_VIEW, Uri.parse("http://vk.com/wtfmu")));
        startActivityForResult(intent, 1);
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

    @Override
    public void onBackPressed() {
        helper.finish();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_DIRECTORY_CACHE: {
                if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {

                    Toast.makeText(getApplicationContext(), "Директория выбранна", Toast.LENGTH_SHORT).show();
                    String test = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                    if (test.lastIndexOf("/") == test.length() - 1) {

                    } else {
                        test = test + "/";
                    }
                    saveText("pathCache", test);

                } else {
                    Toast.makeText(getApplicationContext(), "Директория не выбранна", Toast.LENGTH_SHORT).show();
                }
                mainalert.cancel();
                break;
            }
            case REQUEST_DIRECTORY_FULL: {
                if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED) {

                    Toast.makeText(getApplicationContext(), "Директория выбранна", Toast.LENGTH_SHORT).show();

                    String test = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);
                    if (test.lastIndexOf("/") == test.length() - 1) {

                    } else {
                        test = test + "/";
                    }
                    saveText("pathFull", test);

                } else {
                    Toast.makeText(getApplicationContext(), "Директория не выбранна", Toast.LENGTH_SHORT).show();
                }
                mainalert.cancel();
                break;

            }
        }

    }


    public void Click(View view) {
        Intent intent = new Intent(SettingsGeneralActivity.this, AudioMainActivity.class);
        startActivityForResult(intent, 1);
    }
}



