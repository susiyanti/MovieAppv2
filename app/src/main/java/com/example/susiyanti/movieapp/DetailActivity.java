package com.example.susiyanti.movieapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.susiyanti.movieapp.data.MovieContract;
import com.example.susiyanti.movieapp.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity implements MovieTrailerAdapter.MovieTrailerAdapterOnClickHandler, LoaderManager.LoaderCallbacks<String>{

    private TextView movieTitle;
    private TextView movieOverview;
    private TextView movieYear;
    private TextView movieVote;
    private ImageView movieThumb;
    private Button fav;

    private RecyclerView mRecyclerViewTrailer;
    private RecyclerView mRecyclerViewReview;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;
    private MovieTrailerAdapter movieTrailerAdapter;
    private MovieReviewAdapter movieReviewAdapter;

    private List<String> trailers;
    private List<String> trailersName;
    private List<String> reviews;
    private Movie m;

    private static final int TRAILER_LOADER = 33;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        movieTitle = (TextView) findViewById(R.id.movie_title);
        movieOverview = (TextView) findViewById(R.id.movie_overview);
        movieYear = (TextView) findViewById(R.id.movie_year);
        movieVote = (TextView) findViewById(R.id.movie_vote);
        movieThumb = (ImageView) findViewById(R.id.movie_thumb);
        mRecyclerViewTrailer = (RecyclerView) findViewById(R.id.recyclerview_trailer);
        mRecyclerViewReview = (RecyclerView) findViewById(R.id.recyclerview_review);
        movieTrailerAdapter = new MovieTrailerAdapter(this);
        movieReviewAdapter = new MovieReviewAdapter();
        mRecyclerViewTrailer.setAdapter(movieTrailerAdapter);
        mRecyclerViewReview.setAdapter(movieReviewAdapter);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        mRecyclerViewTrailer.setLayoutManager(layoutManager1);
        mRecyclerViewReview.setLayoutManager(layoutManager2);

        Intent intent = getIntent();
        m = intent.getParcelableExtra(Intent.EXTRA_TEXT);
        movieTitle.setText(m.getTitle());
        movieOverview.setText(m.getOverview());
        movieYear.setText(m.getRelease_date());
        movieVote.setText(m.getVote_average()+" / 10");
        String imgUrl = "http://image.tmdb.org/t/p/w185/" + m.getPoster_path();
        Picasso.with(this).load(imgUrl).into(movieThumb);

        fav = (Button)findViewById(R.id.add_fav);
        final String mSelectionClause = MovieContract.MovieEntry.COLUMN_ID + " = ?";

        final String mSelectionArgs[] = {m.getId()+""};
        final Cursor mCursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                null,
                mSelectionClause,
                mSelectionArgs,
                null);
        if(mCursor == null){
            Log.e("detail", "ada error");
        }else if(mCursor.getCount() < 1){
            fav.setText("Mark as Favorite");
            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues contentValues = new ContentValues();

                    contentValues.put(MovieContract.MovieEntry.COLUMN_ID, m.getId());
                    contentValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, m.getOverview());
                    contentValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, m.getPoster_path());
                    contentValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, m.getRelease_date());
                    contentValues.put(MovieContract.MovieEntry.COLUMN_TITLE, m.getTitle());
                    contentValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, m.getVote_average());
                    // Insert the content values via a ContentResolver
                    Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);

                   if(uri != null) {
                        Toast.makeText(getBaseContext(), "Add to Favorite", Toast.LENGTH_LONG).show();
                    }

                    fav.setText("Unmark as Favorite");
                }
            });
        }else{
            fav.setText("Unmark as Favorite");
            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int mRowsDeleted = 0;
                    mCursor.moveToPosition(0);
                    mRowsDeleted = getContentResolver().delete(
                            MovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(mCursor.getInt(mCursor.getColumnIndex(MovieContract.MovieEntry._ID))+"").build(),
                            null,
                            null
                    );
                    if(mRowsDeleted>0){
                        Toast.makeText(getBaseContext(), "Unfavorited Movie", Toast.LENGTH_LONG).show();
                    }
                    fav.setText("Mark as Favorite");
                }
            });
        }

        getSupportLoaderManager().initLoader(TRAILER_LOADER, null, this);
        loadMovieData(m);
    }

    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerViewTrailer.setVisibility(View.VISIBLE);
        mRecyclerViewReview.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerViewTrailer.setVisibility(View.INVISIBLE);
        mRecyclerViewReview.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void loadMovieData(Movie m) {
        showMovieDataView();
        Bundle queryBundle = new Bundle();
        queryBundle.putInt("id", m.getId());
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<String> trailerLoader = loaderManager.getLoader(TRAILER_LOADER);
        if(trailerLoader == null){
            loaderManager.initLoader(TRAILER_LOADER, queryBundle, this);
        }else{
            loaderManager.restartLoader(TRAILER_LOADER, queryBundle, this);
        }
    }


    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            @Override
            protected void onStartLoading() {
                if(args == null){
                    return;
                }
                mLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                if (args.size() == 0) {
                    return null;
                }

                URL movieRequestUrl = NetworkUtils.buildUrl(args.getInt("id")+"","videos");

                try {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo netInfo = cm.getActiveNetworkInfo();
                    if( netInfo != null && netInfo.isConnectedOrConnecting()){
                        String jsonResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                        JSONObject movieJson = new JSONObject(jsonResponse);
                        JSONArray trailerArray = movieJson.getJSONArray("results");

                        trailers = new ArrayList<String>();
                        trailersName = new ArrayList<String>();

                        for (int i=0; i<trailerArray.length(); i++){
                            JSONObject movieData = trailerArray.getJSONObject(i);
                            trailers.add(movieData.getString("key"));
                            trailersName.add(movieData.getString("name"));
                        }

                        movieRequestUrl = NetworkUtils.buildUrl(args.getInt("id")+"","reviews");
                        jsonResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                        movieJson = new JSONObject(jsonResponse);
                        trailerArray = movieJson.getJSONArray("results");

                        reviews = new ArrayList<String>();

                        for (int i=0; i<trailerArray.length(); i++){
                            JSONObject movieData = trailerArray.getJSONObject(i);
                            reviews.add(movieData.getString("content"));
                        }
                        return "";
                    }else{
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String data) {
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data != null) {
            showMovieDataView();
            movieTrailerAdapter.setMovieData(trailersName, trailers);
            movieReviewAdapter.setMovieData(reviews);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    public void onClick(String t) {
        Uri webpage = Uri.parse(t);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);
    }
}
