package com.example.susiyanti.movieapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Lab316-PC17 on 6/15/2017.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder>{

    private List<Movie> movieData = Collections.EMPTY_LIST;

    private final MovieAdapterOnClickHandler mClickHandler;

    public interface MovieAdapterOnClickHandler {
        void onClick(Movie m);
    }

    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public final ImageView movieImg;

        public MovieViewHolder(View view){
            super(view);
            movieImg = (ImageView) view.findViewById(R.id.movie_img);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            Movie m = movieData.get(adapterPosition);
            mClickHandler.onClick(m);
        }
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        String imgUrl = "http://image.tmdb.org/t/p/w185/" + movieData.get(position).getPoster_path();
        Picasso.with(holder.itemView.getContext()).load(imgUrl).into(holder.movieImg);
    }

    @Override
    public int getItemCount() {
        if (null == movieData) return 0;
        return movieData.size();
    }

    public void setMovieData(List<Movie> data){
        movieData = data;
        notifyDataSetChanged();
    }
}
