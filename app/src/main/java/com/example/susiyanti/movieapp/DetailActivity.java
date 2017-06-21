package com.example.susiyanti.movieapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private TextView movieTitle;
    private TextView movieOverview;
    private TextView movieYear;
    private TextView movieVote;
    private ImageView movieThumb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        movieTitle = (TextView) findViewById(R.id.movie_title);
        movieOverview = (TextView) findViewById(R.id.movie_overview);
        movieYear = (TextView) findViewById(R.id.movie_year);
        movieVote = (TextView) findViewById(R.id.movie_vote);
        movieThumb = (ImageView) findViewById(R.id.movie_thumb);

        Intent intent = getIntent();
        Movie m = intent.getParcelableExtra(Intent.EXTRA_TEXT);
        movieTitle.setText(m.getTitle());
        movieOverview.setText(m.getOverview());
        movieYear.setText(m.getRelease_date());
        movieVote.setText(m.getVote_average()+" / 10");
        String imgUrl = "http://image.tmdb.org/t/p/w185/" + m.getPoster_path();
        Picasso.with(this).load(imgUrl).into(movieThumb);
    }
}
