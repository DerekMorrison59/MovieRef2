package com.derekmorrison.movieref2.Data;

import android.content.ContentValues;

import com.derekmorrison.movieref2.MovieData;

/**
 * Created by Derek on 11/15/2015.
 */
public class DataUtilities {

    public static ContentValues[] GetMovieContentValues(MovieData[] newMovies, String mostPop, String highestVote){

        int movieDataCount = newMovies.length;
        ContentValues movieValues;

        ContentValues[] returnContentValues = new ContentValues[movieDataCount];

        for (int i = 0; i < movieDataCount; i++) {
            movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, "false");
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, newMovies[i].getmMovieId());
            movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, newMovies[i].getmOverview());
            movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, newMovies[i].getmPosterPath());
            movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, newMovies[i].getmReleaseDate());
            movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, newMovies[i].getmTitle());
            movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, newMovies[i].getmRating());
            movieValues.put(MovieContract.MovieEntry.COLUMN_MOST_POPULAR, mostPop);
            movieValues.put(MovieContract.MovieEntry.COLUMN_HIGHEST_VOTE, highestVote);
            movieValues.put(MovieContract.MovieEntry.COLUMN_RECENT_ARRIVAL, "true");

            returnContentValues[i] = movieValues;
        }

        return returnContentValues;
    }
}
