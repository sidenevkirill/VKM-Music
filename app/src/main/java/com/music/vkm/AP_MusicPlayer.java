package com.music.vkm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import static com.music.vkm.Audio_main_activity.mBoundService;
import static com.music.vkm.Audio_main_activity.searchMenuItem;
import static com.music.vkm.Audio_main_activity.searchView;
import static com.music.vkm.Log_In.PERMISSION_REQUEST_CODE;

/**
 * Created by mascot on 04.10.2017.
 */


public class AP_MusicPlayer extends Fragment {

    BroadcastReceiver getInfo;
    Handler handler = new Handler();
    Music tmpmusic;
    ProgressBar audio_panel_progress;

    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
            if (MusicService.mediaPlayer != null) {
                setSeek(MusicService.mediaPlayer.getCurrentPosition(), MusicService.secondary);
            }
            handler.postDelayed(this, 1000);
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_audio_player, container, false);
        audio_panel_progress = getActivity().findViewById(R.id.audio_panel_progress);
        return v;

    }

    public static AP_MusicPlayer newInstance(String text) {

        AP_MusicPlayer f = new AP_MusicPlayer();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }


    @Override
    public void onStart() {
        super.onStart();
        setListeners();
        getInfo(getView());

        if (MusicService.mediaPlayer != null) {
            update();

            TextView aplayer_title = getActivity().findViewById(R.id.new_title);
            TextView aplayer_artist = getActivity().findViewById(R.id.new_artist);

            aplayer_title.setText(wstr.normalizeString(MusicService.getSchedule().getCurrentMusic().getTitle()));
            aplayer_artist.setText(wstr.normalizeString(MusicService.getSchedule().getCurrentMusic().getArtist()));

        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(getInfo);
        handler.removeCallbacks(serviceRunnable);
    }

    private void setListeners() {

        if (getActivity() != null) {


            ImageButton start = getActivity().findViewById(R.id.new_play_pause);

            View.OnClickListener ActionStart = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MusicPlayPause();

                }
            };

            start.setOnClickListener(ActionStart);

            ImageButton prev = getActivity().findViewById(R.id.new_prev);

            View.OnClickListener ActionPrev = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MusicPrev();

                }
            };

            prev.setOnClickListener(ActionPrev);


            ImageButton next = getActivity().findViewById(R.id.new_next);


            View.OnClickListener ActionNext = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MusicNext();

                }
            };

            next.setOnClickListener(ActionNext);


            final SeekBar aplayer_progress = getActivity().findViewById(R.id.new_seekbar);
            final TextView aplayer_time = getActivity().findViewById(R.id.new_time);

            aplayer_progress.setProgress(0);
            aplayer_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    aplayer_time.setText(wstr.millisecondsToTime(progress));
                    if (fromUser) {
                        MusicSeek(progress);
                    }

                }


            });

            ImageButton new_settings = getActivity().findViewById(R.id.new_settings);

            final PopupMenu popup = new PopupMenu(getActivity(), new_settings);
            popup.getMenuInflater().inflate(R.menu.audio, popup.getMenu());
            popup.getMenu().getItem(4).setVisible(false);


            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getTitle().toString().equals(getResources().getString(R.string.save))) {
                        if (hasPermissions()) {
                            mBoundService.cacheMusic(MusicService.getSchedule().getCurrentMusic());
                        } else {
                            Toast.makeText(getContext(), "Для сохранения, нужно разрешение на чтение и запись в память.", Toast.LENGTH_SHORT).show();
                            tmpmusic = MusicService.getSchedule().getCurrentMusic();
                            requestPerms();
                        }
                        return true;
                    }
                    if (item.getTitle().toString().equals(getResources().getString(R.string.delete))) {
                        item.setTitle(getResources().getString(R.string.add));
                        mBoundService.addMusic(MusicService.getSchedule().getCurrentMusic());
                        return true;
                    }
                    if (item.getTitle().toString().equals(getResources().getString(R.string.add))) {
                        item.setTitle(getResources().getString(R.string.delete));
                        mBoundService.addMusic(MusicService.getSchedule().getCurrentMusic());
                        return true;
                    }
                    if (item.getTitle().toString().equals(getResources().getString(R.string.findartist))) {


                        searchMenuItem.expandActionView();
                        searchView.setQuery(MusicService.getSchedule().getCurrentMusic().getArtist(), true);
                        Audio_main_activity.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                        return true;

                    }
                    if (item.getTitle().toString().equals(getResources().getString(R.string.downloadmusic))) {
                        mBoundService.saveMusicFull(MusicService.getSchedule().getCurrentMusic());
                    }
                    return true;
                }
            });


            View.OnClickListener settings = new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mBoundService.findSavedMusic(MusicService.getSchedule().getCurrentMusic())) {
                        popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);
                    } else {
                        popup.getMenu().getItem(0).setTitle(R.string.saveToCache);
                    }


                    if (MusicService.getSchedule().getCurrentMusic().getYour().equals("true")) {
                        popup.getMenu().getItem(2).setTitle(getResources().getString(R.string.delete));
                    } else {
                        popup.getMenu().getItem(2).setTitle(getResources().getString(R.string.add));
                    }
                    popup.show();
                }
            };

            new_settings.setOnClickListener(settings);


            final ImageButton new_add = getActivity().findViewById(R.id.new_add);


            View.OnClickListener addSong = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mBoundService.addMusic(MusicService.getSchedule().getCurrentMusic());
                }
            };

            new_add.setOnClickListener(addSong);

        }
    }

    private boolean hasPermissions() {
        //string array of permissions,
        String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};

        for (String perms : permissions) {
            if (!(getContext().checkCallingOrSelfPermission(perms) == PackageManager.PERMISSION_GRANTED)) {
                return false;
            }
        }
        return true;
    }

    private void requestPerms() {
        String[] permissions = new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
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
            mBoundService.cacheMusic(tmpmusic);
        } else {
            // we will give warning to user that they haven't granted permissions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialog);
                builder.setCancelable(false);
                builder.setTitle(getResources().getString(R.string.needperm));
                builder.setMessage("Разрешение необходимо для сохранения музыки");
                builder.setPositiveButton(getResources().getString(R.string.give), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPerms();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.finish), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(getContext(), "Аудиозапись не будет сохранена.", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.show();
            }
        }

    }

    void setLoading(final boolean work) {
        if (getActivity() != null) {

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final SeekBar aplayer_progress = getActivity().findViewById(R.id.new_seekbar);

                    if (work) {
                        aplayer_progress.setThumb(getResources().getDrawable(android.R.drawable.screen_background_light_transparent));
                    } else {
                        aplayer_progress.setThumb(getResources().getDrawable(R.drawable.ic_music_progress_thumb_24dp));
                    }

                    aplayer_progress.setIndeterminate(work);
                }
            });
        }

    }

    private void getInfo(final View view) {
        getInfo = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.mascotworld.vkaudiomanager.send")) {
                    if (getActivity() != null) {
                        if (MusicService.isBuffering) {
                            setLoading(true);
                        } else {
                            setLoading(false);
                        }
                        final SeekBar aplayer_progress = view.findViewById(R.id.new_seekbar);
                        TextView aplayer_title = view.findViewById(R.id.new_title);
                        TextView aplayer_artist = view.findViewById(R.id.new_artist);
                        TextView aplayer_duration = view.findViewById(R.id.new_duration);


                        final ImageButton start = view.findViewById(R.id.new_play_pause);

                        Integer time = Integer.parseInt(intent.getStringExtra("duration"));
                        aplayer_duration.setText(wstr.millisecondsToTime(time));
                        aplayer_title.setText(wstr.normalizeString(intent.getStringExtra("title")));
                        aplayer_artist.setText(wstr.normalizeString(intent.getStringExtra("artist")));
                        aplayer_progress.setMax(Integer.parseInt(intent.getStringExtra("duration")));


                        if (MusicService.isPlaying) {
                            start.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_48dp));

                        } else {
                            start.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_48dp));
                        }


                        String pic = intent.getStringExtra("pic");

                        ImageView albumart = getActivity().findViewById(R.id.new_albumart);
                        if (!pic.equals("none") && !pic.equals("null")) {
                            Picasso.with(getActivity())
                                    .load(pic)
                                    .placeholder(R.drawable.ic_song_placeholder_96)
                                    .error(R.drawable.ic_song_placeholder_96)
                                    .into(albumart);
                            albumart.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            albumart.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            albumart.setImageResource(R.drawable.ic_song_placeholder_96);
                        }

                        ImageButton aplayer_add = getActivity().findViewById(R.id.new_add);


                        if (intent.getStringExtra("your").equals("true")) {
                            aplayer_add.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_24dp));
                            aplayer_add.setColorFilter(getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.MULTIPLY);
                        } else {
                            aplayer_add.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_24dp));
                            aplayer_add.setColorFilter(getResources().getColor(R.color.colorGray), android.graphics.PorterDuff.Mode.MULTIPLY);
                        }


                        if (MusicService.mediaPlayer != null) {
                            serviceRunnable.run();
                        }


                    }
                }
            }
        };

        IntentFilter intFilt3 = new IntentFilter("com.mascotworld.vkaudiomanager.send");

        if (getActivity() != null)
            getActivity().registerReceiver(getInfo, intFilt3);
    }

    void MusicNext() {
        if (mBoundService != null) {
            mBoundService.MusicNext();

        }
    }

    void MusicPrev() {

        if (mBoundService != null) {
            mBoundService.MusicPrev();

        }


    }

    void MusicPlayPause() {
        if (mBoundService != null) {
            mBoundService.MusicPlayPause();

        }


    }

    void MusicSeek(int tmp) {
        mBoundService.MusicSeek(tmp);
    }

    void update() {
        mBoundService.SendInfo();
    }

    void setSeek(int seek, int sseek) {
        final SeekBar aplayer_progress = getActivity().findViewById(R.id.new_seekbar);
        aplayer_progress.setProgress(seek);
        aplayer_progress.setSecondaryProgress(sseek);
        audio_panel_progress.setProgress(seek);
        audio_panel_progress.setSecondaryProgress(sseek);

        final TextView aplayer_time = getActivity().findViewById(R.id.new_time);
        aplayer_time.setText(wstr.millisecondsToTime(seek));
    }


}

