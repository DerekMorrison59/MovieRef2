package com.derekmorrison.movieref2.Data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Derek on 11/13/2015.
 *
 * This code largely taken from the Sunshine version 2 project
 */
public class MovieProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    public static final int MOVIE = 100;
    public static final int MOVIE_WITH_ID = 101;

    public static final int QUERY_DATE = 200;
    public static final int QUERY_DATE_WITH_ID = 201;


    private static final SQLiteQueryBuilder sMovieQueryBuilder;

    static{
        sMovieQueryBuilder = new SQLiteQueryBuilder();
    }

    // movie.movie_id = ?
    private static final String sMovieIdSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ? ";

    // query_date.sort_type = ?
    private static final String sQueryDateIdSelection =
            MovieContract.MovieEntry.TABLE_NAME+
                    "." + MovieContract.QueryDateEntry.COLUMN_SORT_TYPE + " = ? ";

    private Cursor getMovieById(Uri uri, String[] projection, String sortOrder) {
        long movieId = MovieContract.MovieEntry.getMovieIDFromUri(uri);

        String mId = String.valueOf(movieId);

        String[] selectionArgs;
        String selection;

        selection = sMovieIdSelection;
        selectionArgs = new String[]{mId};

        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getQueryDateById(Uri uri, String[] projection, String sortOrder) {

        String[] selectionArgs;
        String selection;

        selection = sQueryDateIdSelection;
        selectionArgs = new String[]{sortOrder};

        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    public static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_WITH_ID);

        matcher.addURI(authority, MovieContract.PATH_QUERY_DATE, QUERY_DATE);
        matcher.addURI(authority, MovieContract.PATH_QUERY_DATE + "/*", QUERY_DATE_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_WITH_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case QUERY_DATE_WITH_ID:
                return MovieContract.QueryDateEntry.CONTENT_ITEM_TYPE;
            case QUERY_DATE:
                return MovieContract.QueryDateEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            // "movie/*"
            case MOVIE_WITH_ID: {
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }
            // "movie"
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            // "query_date/*"
            case QUERY_DATE_WITH_ID: {
                retCursor = getQueryDateById(uri, projection, sortOrder);
                break;
            }
            // "query_date"
            case QUERY_DATE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.QueryDateEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
            break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    // this method returns the database ID for a given Movie ID
    // if the movie is not in the database then the return value is 0
    private long MovieExists(SQLiteDatabase db, long MovieId){
        long dbId = 0;
        String mId = String.valueOf(MovieId);
        String movieColumns = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] movieIds = {mId};

        Cursor c = db.query(MovieContract.MovieEntry.TABLE_NAME, null, movieColumns, movieIds, null, null,null);
        int retCount = 0;

        // make sure a cursor was returned
        if (c != null) {
            retCount = c.getCount();
            c.moveToFirst();
        }

        // make sure there is at least one record in the cursor
        if (retCount > 0){
            dbId = c.getLong(0);
        }
        return dbId;
    }

    // this method returns the database ID for a given Sort Type of Date
    // -- the last time this Sort Type was retrieved from TMDB
    // if there is no entry for this Sort Type then the return value is 0
    private long QueryDateExists(SQLiteDatabase db, String Sort_Type){
        long dbId = 0;
        String queryDateColumns = MovieContract.QueryDateEntry.COLUMN_SORT_TYPE + "=?";
        String[] queryDateSort = {Sort_Type};

        Cursor c = db.query(MovieContract.QueryDateEntry.TABLE_NAME, null, queryDateColumns, queryDateSort, null, null,null);
        int retCount = c.getCount();
        c.moveToFirst();

        if (retCount > 0){
            dbId = c.getLong(0);
        }
        return dbId;
    }

    /*

     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {

                // look to see if the movie is already in the database
                Long mID = values.getAsLong(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                String movie_ID = values.getAsString(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                //Long mID = 0L;
                //if (movie_ID != null && movie_ID.length() > 0) {
                //    mID = Long.getLong(movie_ID);
                //}

                // a return value of zero means the movie is not in the database and therefore it
                // should be added now
                long _id = MovieExists(db, mID);
                if (_id == 0) {
                    _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                } else {
                    String whereClause = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                    String[] whereArgs = {movie_ID};
                    _id = db.update(MovieContract.MovieEntry.TABLE_NAME, values, whereClause, whereArgs);
                }

                if ( _id > 0 )
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            case QUERY_DATE: {

                // look to see if the sort type is already in the database
                String sort_type = values.getAsString(MovieContract.QueryDateEntry.COLUMN_SORT_TYPE);

                // a return value of zero means the sort type is not in the database and therefore it
                // should be added now, otherwise update the existing record
                long _id = QueryDateExists(db, sort_type);
                if (_id == 0) {
                    _id = db.insert(MovieContract.QueryDateEntry.TABLE_NAME, null, values);
                } else {
                    String queryDateColumns = MovieContract.QueryDateEntry.COLUMN_SORT_TYPE + "=?";
                    String[] queryDateSort = {sort_type};
                    _id = db.update(MovieContract.QueryDateEntry.TABLE_NAME, values, queryDateColumns, queryDateSort);
                }


                if ( _id > 0 )
                    returnUri = MovieContract.QueryDateEntry.buildQueryDateUri(sort_type);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;

            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case QUERY_DATE:
                rowsDeleted = db.delete(
                        MovieContract.QueryDateEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case QUERY_DATE:
                rowsUpdated = db.update(MovieContract.QueryDateEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        // grab the movie id out of the current value object
                        long movieId = value.getAsLong(MovieContract.MovieEntry.COLUMN_MOVIE_ID);

                        // don't insert this movie if it is already in the database
                        long _id = MovieExists(db, movieId);
                        if (_id == 0) {
                            _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        } else {
                            // update the existing movie record to be a recent arrival
                            String movieColumns = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
                            String[] movieIdent = {String.valueOf(movieId)};

                            // set the update column to either Highest Vote or Most Popular based
                            // on the new TMDB record that is being processed
                            String most_pop = value.getAsString(MovieContract.MovieEntry.COLUMN_MOST_POPULAR);
                            String updateColumn = MovieContract.MovieEntry.COLUMN_HIGHEST_VOTE;
                            if (most_pop.equals("true")){
                                updateColumn = MovieContract.MovieEntry.COLUMN_MOST_POPULAR;
                            }

                            ContentValues cv = new ContentValues(1);
                            cv.put(MovieContract.MovieEntry.COLUMN_RECENT_ARRIVAL, "true");
                            cv.put(updateColumn, "true");
                            _id = db.update(MovieContract.MovieEntry.TABLE_NAME, cv, movieColumns, movieIdent);
                        }
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
