package com.derekmorrison.movieref2.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.derekmorrison.movieref2.Data.MovieContract.MovieEntry;
import com.derekmorrison.movieref2.Data.MovieContract.QueryDateEntry;

/**
 * Created by Derek on 11/13/2015.
 */
public class MovieDbHelper  extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_TABLE =
            "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +
            MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
            MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
            MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
            MovieEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
            MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
            MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
            MovieEntry.COLUMN_FAVORITE + " TEXT NOT NULL, " +
            MovieEntry.COLUMN_MOST_POPULAR + " TEXT NOT NULL, " +
            MovieEntry.COLUMN_HIGHEST_VOTE + " TEXT NOT NULL, " +
            MovieEntry.COLUMN_RECENT_ARRIVAL + " TEXT NOT NULL )";

        final String SQL_CREATE_QUERY_DATE_TABLE =
            "CREATE TABLE " + QueryDateEntry.TABLE_NAME + " (" +
            QueryDateEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            QueryDateEntry.COLUMN_QUERY_DATE + " INTEGER NOT NULL," +
            QueryDateEntry.COLUMN_SORT_TYPE + " TEXT NOT NULL)" ;

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_QUERY_DATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + QueryDateEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

        // what we need to do here is keep all entries in the movie table where favorite = true

        // maybe rename old table (MovieTemp)
        // delete all "non-fav" records,
        // create new table ( MovieEntry.TABLE_NAME )
        // and then copy all records from old to new?
    }
}
