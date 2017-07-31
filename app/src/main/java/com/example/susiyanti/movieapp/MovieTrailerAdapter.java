package com.example.susiyanti.movieapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * Created by Lab316-PC17 on 7/31/2017.
 */

public class MovieTrailerAdapter extends RecyclerView.Adapter<MovieTrailerAdapter.TrailerViewHolder> {

    private List<String> trailerName = Collections.EMPTY_LIST;
    private List<String> trailerData = Collections.EMPTY_LIST;

    private final MovieTrailerAdapter.MovieTrailerAdapterOnClickHandler mClickHandler;

    public interface MovieTrailerAdapterOnClickHandler {
        void onClick(String t);
    }

    public MovieTrailerAdapter(MovieTrailerAdapterOnClickHandler clickHandler){
        mClickHandler = clickHandler;
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_trailer_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MovieTrailerAdapter.TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        holder.mTrailer.setText(trailerName.get(position));
    }

    @Override
    public int getItemCount() {
        if (null == trailerData) return 0;
        return trailerData.size();
    }

    public void setMovieData(List<String> name, List<String> data){
        trailerData = data;
        trailerName = name;
        notifyDataSetChanged();
    }

    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public final TextView mTrailer;

        public TrailerViewHolder(View view){
            super(view);
            mTrailer = (TextView) view.findViewById(R.id.movie_trailer);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String t = "http://www.youtube.com/watch?v="+trailerData.get(adapterPosition);
            mClickHandler.onClick(t);
        }
    }

}
