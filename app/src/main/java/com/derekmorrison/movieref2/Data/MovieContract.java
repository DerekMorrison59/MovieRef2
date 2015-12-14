package com.derekmorrison.movieref2.Data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Derek on 11/13/2015.
 *
 * Defines Table and Column names to store movie data from The Movie Database (TMDB)
 *
 * The code for this class was largely taken from the Sunshine Version 2 project
 */
public class MovieContract {

    // The "Content authority" is a name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.derekmorrison.movieref2";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // these constants are used to identify the type of call to TMDB that was used to get
    // the movie record
    public static final String SORTED_BY_MOST_POPULAR = "MostPopular";
    public static final String SORTED_BY_HIGHEST_VOTE = "HighestVote";

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_MOVIE = "movie";
    public static final String PATH_QUERY_DATE = "query_date";


    /* Inner class that defines the table contents of the movie table */
    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";

        // TMDB movie ID
        public static final String COLUMN_MOVIE_ID = "id";

        // movie overview
        public static final String COLUMN_OVERVIEW = "overview";

        // release date
        public static final String COLUMN_RELEASE_DATE = "release_date";

        // movie poster path
        public static final String COLUMN_POSTER_PATH = "poster_path";

        // movie title
        public static final String COLUMN_TITLE = "title";

        // vote average (out of 10) - stored as a float
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";

        // favorite - boolean that indicates if the user has selected this movie as a 'favorite'
        // the default is 'false'
        public static final String COLUMN_FAVORITE = "favorite";

        // indicates the type of call to TMDB was 'MOST POPULAR' to get the movie record
        public static final String COLUMN_MOST_POPULAR = "most_popular";

        // indicates the type of call to TMDB that 'HIGHEST VOTE' to get the movie record
        public static final String COLUMN_HIGHEST_VOTE = "highest_vote";

        // Recent Arrival - boolean that indicates that this movie came from the most recent set of
        // movie data returned from TMDB (for the specified sort type)
        public static final String COLUMN_RECENT_ARRIVAL = "recent_arrival";

        // this method simply takes the movie ID and appends it to the standard URI to narrow
        // the selection to a single movie record
        public static Uri buildMovieUri(long movieID) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(movieID)).build();
        }

        public static long getMovieIDFromUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(1));
        }
    }

    /* Inner class that defines the table contents of the Query Date table */
    public static final class QueryDateEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_QUERY_DATE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUERY_DATE;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_QUERY_DATE;

        // Table name
        public static final String TABLE_NAME = "querydate";

        // indicates the type of call to TMDB that was used to get the movie record
        public static final String COLUMN_SORT_TYPE = "sort_type";

        // The location setting string is what will be sent to openweathermap
        // as the location query.
        public static final String COLUMN_QUERY_DATE = "last_query_date";


        public static Uri buildQueryDateUri(String sort_type) {
            return CONTENT_URI.buildUpon().appendPath(sort_type).build();
        }

        public static String getSortTypeFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
