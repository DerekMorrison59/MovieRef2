package com.derekmorrison.movieref2.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.derekmorrison.movieref2.Data.MovieContract;
import com.derekmorrison.movieref2.Data.MovieProvider;

/**
 * Created by Derek on 11/14/2015.
 *
 * Structure copied from Sunshine Version 2
 */
public class TestUriMatcher extends AndroidTestCase {

    private static final String MOVIE_QUERY = "76757";
    private static final long MOVIE_ID = 76757;

    private static final long TEST_DATE = 1419033600L;  // December 20th, 2014
    private static final long TEST_LOCATION_ID = 10L;

    //
    private static final Uri TEST_MOVIE_DIR = MovieContract.MovieEntry.CONTENT_URI;
    private static final Uri TEST_MOVIE_WITH_MOVIE_ID = MovieContract.MovieEntry.buildMovieUri(MOVIE_ID);

//    private static final Uri TEST_WEATHER_WITH_LOCATION_AND_DATE_DIR = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(LOCATION_QUERY, TEST_DATE);
//    // content://com.example.android.sunshine.app/location"
//    private static final Uri TEST_LOCATION_DIR = WeatherContract.LocationEntry.CONTENT_URI;

    /*
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The MOVIE URI was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_DIR), MovieProvider.MOVIE);

        assertEquals("Error: The MOVIE WITH MOVIE ID was matched incorrectly.",
                testMatcher.match(TEST_MOVIE_WITH_MOVIE_ID), MovieProvider.MOVIE_WITH_ID);
    }
}
