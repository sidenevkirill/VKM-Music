package com.music.vkm;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by mascot on 31.08.2017.
 */

public class AdapterMusic extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private Context mContext;
    private View view_ex, view_playlists;
    String TAG = "AdapterMusic";
    int type = 1;

    private OnItemClickListener listener;
    private OnMenuClickListener menuListener;
    private OnShuffeClickListener shuffeListener;

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (type == 5) return 1;
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    public interface OnMenuClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnMenuClickListener(OnMenuClickListener listener) {
        this.menuListener = listener;
    }

    public interface OnShuffeClickListener {
        void onItemClick(View itemView, int position);
    }

    public void setOnShuffeClickListener(OnShuffeClickListener listener) {
        this.shuffeListener = listener;
    }


    public class MusicViewHolder extends RecyclerView.ViewHolder {

        ImageView pic;
        TextView artist;
        TextView title;
        // TextView time;
        ImageView download;
        ImageButton button;
        RelativeLayout cv;


        MusicViewHolder(final View itemView) {
            super(itemView);
            pic = itemView.findViewById(R.id.pic);
            artist = itemView.findViewById(R.id.artist);
            title = itemView.findViewById(R.id.title);
            //  time = (TextView) itemView.findViewById(R.id.time_item);
            button = itemView.findViewById(R.id.settings_item);
            cv = itemView.findViewById(R.id.cv);
            download = itemView.findViewById(R.id.playlist_saved_icon);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position - 1);
                        }
                    }
                }
            });
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (menuListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            menuListener.onItemClick(button, position - 1);
                        }
                    }
                }
            });


        }
    }

    public class PlayListViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;
        CardView shuffle_all;


        PlayListViewHolder(final View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.rv_playlist);
            shuffle_all = itemView.findViewById(R.id.shuffle_all);
            shuffle_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (shuffeListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            shuffeListener.onItemClick(shuffle_all, position);
                        }
                    }
                }
            });


        }

    }


    public List<Music> musicList;
    public List<Music> musicListFiltered;
    public List<PlayList> playLists;


    public List<Music> getList() {
        return musicList;
    }

    public AdapterMusic(List<Music> datas, List<PlayList> playLists, int type) {
        this.musicList = datas;
        this.musicListFiltered = datas;
        this.playLists = playLists;
        this.type = type;
    }

    public AdapterMusic(Music_Schedule schedule, List<PlayList> playLists, int type) {
        this.musicList = schedule.playlist;
        this.musicListFiltered = schedule.playlist;
        this.playLists = playLists;
        this.type = type;
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        mContext = parentViewGroup.getContext();

        switch (viewType) {
            case 0: {
                View v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.music_item, parentViewGroup, false);

                if (type == 1) {
                    v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.playlist_null, parentViewGroup, false);
                } else if (type == 2) {
                    v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.playlist_header, parentViewGroup, false);
                } else if (type == 3) {
                    v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.shuffle_view, parentViewGroup, false);

                } else if (type == 4) {
                    v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.playlist_not_null, parentViewGroup, false);
                }

                view_playlists = v;
                return new PlayListViewHolder(v);
            }
            case 1: {
                View v = LayoutInflater.from(parentViewGroup.getContext()).inflate(R.layout.music_item, parentViewGroup, false);

                view_ex = v;

                return new MusicViewHolder(v);
            }
        }


        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Log.d("notifyDataSetChanged", "position: " + Integer.toString(musicList.size()));

        switch (holder.getItemViewType()) {
            case 0: {
                Log.d(TAG, "playList size: " + Integer.toString(playLists.size()));
                if (type == 4) {
                    PlayListViewHolder playListViewHolder = (PlayListViewHolder) holder;

                    RecyclerView recPlaylists = playListViewHolder.recyclerView;
                    LinearLayoutManager llm = new LinearLayoutManager(view_playlists.getContext(), LinearLayoutManager.HORIZONTAL, false);
                    recPlaylists.setLayoutManager(llm);
                    recPlaylists.setHasFixedSize(false);
                    recPlaylists.setNestedScrollingEnabled(false);
                    recPlaylists.addItemDecoration(new SpacesItemDecoration(5));
                    final AdapterPlaylists adapterPlaylists = new AdapterPlaylists(playLists);
                    adapterPlaylists.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(View itemView, int position) {

                        }
                    });
                    recPlaylists.setAdapter(adapterPlaylists);


                }

                break;
            }
            case 1: {
                Log.d("TestViewRVSaveMusic", "onBindViewHolder: ");
                MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
                int i;
                if (type == 5)
                    i = position;
                else i = position-1;

                if (i < musicList.size()) {
                    musicViewHolder.title.setText(wstr.normalizeString(musicList.get(i).getTitle()));
                    musicViewHolder.artist.setText(wstr.normalizeString(musicList.get(i).getArtist()));

                    if (!musicList.get(i).getLyrics_id().equals("null")) {
                        musicViewHolder.title.setTextColor(view_ex.getResources().getColor(R.color.colorPrimary));
                    } else {
                        musicViewHolder.title.setTextColor(view_ex.getResources().getColor(R.color.md_black_1000));
                    }


                    if (musicList.get(i).getSave().equals("true")) {
                        musicViewHolder.download.setVisibility(View.VISIBLE);
                    } else {
                        musicViewHolder.download.setVisibility(View.INVISIBLE);
                    }


                    if (!musicList.get(i).getPic().equals("none")) {

                        if (!musicList.get(i).getPic().equals("")) {
                            Picasso.with(mContext)
                                    .load(musicList.get(i).getPic())
                                    .placeholder(R.drawable.placeholder_albumart_56dp)
                                    .error(R.drawable.placeholder_albumart_56dp)
                                    .into(musicViewHolder.pic);
                        } else {
                            musicViewHolder.pic.setBackgroundResource(R.drawable.placeholder_albumart_56dp);
                            musicViewHolder.pic.setImageDrawable(null);
                        }

                    } else {
                        musicViewHolder.pic.setBackgroundResource(R.drawable.placeholder_albumart_56dp);
                        musicViewHolder.pic.setImageDrawable(null);
                    }
                }

                break;
            }

        }


    }


    @Override
    public int getItemCount() {

        if (musicList == null) return 0;
        return musicList.size() + 1;

    }


    private String loadText(String saved_text, View v) {
        SharedPreferences sPref = v.getContext().getSharedPreferences(Settings.SPreferences, MODE_PRIVATE);
        String savedText = sPref.getString(saved_text, "");
        return savedText;
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    musicList = musicListFiltered;
                } else {
                    musicList = musicListFiltered;
                    List<Music> filteredList = new ArrayList<>();
                    String first, second;
                    charString = charString.toLowerCase();
                    for (int i = 0; i < musicList.size(); i++) {
                        first = musicList.get(i).getArtist().toLowerCase();
                        second = musicList.get(i).getTitle().toLowerCase();

                        if (first.contains(charString) || second.contains(charString)) {
                            filteredList.add(musicList.get(i));
                        }
                    }


                    musicList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = musicList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                musicList = (ArrayList<Music>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }






}
