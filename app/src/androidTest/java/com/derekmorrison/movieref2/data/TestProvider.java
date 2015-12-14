package com.derekmorrison.movieref2.data;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;

import com.derekmorrison.movieref2.Data.MovieContract;
import com.derekmorrison.movieref2.Data.MovieContract.MovieEntry;
import com.derekmorrison.movieref2.Data.MovieDbHelper;
import com.derekmorrison.movieref2.Data.MovieProvider;

/**
 * Created by Derek on 11/14/2015.
 * 
 * Taken from the Sunshine Version 2 project
 * 
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
     */
    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(
                MovieEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());
        cursor.close();

    }

    /*
        Student: Refactor this function to use the deleteAllRecordsFromProvider functionality once
        you have implemented delete functionality there.
     */
    public void deleteAllRecords() {
        deleteAllRecordsFromProvider();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    /*
        This test checks to make sure that the content provider is registered correctly.
     */
    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        // We define the component name based on the package name from the context and the
        // MovieProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MovieProvider.class.getName());
        try {
            // Fetch the provider info using the component name from the PackageManager
            // This throws an exception if the provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract.
            assertEquals("Error: MovieProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + MovieContract.CONTENT_AUTHORITY,
                    providerInfo.authority, MovieContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            // I guess the provider isn't registered correctly.
            assertTrue("Error: MovieProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    /*
            This test doesn't touch the database.  It verifies that the ContentProvider returns
            the correct type for each type of URI that it can handle.
            Students: Uncomment this test to verify that your implementation of GetType is
            functioning correctly.
         */
    public void testGetType() {
        // content://com.derekmorrison.movieref2/movie/
        String type = mContext.getContentResolver().getType(MovieEntry.CONTENT_URI);
        
        // vnd.android.cursor.dir/com.derekmorrison.movieref2/movie
        assertEquals("Error: the MovieEntry CONTENT_URI should return MovieEntry.CONTENT_TYPE",
                MovieEntry.CONTENT_TYPE, type);

        long movieId = 76757;
        
        // content://com.derekmorrison.movieref2/movie/76757
        type = mContext.getContentResolver().getType(
                MovieEntry.buildMovieUri(movieId));
        // vnd.android.cursor.dir/com.derekmorrison.movieref2/movie
        assertEquals("Error: the MovieEntry CONTENT_URI with movie ID should return MovieEntry.CONTENT_ITEM_TYPE",
                MovieEntry.CONTENT_ITEM_TYPE, type);

    }


    /*
        This test uses the database directly to insert and then uses the ContentProvider to
        read out the data.  Uncomment this test to see if the basic weather query functionality
        given in the ContentProvider is working correctly.
     */
    public void testBasicMovieQuery() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieValues = TestUtilities.createMovieValues();

        long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, movieValues);
        assertTrue("Unable to Insert MovieEntry into the Database", movieRowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieQuery", movieCursor, movieValues);
    }

    public void testBasicQueryDateQuery() {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // clear any existing db entries
        long rowId = db.delete(MovieContract.QueryDateEntry.TABLE_NAME, null, null);

        ContentValues queryDateValues = TestUtilities.createQueryDateValues();

        rowId = db.insert(MovieContract.QueryDateEntry.TABLE_NAME, null, queryDateValues);
        assertTrue("Unable to Insert QueryDateEntry into the Database", rowId != -1);

        db.close();

        // Test the basic content provider query
        Cursor queryDateCursor = mContext.getContentResolver().query(
                MovieContract.QueryDateEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        int dateCount = queryDateCursor.getCount();

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicQueryDateQuery", queryDateCursor, queryDateValues);



        String selectColumns = MovieContract.QueryDateEntry.COLUMN_SORT_TYPE + "=?";
        String[] selectParams = {MovieContract.SORTED_BY_MOST_POPULAR};

        queryDateCursor = mContext.getContentResolver().query(
                MovieContract.QueryDateEntry.CONTENT_URI,
                null,
                selectColumns,
                selectParams,
                null
        );

        queryDateCursor.moveToFirst();
        dateCount = queryDateCursor.getCount();

        int seconds = queryDateCursor.getInt(1);
        String sort = queryDateCursor.getString(2);

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicQueryDateQuery", queryDateCursor, queryDateValues);
    }

    public void testBasicMovieIDQuery() {
        // insert our test records into the database
        //MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        //SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues movieValues = TestUtilities.createMovieValues();

        //long movieRowId = db.insert(MovieEntry.TABLE_NAME, null, movieValues);
        //assertTrue("Unable to Insert MovieEntry into the Database", movieRowId != -1);

        //db.close();

        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, movieValues);



        String movieColumns = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] movieIds = {"76757"};

        // Test the basic content provider query
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                movieColumns,
                movieIds,
                null
        );

        // Make sure we get the correct cursor out of the database
        TestUtilities.validateCursor("testBasicMovieIDQuery", movieCursor, movieValues);
    }


    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the insert functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testInsertReadProvider() {

        ContentValues testValues = TestUtilities.createMovieValues();

        // Register a content observer for our insert.  This time, directly with the content resolver
        TestUtilities.TestContentObserver tco = TestUtilities.getTestContentObserver();

        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, tco);
        Uri movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        long movieRowId1 = ContentUris.parseId(movieUri);

        movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        long movieRowId2 = ContentUris.parseId(movieUri);
        movieUri = mContext.getContentResolver().insert(MovieEntry.CONTENT_URI, testValues);
        long movieRowId3 = ContentUris.parseId(movieUri);

        // Did our content observer get called?  Students:  If this fails, your insert location
        // isn't calling getContext().getContentResolver().notifyChange(uri, null);
        tco.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(tco);

        long movieRowId = ContentUris.parseId(movieUri);

        // Verify we got a row back.
        assertTrue(movieRowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // A cursor is your primary interface to the query results.
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestUtilities.validateCursor("testInsertReadProvider. Error validating LocationEntry.",
                cursor, testValues);

//        // A cursor is your primary interface to the query results.
//        Cursor weatherCursor = mContext.getContentResolver().query(
//                MovieEntry.CONTENT_URI,  // Table to Query
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null // columns to group by
//        );
//
//        TestUtilities.validateCursor("testInsertReadProvider. Error validating MovieEntry insert.",
//                weatherCursor, weatherValues);

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        //weatherValues.putAll(testValues);

//        // Get the joined Weather and Location data
//        weatherCursor = mContext.getContentResolver().query(
//                MovieEntry.buildWeatherLocation(TestUtilities.TEST_LOCATION),
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data.",
//                weatherCursor, weatherValues);
//
//        // Get the joined Weather and Location data with a start date
//        weatherCursor = mContext.getContentResolver().query(
//                MovieEntry.buildWeatherLocationWithStartDate(
//                        TestUtilities.TEST_LOCATION, TestUtilities.TEST_DATE),
//                null, // leaving "columns" null just returns all the columns.
//                null, // cols for "where" clause
//                null, // values for "where" clause
//                null  // sort order
//        );
//        TestUtilities.validateCursor("testInsertReadProvider.  Error validating joined Weather and Location Data with start date.",
//                weatherCursor, weatherValues);

        String movieColumns = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] movieIds = {"76757"};

        // Get the Movie data for a specific ID
        Cursor movieCursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null,
                movieColumns,
                movieIds,
                null
        );
        TestUtilities.validateCursor("testInsertReadProvider.  Error validating data for a specific movie ID.",
                movieCursor, testValues);
    }

    // Make sure we can still delete after adding/updating stuff
    //
    // Student: Uncomment this test after you have completed writing the delete functionality
    // in your provider.  It relies on insertions with testInsertReadProvider, so insert and
    // query functionality must also be complete before this test can be used.
    public void testDeleteRecords() {
        testInsertReadProvider();

        // Register a content observer for our movie delete.
        TestUtilities.TestContentObserver movieObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, movieObserver);

        deleteAllRecordsFromProvider();

        // Students: If either of these fail, you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in the ContentProvider
        // delete.  (only if the insertReadProvider is succeeding)
        movieObserver.waitForNotificationOrFail();

        mContext.getContentResolver().unregisterContentObserver(movieObserver);
    }


    static private final int BULK_INSERT_RECORDS_TO_INSERT = 4;

    static ContentValues[] createBulkInsertMovieValues() {

        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, "false");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, 76757);
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "In a universe where human genetic material is the most precious commodity, an impoverished young Earth woman becomes the key to strategic maneuvers and internal strife within a powerful dynasty…");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/aMEsvTUklw0uZ3gk3Q6lAj6302a.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-03-20");
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Jupiter Ascending");
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 5.4);
        movieValues.put(MovieEntry.COLUMN_RECENT_ARRIVAL, "false");
        movieValues.put(MovieEntry.COLUMN_MOST_POPULAR, "false");
        movieValues.put(MovieEntry.COLUMN_HIGHEST_VOTE, "true");
        returnContentValues[0] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, "true");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, 102899);
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "Armed with the astonishing ability to shrink in scale but increase in strength, con-man Scott Lang must embrace his inner-hero and help his mentor, Dr. Hank Pym, protect the secret behind his spectacular Ant-Man suit from a new generation of towering threats. Against seemingly insurmountable obstacles, Pym and Lang must plan and pull off a heist that will save the world.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/7SGGUiTE6oc2fh9MjIk5M00dsQd.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-07-17");
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Ant-Man");
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 7.0);
        movieValues.put(MovieEntry.COLUMN_RECENT_ARRIVAL, "true");
        movieValues.put(MovieEntry.COLUMN_MOST_POPULAR, "true");
        movieValues.put(MovieEntry.COLUMN_HIGHEST_VOTE, "false");
        returnContentValues[1] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, "true");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, 286217);
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "During a manned mission to Mars, Astronaut Mark Watney is presumed dead after a fierce storm and left behind by his crew. But Watney has survived and finds himself stranded and alone on the hostile planet. With only meager supplies, he must draw upon his ingenuity, wit and spirit to subsist and find a way to signal to Earth that he is alive.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/AjbENYG3b8lhYSkdrWwlhVLRPKR.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-10-02");
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "The Martian");
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 7.7);
        movieValues.put(MovieEntry.COLUMN_RECENT_ARRIVAL, "false");
        movieValues.put(MovieEntry.COLUMN_MOST_POPULAR, "true");
        movieValues.put(MovieEntry.COLUMN_HIGHEST_VOTE, "false");
        returnContentValues[2] = movieValues;

        movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, "false");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, 168259);
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "Deckard Shaw seeks revenge against Dominic Toretto and his family for his comatose brother.");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/dCgm7efXDmiABSdWDHBDBx2jwmn.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-04-03");
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Furious 7");
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 7.6);
        movieValues.put(MovieEntry.COLUMN_RECENT_ARRIVAL, "true");
        movieValues.put(MovieEntry.COLUMN_MOST_POPULAR, "false");
        movieValues.put(MovieEntry.COLUMN_HIGHEST_VOTE, "true");
        returnContentValues[3] = movieValues;

        return returnContentValues;
    }

    // Student: Uncomment this test after you have completed writing the BulkInsert functionality
    // in your provider.  Note that this test will work with the built-in (default) provider
    // implementation, which just inserts records one-at-a-time, so really do implement the
    // BulkInsert ContentProvider function.
    public void testBulkInsert() {

        // Now we can bulkInsert some movies.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, bulkInsertContentValues);

        // Students:  If this fails, it means that you most-likely are not calling the
        // getContext().getContentResolver().notifyChange(uri, null); in your BulkInsert
        // ContentProvider method.
        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        // test the number of records inserted
        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null
                //MovieEntry.COLUMN_VOTE_AVERAGE + " ASC"  // sort order == by VOTE ASCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), insertCount);

        String fav;
        long movieID;
        String overview;
        String poster;
        String release;
        String title;
        float vote;
        String sort;
        String recent;

        float voteTotal = 0;

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            movieID = cursor.getLong(1);
            overview = cursor.getString(2);
            poster = cursor.getString(3);
            release = cursor.getString(4);
            title = cursor.getString(5);
            vote = cursor.getFloat(6);
            fav = cursor.getString(7);
            sort = cursor.getString(8);
            recent = cursor.getString(9);

            voteTotal += vote;
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }
        cursor.close();
    }

    public void testSelectMostPopular() {

        // Now we can bulkInsert some movies.  In fact, we only implement BulkInsert for weather
        // entries.  With ContentProviders, you really only have to implement the features you
        // use, after all.
        ContentValues[] bulkInsertContentValues = createBulkInsertMovieValues();

        // Register a content observer for our bulk insert.
        TestUtilities.TestContentObserver weatherObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(MovieEntry.CONTENT_URI, true, weatherObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(MovieEntry.CONTENT_URI, bulkInsertContentValues);

        weatherObserver.waitForNotificationOrFail();
        mContext.getContentResolver().unregisterContentObserver(weatherObserver);

        // test the number of records inserted
        assertEquals(insertCount, BULK_INSERT_RECORDS_TO_INSERT);

        // request the records in the same order they were inserted
        Cursor cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null
                //MovieEntry.COLUMN_VOTE_AVERAGE + " DESC"  // sort order == by VOTE DESCENDING
        );

        // we should have as many records in the database as we've inserted
        assertEquals(cursor.getCount(), insertCount);

        String fav;
        long movieID;
        String overview;
        String poster;
        String release;
        String title;
        float vote;
        String sort;
        String recent;

        float voteTotal = 0;

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext() ) {
            movieID = cursor.getLong(1);
            overview = cursor.getString(2);
            poster = cursor.getString(3);
            release = cursor.getString(4);
            title = cursor.getString(5);
            vote = cursor.getFloat(6);
            fav = cursor.getString(7);
            sort = cursor.getString(8);
            recent = cursor.getString(9);

            voteTotal += vote;
            TestUtilities.validateCurrentRecord("testBulkInsert.  Error validating MovieEntry " + i,
                    cursor, bulkInsertContentValues[i]);
        }


        // get all 'Most Popular' movies that just arrived
        String movieColumns = MovieEntry.COLUMN_MOST_POPULAR + "=?" + " AND " + MovieEntry.COLUMN_RECENT_ARRIVAL + "=?";
        String[] movieIds = {"true", "true"};

        cursor = mContext.getContentResolver().query(
                MovieEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                movieColumns, // cols for "where" clause
                movieIds, // values for "where" clause
                MovieEntry.COLUMN_VOTE_AVERAGE + " DESC"  // sort order == by VOTE Descending
        );

        voteTotal = 0;

        // and let's make sure they match the ones we created
        cursor.moveToFirst();
        for ( int i = 0; i < cursor.getCount(); i++, cursor.moveToNext() ) {
            movieID = cursor.getLong(1);
            overview = cursor.getString(2);
            poster = cursor.getString(3);
            release = cursor.getString(4);
            title = cursor.getString(5);
            vote = cursor.getFloat(6);
            fav = cursor.getString(7);
            sort = cursor.getString(8);
            recent = cursor.getString(9);

            voteTotal += vote;
        }

        cursor.close();
    }
}
