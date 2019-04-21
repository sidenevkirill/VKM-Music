package com.music.vkm;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.music.vkm.adapter.MusicAdapter;
import com.music.vkm.item.Music;
import com.music.vkm.item.PlayList;
import com.music.vkm.util.MusicService;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.music.vkm.AudioMainActivity.mBoundService;
import static com.music.vkm.AudioMainActivity.searchMenuItem;
import static com.music.vkm.AudioMainActivity.searchView;
import static com.music.vkm.LoginActivity.PERMISSION_REQUEST_CODE;

/**
 * Created by mascot on 04.10.2017.
 */

public class PlayListFragment extends Fragment {

    BroadcastReceiver service;
    private View view;
    String TAG = "PlayList";
    SharedPreferences sPref;
    Music tmpmusic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.new_audio_player_playlist, container, false);

        view = v;


        setRV();
        getInfo();
        return v;
    }

    public static PlayListFragment newInstance(String text) {

        PlayListFragment f = new PlayListFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);


        return f;
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    public void setRV() {

        RecyclerView recMusic = view.findViewById(R.id.new_playlist);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());


        recMusic.setLayoutManager(llm);
        recMusic.setHasFixedSize(true);
        recMusic.setNestedScrollingEnabled(false);
        // recMusic.addItemDecoration(new SpacesItemDecoration(5));

        final MusicAdapter adapter = new MusicAdapter(MusicService.getSchedule().getPlaylist(), new ArrayList<PlayList>(), 5);
        adapter.setOnItemClickListener(new MusicAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Log.d(TAG, "onItemClick: ");

                if (adapter.getList().size() > 0) {
                    mBoundService.setMusicList(adapter.getList());
                    mBoundService.setPosition(adapter.getList().get(position));
                }
            }
        });

        adapter.setOnMenuClickListener(new MusicAdapter.OnMenuClickListener() {
            @Override
            public void onItemClick(View itemView, final int position) {


                final PopupMenu popup = new PopupMenu(view.getContext(), itemView);
                popup.getMenuInflater().inflate(R.menu.audio, popup.getMenu());


                popup.show();
                if (adapter.getList().size() >= position) {
                    if (mBoundService.findSavedMusic(adapter.getList().get(position))) {
                        popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);
                    } else {
                        popup.getMenu().getItem(0).setTitle(R.string.saveToCache);
                    }
                    if (adapter.getList().get(position).getYour().equals("true")) {
                        popup.getMenu().getItem(2).setTitle(view.getResources().getString(R.string.delete));
                    }
                }


                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.saveToCache))) {
                            if (hasPermissions()) {
                                mBoundService.cacheMusic(adapter.getList().get(position));
                                popup.getMenu().getItem(0).setTitle(R.string.deleteFromCache);
                            } else {
                                Toast.makeText(getContext(), "Для сохранения, нужно разрешение на чтение и запись в память.", Toast.LENGTH_SHORT).show();
                                tmpmusic = adapter.getList().get(position);
                                requestPerms();
                            }

                            return true;
                        }
                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.deleteFromCache))) {

                            if (hasPermissions()) {
                                mBoundService.cacheMusic(adapter.getList().get(position));
                                popup.getMenu().getItem(0).setTitle(R.string.saveToCache);
                            } else {
                                Toast.makeText(getContext(), "Для сохранения, нужно разрешение на чтение и запись в память.", Toast.LENGTH_SHORT).show();
                                tmpmusic = adapter.getList().get(position);
                                requestPerms();
                            }

                            return true;
                        }


                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.delete))) {
                            item.setTitle(view.getResources().getString(R.string.add));
                            mBoundService.addMusic(adapter.getList().get(position));
                            return true;
                        }
                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.add))) {
                            item.setTitle(view.getResources().getString(R.string.delete));
                            mBoundService.addMusic(adapter.getList().get(position));
                            return true;
                        }
                        if (item.getTitle().toString().equals(view.getResources().getString(R.string.findartist))) {


                            searchMenuItem.expandActionView();
                            searchView.setQuery(adapter.getList().get(position).getArtist(), true);
                            AudioMainActivity.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            return true;

                        }
                        if (item.getTitle().equals(view.getResources().getString(R.string.playNext))) {

                            MusicService.getSchedule().insertNextMusic(adapter.getList().get(position));
                            return true;
                        }
                        if (view != null)
                            if (item.getTitle().toString().equals(view.getResources().getString(R.string.downloadmusic))) {
                                mBoundService.saveMusicFull(adapter.getList().get(position));
                            }
                        return true;
                    }
                });

                // recMusic.getAdapter().notifyDataSetChanged();
            }

        });


        recMusic.setAdapter(adapter);

        recMusic.scrollToPosition(MusicService.getSchedule().getPlaylist().indexOf(MusicService.getSchedule().getCurrentMusic()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
        itemTouchHelper.attachToRecyclerView(recMusic);

    }

    private void saveText(String saved_text, String save) {
        if (getContext() != null) {
            sPref = getContext().getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString(saved_text, save);
            ed.commit();
        }
    }

    private String loadText(String saved_text) {
        SharedPreferences sPref = getContext().getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }


    void setInfo() {
        //  if (MusicService.isFirst && MusicService.getSchedule().size() != 0) {
        setRV();


        TextView new_artist = view.findViewById(R.id.new_artist);

        new_artist.setText(MusicService.getSchedule().getNamePlaylist());

        Log.d(TAG, "setRV: " + MusicService.getSchedule().getSystemNamePlaylist());
        RecyclerView recMusic = view.findViewById(R.id.new_playlist);
        recMusic.scrollToPosition(MusicService.getSchedule().getPosition());

    }

    void getInfo() {
        service = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("com.mascotworld.vkaudiomanager.send")) {
                    setInfo();
                }
            }
        };

        IntentFilter intFilt3 = new IntentFilter("com.mascotworld.vkaudiomanager.send");
        getActivity().registerReceiver(service, intFilt3);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(service);
    }


    private ItemTouchHelper.Callback createHelperCallback() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT) {


                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        deleteItem(viewHolder.getAdapterPosition());
                    }

                    @Override
                    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                        final ColorDrawable background = new ColorDrawable(Color.RED);
                        background.setBounds(viewHolder.itemView.getRight() + (int) dX,
                                viewHolder.itemView.getTop(),
                                viewHolder.itemView.getRight(),
                                viewHolder.itemView.getBottom());
                        background.draw(c);

                        Drawable icon = ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.ic_delete_24);


                        int iconTop = viewHolder.itemView.getTop() + (viewHolder.itemView.getHeight() - 25) / 2;
                        int iconMargin = (viewHolder.itemView.getHeight() - 25) / 2;
                        int iconLeft = viewHolder.itemView.getRight() - iconMargin - 25;
                        int iconRight = viewHolder.itemView.getRight() - iconMargin;
                        int iconBottom = iconTop + 25;

                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
                        icon.draw(c);

                        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                    }
                };
        return simpleItemTouchCallback;
    }

    private void moveItem(int oldPos, int newPos) {

        //TODO: не правильно работает
        RecyclerView recMusic = view.findViewById(R.id.new_playlist);


        recMusic.getAdapter().notifyItemMoved(oldPos, newPos);
        MusicService.getSchedule().swapMusic(oldPos, newPos);

    }

    private void deleteItem(int position) {
        RecyclerView recMusic = view.findViewById(R.id.new_playlist);
        MusicService.getSchedule().getPlaylist().remove(position);
        recMusic.getAdapter().notifyItemRemoved(position);

        if (MusicService.getSchedule().position != 0) {
            MusicService.getSchedule().position--;
        }

        if (!MusicService.getSchedule().getSystemNamePlaylist().contains("changedPlaylistSystem")) {
            MusicService.getSchedule().setSystemNamePlaylist(MusicService.getSchedule().getSystemNamePlaylist() + "changedPlaylistSystem");
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
}