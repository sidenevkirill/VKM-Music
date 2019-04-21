package com.music.vkm;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.music.vkm.adapter.MusicAdapter;
import com.music.vkm.item.Music;
import com.music.vkm.item.PlayList;
import com.music.vkm.util.AudioUtil;
import com.music.vkm.util.Cookie;
import com.music.vkm.util.MusicSchedule;
import com.music.vkm.util.MusicService;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.music.vkm.AudioMainActivity.mBoundService;
import static com.music.vkm.AudioMainActivity.searchMenuItem;
import static com.music.vkm.AudioMainActivity.searchView;
import static com.music.vkm.LoginActivity.PERMISSION_REQUEST_CODE;

/**
 * Created by mascot on 13.10.2017.
 */

public class AudioListPopularFragment extends Fragment {
    String tmp, tmp1, tmp2, tmp3, tmp4, tmp5, tmp6, tmp7, tmp8;
    public static String hashadd, hashdel, hashres;
    public static String ownerId;
    private View view;
    SharedPreferences sPref;
    public static MusicSchedule schedule = new MusicSchedule();
    private ProgressDialog progressDialog;
    SwipeRefreshLayout mSwipeRefreshLayout;
    com.music.vkm.util.Cookie Cookie;
    String TAG = "Popular";
    static MusicAdapter adapter;

    Music tmpmusic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.audio_list_fragment, container, false);
        view = v;


        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        RecyclerView recMusic = view.findViewById(R.id.rv_fragment);

        LinearLayoutManager llm = new LinearLayoutManager(view.getContext());

        recMusic.setLayoutManager(llm);
        recMusic.setHasFixedSize(true);
        recMusic.setNestedScrollingEnabled(true);


        adapter = new MusicAdapter(schedule.getPlaylist(), new ArrayList<PlayList>(), 3);
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
        adapter.setOnShuffeClickListener(new MusicAdapter.OnShuffeClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {


                if (schedule.size() != 0) {

                    mBoundService.setMusicList(schedule);
                    mBoundService.shuffle();


                } else {
                    Snackbar.make(view, getResources().getString(R.string.notavailable), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
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


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mBoundService.updateCachedMusic();
                checkOffline();
            }
        });


        return v;
    }

    public static AudioListPopularFragment newInstance(String text) {

        AudioListPopularFragment f = new AudioListPopularFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);

        return f;
    }

    void getPopular() {

        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setRefreshing(true);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        Request request = new Request.Builder()
                .addHeader("accept-language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4")
                .addHeader("x-compress", "null")
                .addHeader("upgrade-insecure-requests", "1")
                .addHeader("referer", "https://m.vk.com/audio")
                .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("cookie", loadText("sid") + "; remixmdevice=1366/768/1/!!-!!!!")
                .url("https://m.vk.com/audio?act=popular")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null)
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);
                            RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
                            TextView info = view.findViewById(R.id.info);
                            if (schedule.size() > 0) {
                                recMusic.setVisibility(View.VISIBLE);
                                info.setVisibility(View.INVISIBLE);
                            } else {
                                recMusic.setVisibility(View.INVISIBLE);
                                info.setVisibility(View.VISIBLE);
                            }
                            info.setText(view.getResources().getText(R.string.internet_error));
                        }
                    });

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                final String responseData = response.body().string();

                String respons = responseData;

                Log.d("GetPopular", "onResponse: " + response.headers().toString());

                if (response.header("location") != null) {
                    if (response.header("location").contains("https://login.vk.com/?role=pda&_origin=")) {
                        Cookie = new Cookie();
                        Log.d("TryToGetSidOff", "start getting sid");
                        getSid(response.header("location"));
                    }
                } else {

                    if (respons.contains("<form method=\"post\" action=\"")) {
                        schedule.clear();
                        RefreshRV();
                        stopRefresh();
                        if (getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {


                                }
                            });
                    } else {

                        if (respons.contains("<div id=\"audio")) {

                            if (getActivity() != null)
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                       /* FrameLayout block_info = (FrameLayout) view.findViewById(R.id.block_info);
                                        block_info.setVisibility(View.VISIBLE);

                                        TextView text = (TextView) view.findViewById(R.id.text);
                                        text.setVisibility(View.INVISIBLE);

                                        Button act_login = (Button) view.findViewById(R.id.act_login);
                                        act_login.setVisibility(View.INVISIBLE);

                                        act_login.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                saveText("sid", "");
                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                startActivity(intent);
                                                AudioListPopularFragment.this.onDestroy();
                                            }
                                        });*/

                                    }
                                });
                            respons = respons.substring(respons.indexOf("<div id=\"audio"));

                            hashadd = AudioUtil.pars("\"add_hash\":\"", respons, "\"");
                            hashdel = AudioUtil.pars("\"del_hash\":\"", respons, "\"");
                            hashres = AudioUtil.pars("\"res_hash\":\"", respons, "\"");

                            schedule.clear();
                            while (respons.contains("</table>")) {

                                tmp = AudioUtil.pars("<div id=\"audio", respons, "</table>");

                                respons = respons.substring(respons.indexOf("</table>") + 7);


                                if (tmp.contains("background-image:url(")) {
                                    tmp1 = AudioUtil.pars("background-image:url(", tmp, ")");
                                } else
                                    tmp1 = "none";


                                String tmp2;
                                if (tmp.contains("audioplayer.del")) {
                                    tmp2 = "true";
                                } else {
                                    tmp2 = "false";
                                }
                                if (getActivity() != null)
                                    schedule.add(new Music(AudioUtil.pars("<span class=\"ai_artist\">", tmp, "</span>"), AudioUtil.pars("switchTimeFormat(this, event);\">", tmp, "</div>"), AudioUtil.pars("<input type=\"hidden\" value=\"", tmp, "\">"), AudioUtil.pars("<span class=\"ai_title\">", tmp, "</span>"), "null", tmp1, AudioUtil.pars("data-id=\"", tmp, "\""), "false", tmp2, getResources().getString(R.string.popular), hashadd, hashdel, hashres));


                            }

                        } else {
                            schedule.clear();
                        }
                        RefreshRV();
                        stopRefresh();
                    }

                    if (getActivity() != null)
                        if (getResources().getString(R.string.popular).equals(MusicService.getSchedule().getSystemNamePlaylist())) {
                            updateMusic();
                        }
                }
            }
        });
    }

    void updateMusic() {
        MusicService.getSchedule().updatePlaylist(schedule);
    }

    void stopRefresh() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);

                }
            });
    }

    void RefreshRV() {
        if (getActivity() != null)
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
                    recMusic.getAdapter().notifyDataSetChanged();
                    Log.d("UpdateRV", "run: " + schedule.size());

                    TextView info = view.findViewById(R.id.info);
                    if (schedule.size() > 0) {
                        recMusic.setVisibility(View.VISIBLE);
                        info.setVisibility(View.INVISIBLE);
                    } else {
                        recMusic.setVisibility(View.INVISIBLE);
                        info.setVisibility(View.VISIBLE);
                    }
                }
            });
    }

    private void checkOffline() {

        if (loadText("offline").equals("true")) {
            Snackbar.make(view, "Выйти из оффлайн?", Snackbar.LENGTH_LONG)
                    .setAction("Да", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getPopular();
                        }
                    }).show();
        } else {
            getPopular();

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        final RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
        if (schedule.size() == 0) {

            recMusic.setVisibility(View.INVISIBLE);
            checkOffline();
        } else {
            recMusic.setVisibility(View.VISIBLE);
        }

    }

    void getcookies2(Response response) {
        String cookie = response.headers().toString();
        String tmp1, tmp2, tmp3;


        Log.d("AuthCookie", cookie);
        if (response.header("location").contains("_q_hash=")) {
            Cookie.setCookie(1, loadText("l"));
            Cookie.setCookie(2, loadText("p"));
            Cookie.setCookie(3, ("remixq" + AudioUtil.pars("remixq", cookie, ".com") + ".com").replace(" ", ""));
        }


    }

    private void getSid(String url) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        final Request request = new Request.Builder()

                .addHeader("User-Agent", loadText("userAgent"))
                .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Upgrade-Insecure-Requests", "1")
                .addHeader("Origin", "https://m.vk.com/login")
                .addHeader("cookie", loadText("sid") + ";" + Cookie.getCookieinLine() + ";" + loadText("l") + ";" + loadText("p"))
                .url(url)


                .build();


        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {

                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {


                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();


                Log.d("TestAuth", responseHead);

                getcookies2(response);

                //  cookie.addCookie("remixq_" + AudioUtil.pars("remixq_", responseHead, ";"));
                if (responseHead.contains("remixq_")) {
                    getremixsid(response.header("location"));
                } else {

                    if (getActivity() != null)
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                RecyclerView recMusic = view.findViewById(R.id.rv_fragment);
                                recMusic.getAdapter().notifyDataSetChanged();
                                mSwipeRefreshLayout.setRefreshing(false);
                              /*  FrameLayout block_info = (FrameLayout) view.findViewById(R.id.block_info);
                                block_info.setVisibility(View.VISIBLE);

                                TextView text = (TextView) view.findViewById(R.id.text);
                                text.setVisibility(View.INVISIBLE);

                                Button act_login = (Button) view.findViewById(R.id.act_login);
                                act_login.setVisibility(View.VISIBLE);

                                //cardsVisibility(0);

                                act_login.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        saveText("sid", "");
                                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                                        startActivity(intent);
                                        AudioListPopularFragment.this.onDestroy();
                                    }
                                });*/

                            }
                        });
                }

            }
        });

    }

    private void getremixsid(String url) {
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
                .addHeader("cookie", loadText("sid") + ";" + Cookie.getCookieinLine())
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {


                final String responseData = response.body().string();
                final String responseHead = response.headers().toString();

                Log.d("GetNewSid", responseHead);

                saveText("sid", "remixsid" + AudioUtil.pars("remixsid", responseHead, ".com") + ".com");


                getPopular();

            }
        });
    }

    void saveText(String saved_text, String save) {
        sPref = getActivity().getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(saved_text, save);
        ed.commit();
    }

    private String loadText(String saved_text) {
        if (getContext() != null) {
            SharedPreferences sPref = getContext().getSharedPreferences(SettingsGeneralActivity.SPreferences, MODE_PRIVATE);
            String savedText = sPref.getString(saved_text, "");
            return savedText;
        }
        return "null";
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

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.AlertDialog);
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