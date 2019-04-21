package com.music.vkm.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.music.vkm.item.Music;
import com.music.vkm.R;
import com.music.vkm.util.AudioUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mascot on 06.10.2017.
 */

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.HistViewHolder> {

    private Context mContext;
    private List<Music> listfordownload = new ArrayList<>();
    private boolean[] checked;


    private MusicAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }


    public void setOnItemClickListener(MusicAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public static class HistViewHolder extends RecyclerView.ViewHolder {


        TextView artist;
        TextView title;
        CheckBox CheckBox;
        ImageView download;
        ImageView pic;
        CardView cv;


        HistViewHolder(View itemView) {
            super(itemView);
            download = itemView.findViewById(R.id.playlist_saved_icon);
            pic = itemView.findViewById(R.id.pic);
            artist = itemView.findViewById(R.id.artist);
            title = itemView.findViewById(R.id.title);
            CheckBox = itemView.findViewById(R.id.chbox_download);
            cv = itemView.findViewById(R.id.cv);

        }
    }


    private List<Music> musicList;

    public DownloadAdapter(List<Music> musics) {
        listfordownload.clear();
        checked = new boolean[musics.size()];
        this.musicList = musics;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public DownloadAdapter.HistViewHolder onCreateViewHolder(ViewGroup parentViewGroup, int i) {
        mContext = parentViewGroup.getContext();
        View v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.download_item, parentViewGroup, false);
        return new DownloadAdapter.HistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final DownloadAdapter.HistViewHolder histViewHolder, final int i) {
        histViewHolder.title.setText(AudioUtil.normalizeString(musicList.get(i).getTitle()));
        histViewHolder.artist.setText(AudioUtil.normalizeString(musicList.get(i).getArtist()));


        if (musicList.get(i).getSave().equals("true")) {
            histViewHolder.download.setVisibility(View.VISIBLE);
        } else {
            histViewHolder.download.setVisibility(View.INVISIBLE);
        }


        if (!musicList.get(i).getPic().equals("none")) {
            Picasso.get()
                    .load(musicList.get(i).getPic())
                    .placeholder(R.drawable.placeholder_albumart_56dp)
                    .error(R.drawable.placeholder_albumart_56dp)
                    .into(histViewHolder.pic);
        } else {
            histViewHolder.pic.setBackgroundResource(R.drawable.placeholder_albumart_56dp);
            histViewHolder.pic.setImageDrawable(null);
        }

        //CheckBox

        View.OnClickListener chboxclick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checked[i] = !checked[i];
                if (checked[i]) {
                    listfordownload.add(musicList.get(i));
                    histViewHolder.CheckBox.setChecked(true);
                } else {
                    histViewHolder.CheckBox.setChecked(false);

                    if (listfordownload.contains(musicList.get(i))) {
                        listfordownload.remove(musicList.get(i));
                    }
                }
            }
        };


        histViewHolder.cv.setOnClickListener(chboxclick);
        histViewHolder.CheckBox.setOnClickListener(chboxclick);

        if (checked.length > 0)
            if (checked[i]) {
                if (!listfordownload.contains(musicList.get(i)))
                    listfordownload.add(musicList.get(i));
                histViewHolder.CheckBox.setChecked(true);
            } else {

                if (listfordownload.contains(musicList.get(i))) {
                    listfordownload.remove(musicList.get(i));
                }
                histViewHolder.CheckBox.setChecked(false);
            }
        histViewHolder.cv.setTag(i);
    }


    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public boolean[] getChecked() {
        return checked;
    }

    public void setChecked(boolean type) {
        boolean[] checkarray = new boolean[musicList.size()];
        if (type) {
            for (int i = 0; i < checkarray.length; i++)
                checkarray[i] = true;

            listfordownload.addAll(musicList);
        } else {
            listfordownload.clear();
        }
        checked = checkarray;
        notifyDataSetChanged();
    }


    public int getCountChecked() {
        return listfordownload.size();
    }

    public List<Music> getDownloadList() {
        return listfordownload;
    }

}