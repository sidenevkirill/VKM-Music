package com.music.vkm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by halez on 31.01.2018.
 */

public class NotificationService extends BroadcastReceiver {
    private static final int NOTIFY_ID = 10;
    private static final String CHANNEL_ID = "com.mascotworld.vkaudiomanager.notify";

    @Override
    public void onReceive(Context context, Intent intent) {

        String week = loadText("LAST_LAUNCH_WEEK", context);
        if (week.equals("")) {
            week = "0";
            saveText("watch_ad", "true", context);
            saveText("LAST_LAUNCH_WEEK", new SimpleDateFormat("dd", Locale.US).format(new Date()), context);
        }
        String now = new SimpleDateFormat("dd", Locale.US).format(new Date());


        int weekch = Integer.parseInt(week);
        int nowch = Integer.parseInt(now);

        if ((weekch - nowch) < -6) {
            saveText("watch_ad", "true", context);
            saveText("LAST_LAUNCH_WEEK", new SimpleDateFormat("dd", Locale.US).format(new Date()), context);
        }


        if (loadText("LAST_LAUNCH_DATE", context).equals(new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(new Date()))) {
            if (loadText("advertisiment", context).equals("true"))
                createNotification(context);
            Log.d("TestFirstLaunch", "it's not a first");
        } else {
            if (loadText("watch_ad", context).equals("true")) {
                if (loadText("advertisiment", context).equals("true")) {
                    if (loadText("first_open", context).equals("false")) {
                        Toast.makeText(context, "В настройках вы можете выключить просмотр рекламы раз в день.", Toast.LENGTH_LONG).show();
                        Intent intent1 = new Intent(context, AdActivity.class);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent1);
                    }
                }
            }


            Log.d("TestFirstLaunch", "it's first");
            saveText("LAST_LAUNCH_DATE", new SimpleDateFormat("yyyy/MM/dd", Locale.US).format(new Date()), context);
        }


    }


    void createNotification(Context context) {
        Log.d("Notification", "Start");
        Intent notificationIntent = new Intent(context, AdActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Resources res = context.getResources();

        // до версии Android 8.0 API 26
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentIntent(contentIntent)
                // обязательные настройки
                .setSmallIcon(R.drawable.ic_music_24)
                //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle(context.getResources().getString(R.string.notification_ad))
                //.setContentText(res.getString(R.string.notifytext))
                .setContentText(context.getResources().getString(R.string.notification_info)) // Текст уведомления
                // необязательные настройки
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.logos)) // большая
                // картинка
                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setAutoCancel(true); // автоматически закрыть уведомление после нажатия

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel("10", "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            assert nm != null;
            builder.setChannelId("10");
            nm.createNotificationChannel(notificationChannel);
        }

        nm.notify(NOTIFY_ID, builder.build());
    }


    private String loadText(String saved_text, Context context) {
        SharedPreferences sPref = context.getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }

    void saveText(String saved_text, String save, Context context) {
        SharedPreferences sPref = context.getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(saved_text, save);
        ed.apply();
    }
}
