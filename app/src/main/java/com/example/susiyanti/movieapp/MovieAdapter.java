package com.example.susiyanti.movieapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.susiyanti.movieapp.data.MovieContract;
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
    private Cursor mCursor;
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
            Movie m;
            if(movieData != null){
                m = movieData.get(adapterPosition);
            }else{
                mCursor.moveToPosition(adapterPosition);
                m = new Movie();
                m.setId(mCursor.getInt(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID)));
                m.setRelease_date(mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                m.setPoster_path(mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH)));
                m.setOverview(mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
                m.setTitle(mCursor.getString(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
                m.setVote_average(mCursor.getDouble(mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE)));
            }

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
        String imgUrl = "http://image.tmdb.org/t/p/w185/";
        if(movieData!=null) {
           imgUrl +=  movieData.get(position).getPoster_path();
        }else{
            // Indices for the _id, description, and priority columns
            int idIndex = mCursor.getColumnIndex(MovieContract.MovieEntry._ID);
            int posterPathIndex = mCursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH);

            mCursor.moveToPosition(position); // get to the right location in the cursor

            // Determine the values of the wanted data
            final int id = mCursor.getInt(idIndex);
            String posterpath = mCursor.getString(posterPathIndex);

            imgUrl += posterpath;
        }
        Picasso.with(holder.itemView.getContext()).load(imgUrl).into(holder.movieImg);
    }

    @Override
    public int getItemCount() {
        if (null == movieData)
            if(null == mCursor)
                return 0;
            else
                return mCursor.getCount();
        return movieData.size();
    }

    public void setMovieData(List<Movie> data){
        movieData = data;
        notifyDataSetChanged();
    }

    public Cursor swapCursor(Cursor c) {
        // check if this cursor is the same as the previous cursor (mCursor)
        if (mCursor == c) {
            return null; // bc nothing has changed
        }
        Cursor temp = mCursor;
        this.mCursor = c; // new cursor value assigned

        //check if this is a valid cursor, then update the cursor
        if (c != null) {
            this.notifyDataSetChanged();
        }
        return temp;
    }
}
