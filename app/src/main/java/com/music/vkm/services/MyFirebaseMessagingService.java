package com.music.vkm.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.music.vkm.AudioMainActivity;
import com.music.vkm.R;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage message) {
        sendMyNotification(message.getNotification().getBody());
    }


    private void sendMyNotification(String message) {

        Log.d("Notification", "Start");
        Intent notificationIntent = new Intent(getApplicationContext(), AudioMainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Resources res = getApplicationContext().getResources();

        // до версии Android 8.0 API 26
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_music_24)
                .setContentTitle(getApplicationContext().getResources().getString(R.string.notification))
                .setContentText(message)
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.logo_3))
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel("10", "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            assert nm != null;
            builder.setChannelId("10");
            nm.createNotificationChannel(notificationChannel);
        }

        nm.notify(0, builder.build());
    }
}