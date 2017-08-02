package com.example.susiyanti.movieapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.susiyanti.movieapp.data.MovieContract;
import com.example.susiyanti.movieapp.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler, LoaderManager.LoaderCallbacks<List<Movie>>{

    private static final String TAG = MainActivity.class.getSimpleName();
    private static String sort_by = "popular";
    private static final int MOVIE_LOADER = 22;

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    private TextView mErrorMessageDisplay;
    private ProgressBar mLoadingIndicator;

    List<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movie);
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mRecyclerView.setLayoutManager(layoutManager);

        if(savedInstanceState!=null){
            sort_by = savedInstanceState.getString("sort");
            movies = savedInstanceState.getParcelableArrayList("movie");
            mMovieAdapter.setMovieData(movies);
        }
        if(movies==null) {
            getSupportLoaderManager().initLoader(MOVIE_LOADER, null, this);
            loadMovieData(sort_by);
        }
    }

    private void loadMovieData(String sortedBy){
        showMovieDataView();
        Bundle queryBundle = new Bundle();
        queryBundle.putString("sort", sortedBy);
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<List<Movie>> movieLoader = loaderManager.getLoader(MOVIE_LOADER);
        if(movieLoader == null){
            Log.d("fav", "first");
            loaderManager.initLoader(MOVIE_LOADER, queryBundle, this);
        }else{
            Log.d("fav", "restart");
            loaderManager.restartLoader(MOVIE_LOADER, queryBundle, this);
        }
    }

    private void showMovieDataView() {
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(Movie m) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra(Intent.EXTRA_TEXT, m);
        startActivity(intentToStartDetailActivity);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(this) {

            @Override
            protected void onStartLoading() {
                if(args == null){
                    return;
                }
                mLoadingIndicator.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Override
            public List<Movie> loadInBackground() {
                if (args.size() == 0) {
                    return null;
                }
                if(args.getString("sort").equals("fav")){

                    try{
                        Log.d("fav","ambil data dari db");
                        Cursor cursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                null);
                        List<Movie> movies = new ArrayList<Movie>();
                        if (cursor.moveToFirst()) {
                            while (!cursor.isAfterLast()) {
                                Movie m = new Movie();
                                m.setId(cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_ID)));
                                m.setOverview(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW)));
                                m.setPoster_path(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH)));
                                m.setRelease_date(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE)));
                                m.setTitle(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE)));
                                m.setVote_average(cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE)));
                                movies.add(m);
                                cursor.moveToNext();
                            }
                        }
                        cursor.close();
                        return movies;
                    }catch (Exception e){
                        Log.e(TAG, "Failed to asynchronously load data.");
                        e.printStackTrace();
                        return null;
                    }
                }else {
                    URL movieRequestUrl = NetworkUtils.buildUrl(args.getString("sort"));

                    try {
                        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo netInfo = cm.getActiveNetworkInfo();
                        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                            String jsonResponse = NetworkUtils.getResponseFromHttpUrl(movieRequestUrl);
                            JSONObject movieJson = new JSONObject(jsonResponse);
                            JSONArray movieArray = movieJson.getJSONArray("results");

                            List<Movie> movies = new ArrayList<Movie>();
                            for (int i = 0; i < movieArray.length(); i++) {
                                JSONObject movieData = movieArray.getJSONObject(i);
                                Movie m = new Movie();
                                m.setId(movieData.getInt("id"));
                                m.setTitle(movieData.getString("title"));
                                m.setOverview(movieData.getString("overview"));
                                m.setPoster_path(movieData.getString("poster_path"));
                                m.setVote_average(movieData.getDouble("vote_average"));
                                m.setRelease_date(movieData.getString("release_date"));
                                movies.add(m);
                            }
                            Log.d("Network", "Didlaman load");
                            return movies;
                        } else {
                            return null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data != null) {
            showMovieDataView();
            movies =  data;
            mMovieAdapter.setMovieData(data);
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.sort_pop:
                sort_by = "popular";
                loadMovieData(sort_by);
                return true;
            case R.id.sort_top:
                sort_by = "top_rated";
                loadMovieData(sort_by);
                return true;
            case R.id.sort_fav:
                sort_by = "fav";
                loadMovieData(sort_by);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("sort", sort_by);
        outState.putParcelableArrayList("movie", (ArrayList<? extends Parcelable>) movies);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        sort_by = savedInstanceState.getString("sort");
        movies = savedInstanceState.getParcelableArrayList("movie");
    }
}
