package com.music.vkm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

/**
 * Created by mascot on 12.09.2017.
 */

public class NotificationGenerator {
    public static final String NOTIFY_CLOSE = "com.mascotworld.vkaudiomanager.close";
    public static final String NOTIFY_PLAY = "com.mascotworld.vkaudiomanager.play";
    public static final String NOTIFY_NEXT = "com.mascotworld.vkaudiomanager.next";
    public static final String NOTIFY_PREV = "com.mascotworld.vkaudiomanager.prev";
    public static final String NOTIFY_SEEK = "com.mascotworld.vkaudiomanager.sendseek";
    public static Notification notification;
    static SharedPreferences sPref;
    private static final int NOTIFICATION_ID_OPEN_ACTIVITY = 9;
    public static final int NOTIFICATION_ID_CUSTOM_BIG = 9;

    static String loadText(String saved_text, Context context) {
        sPref = context.getSharedPreferences(Settings.SPreferences, Context.MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }

    public static void customBigNotification(Context context, String title, String artist, String url, String work, boolean ongoing) {
        RemoteViews expandedView;
        RemoteViews expandedView1;
        if (Build.VERSION.SDK_INT < 21) {
            expandedView = new RemoteViews(context.getPackageName(), R.layout.audio_notification_old);

            expandedView1 = new RemoteViews(context.getPackageName(), R.layout.audio_notification_small_old);

        } else {
            expandedView = new RemoteViews(context.getPackageName(), R.layout.audio_notification);

            expandedView1 = new RemoteViews(context.getPackageName(), R.layout.audio_notification_small);
        }


        final NotificationCompat.Builder nc = new NotificationCompat.Builder(context);
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notifyIntent;

        notifyIntent = new Intent(context, Audio_main_activity.class);
        notifyIntent.putExtra("open","true");

        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        nc.setContentIntent(pendingIntent);
        nc.setSmallIcon(R.drawable.ic_stat_notify_play);
        nc.setPriority(Notification.PRIORITY_MAX);
        nc.setVisibility(VISIBILITY_PUBLIC);

        nc.setOngoing(ongoing);
        nc.setAutoCancel(false);

        nc.setCustomContentView(expandedView1);
        nc.setCustomBigContentView(expandedView);

        nc.getBigContentView().setTextViewText(R.id.title_notification, wstr.normalizeString(title));
        nc.getBigContentView().setTextViewText(R.id.content, wstr.normalizeString(artist));
        nc.getContentView().setTextViewText(R.id.title_notification_small, wstr.normalizeString(title));
        nc.getContentView().setTextViewText(R.id.content_small, wstr.normalizeString(artist));


        if (!url.equals("null")) {
            Bitmap bitmap = null;

            Picasso.with(context).load(url).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    nc.getBigContentView().setImageViewBitmap(R.id.cover, bitmap);
                    nc.getContentView().setImageViewBitmap(R.id.cover_small, bitmap);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });


        } else {
            nc.getBigContentView().setImageViewResource(R.id.cover, R.drawable.aplayer_cover_placeholder);
            nc.getContentView().setImageViewResource(R.id.cover_small, R.drawable.aplayer_cover_placeholder);
        }


        if (work.equals("play")) {
            nc.getBigContentView().setImageViewResource(R.id.playpause, R.drawable.ic_pause_24dp);
            nc.getContentView().setImageViewResource(R.id.playpause_small, R.drawable.ic_pause_24dp);
            nc.setSmallIcon(R.drawable.ic_stat_notify_play);
        } else if (work.equals("pause")) {
            nc.getBigContentView().setImageViewResource(R.id.playpause, R.drawable.ic_play_24dp);
            nc.getContentView().setImageViewResource(R.id.playpause_small, R.drawable.ic_play_24dp);
            nc.setSmallIcon(R.drawable.ic_stat_notify_pause);
        }

        setListeners(expandedView, context);

        setListeners_small(expandedView1, context);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel("9", "NOTIFICATION_CHANNEL_NAME", importance);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);

            assert nm != null;
            nc.setChannelId("9");
            nm.createNotificationChannel(notificationChannel);
        }
        notification = nc.build();
        nm.notify(NOTIFICATION_ID_CUSTOM_BIG, notification);


    }

    private static void setListeners(RemoteViews view, Context context) {
        Intent close = new Intent(NOTIFY_CLOSE);
        Intent next = new Intent(NOTIFY_NEXT);
        Intent play = new Intent(NOTIFY_PLAY);
        Intent prev = new Intent(NOTIFY_PREV);


        PendingIntent pPrevious = PendingIntent.getBroadcast(context, 0, close, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.stop, pPrevious);

        PendingIntent pNext = PendingIntent.getBroadcast(context, 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.next, pNext);

        PendingIntent pPlay = PendingIntent.getBroadcast(context, 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.playpause, pPlay);

        PendingIntent pPrev = PendingIntent.getBroadcast(context, 0, prev, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.prev, pPrev);


    }

    private static void setListeners_small(RemoteViews view, Context context) {


        Intent close_small = new Intent(NOTIFY_CLOSE);
        Intent next_small = new Intent(NOTIFY_NEXT);
        Intent play_small = new Intent(NOTIFY_PLAY);
        Intent prev = new Intent(NOTIFY_PREV);


        PendingIntent pPrevious_small = PendingIntent.getBroadcast(context, 0, close_small, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.stop_small, pPrevious_small);

        PendingIntent pNext_small = PendingIntent.getBroadcast(context, 0, next_small, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.next_small, pNext_small);

        PendingIntent pPlay_small = PendingIntent.getBroadcast(context, 0, play_small, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.playpause_small, pPlay_small);

        PendingIntent pPrev = PendingIntent.getBroadcast(context, 0, prev, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.prev_small, pPrev);
    }

    public static void loadNotification() {

    }


}
