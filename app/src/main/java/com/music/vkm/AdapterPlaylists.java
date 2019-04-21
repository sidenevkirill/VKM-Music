package com.music.vkm;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by halez on 06.01.2018.
 */


public class AdapterPlaylists extends RecyclerView.Adapter<com.music.vkm.AdapterPlaylists.HistViewHolder> {
    View view;

    private AdapterMusic.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }


    public void setOnItemClickListener(AdapterMusic.OnItemClickListener listener) {
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

    AdapterPlaylists(List<PlayList> list) {
        this.playLists = list;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public com.music.vkm.AdapterPlaylists.HistViewHolder onCreateViewHolder(ViewGroup parentViewGroup, int i) {
        View v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.playlist_item, parentViewGroup, false);
        view = v;
        return new com.music.vkm.AdapterPlaylists.HistViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final com.music.vkm.AdapterPlaylists.HistViewHolder histViewHolder, final int i) {

        if (!playLists.get(i).getItemCover().contains("_grid_count_")) {
            if (playLists.get(i).getItemCover().contains("url(")) {
                Picasso.with(view.getContext())
                        .load(wstr.pars("url('", playLists.get(i).getItemCover(), "')"))
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

                Intent intent = new Intent(view.getContext(), PlayList_View.class);
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