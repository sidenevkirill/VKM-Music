package com.music.vkm.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.music.vkm.PlayListViewActivity;
import com.music.vkm.R;
import com.music.vkm.item.PlayList;
import com.music.vkm.util.AudioUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by halez on 06.01.2018.
 */


public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.HistViewHolder> {
    View view;

    private MusicAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }


    public void setOnItemClickListener(MusicAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }


    public class HistViewHolder extends RecyclerView.ViewHolder {


        TextView title;
        ImageView cover;
        LinearLayout rl;


        HistViewHolder(final View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.cover);
            title = itemView.findViewById(R.id.title);
            rl = itemView.findViewById(R.id.rv_cover);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }
    }

    List<PlayList> playLists;

    public List<PlayList> getList() {
        return playLists;
    }

    PlaylistsAdapter(List<PlayList> list) {
        this.playLists = list;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PlaylistsAdapter.HistViewHolder onCreateViewHolder(ViewGroup parentViewGroup, int i) {
        View v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.playlist_item, parentViewGroup, false);
        view = v;
        return new PlaylistsAdapter.HistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final PlaylistsAdapter.HistViewHolder histViewHolder, final int i) {

        if (!playLists.get(i).getItemCover().contains("_grid_count_")) {
            if (playLists.get(i).getItemCover().contains("url(")) {
                Picasso.get()
                        .load(AudioUtil.pars("url('", playLists.get(i).getItemCover(), "')"))
                        .placeholder(R.drawable.placeholder_playlist)
                        .error(R.drawable.placeholder_playlist)
                        .into(histViewHolder.cover);
            } else {

                histViewHolder.cover.setImageResource(R.drawable.placeholder_playlist);

            }
        } else {
            histViewHolder.cover.setImageResource(R.drawable.placeholder_playlist);
        }

        histViewHolder.title.setText(playLists.get(i).getItemTitle());

        histViewHolder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(view.getContext(), PlayListViewActivity.class);
                intent.putExtra("playlist", playLists.get(i));
                v.getContext().startActivity(intent);

            }
        });


    }


    @Override
    public int getItemCount() {
        return playLists.size();
    }

}