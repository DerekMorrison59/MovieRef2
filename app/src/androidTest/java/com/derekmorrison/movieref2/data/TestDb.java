package com.derekmorrison.movieref2.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.test.AndroidTestCase;

import com.derekmorrison.movieref2.Data.MovieContract;
import com.derekmorrison.movieref2.Data.MovieDbHelper;

import java.util.HashSet;

/**
 * Created by Derek on 11/13/2015.
 */
public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    // this method creates a fresh new database and then tests it for correctness
    // first that all the tables were created
    // second that each table contains the correct columns
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.MovieEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.QueryDateEntry.TABLE_NAME);

        // make sure any old versions of the database are deleted before the test gets going
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);

        // use the helper method that instantiates a new empty Movie database
        SQLiteDatabase db = new MovieDbHelper(this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // query the system to get the name(s) of all 'user' tables
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        // there should be at least one table (MovieContract.MovieEntry.TABLE_NAME = movie)
        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain the correct list of tables
        assertTrue("Error: Your database was created without the correct list of tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for movie table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> movieColumnHashSet = new HashSet<String>();

        // use all the columns as defined in the MovieContract class
        movieColumnHashSet.add(MovieContract.MovieEntry._ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_FAVORITE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_TITLE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_RECENT_ARRIVAL);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_MOST_POPULAR);
        movieColumnHashSet.add(MovieContract.MovieEntry.COLUMN_HIGHEST_VOTE);

        // check to make sure that the table in the database has the exact same list of columns
        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            movieColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that the table doesn't contain all of the same columns
        assertTrue("Error: The table doesn't contain all of the required movie entry columns",
                movieColumnHashSet.isEmpty());




        c = db.rawQuery("PRAGMA table_info(" + MovieContract.QueryDateEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for queryDate table information.",
                c.moveToFirst());

        // Build a HashSet of all of the Query Date column names
        final HashSet<String> queryDateColumnHashSet = new HashSet<String>();
        queryDateColumnHashSet.add(MovieContract.QueryDateEntry._ID);
        queryDateColumnHashSet.add(MovieContract.QueryDateEntry.COLUMN_QUERY_DATE);
        queryDateColumnHashSet.add(MovieContract.QueryDateEntry.COLUMN_SORT_TYPE);

        // check to make sure that the table in the database has the exact same list of columns
        columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            queryDateColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that the table doesn't contain all of the same columns
        assertTrue("Error: The table doesn't contain all of the required Query Date columns",
                queryDateColumnHashSet.isEmpty());

        c.close();
        db.close();
    }

    /*
        This method does an insert, select all, select by movie ID and an update
     */
    public void testMovieTable() {

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create movie values
        ContentValues movieValues = TestUtilities.createMovieValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        // a return value of -1 means the insert failed
        long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);
        assertTrue(movieRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor movieCursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from movie query", movieCursor.moveToFirst());

        // Fifth Step: Validate the movie Query
        TestUtilities.validateCurrentRecord("testInsertReadDb movieEntry failed to validate",
                movieCursor, movieValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from movie query",
                movieCursor.moveToNext());


        // This test looks for a single record using a movie ID
        //movieCursor = db.rawQuery("SELECT * FROM movie WHERE id='76757'", null);
        long movie_id = 76757L;
        String movieColumns = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=?";
        String[] movieIds = {"76757"};

        movieCursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                movieColumns, // cols for "where" clause
                movieIds, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        assertTrue("Error: No Records returned from movie ID query", movieCursor.moveToFirst());

        // there is only one record. get the movie ID from the cursor
        long the_id = movieCursor.getLong(1);

        assertEquals("Error: Movie ID not found using movie ID Query", the_id, movie_id);

        assertFalse("Error: More than one record returned from movie ID query",
                movieCursor.moveToNext());


        // look for all records which have Favorite = false and update them to Favorite = true
        movieColumns = MovieContract.MovieEntry.COLUMN_FAVORITE + "=?";
        String[] movieFav = {"false"};
        ContentValues movieValues2 = new ContentValues();
        movieValues2.put(MovieContract.MovieEntry.COLUMN_FAVORITE, "true");

        int rowsUpdated = db.update(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                movieValues2, // leaving "columns" null just returns all the columns.
                movieColumns, // cols for "where" clause
                movieFav // values for "where" clause
        );

        assertEquals("Error: Number of rows updated should be 1", rowsUpdated, 1);

        movieCursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // uncomment this assertion to test the update. it's supposed to fail because the rows no
        // longer match after the update

//        assertTrue("Error: No Records returned from movie ID query", movieCursor.moveToFirst());
//        TestUtilities.validateCurrentRecord("update movieEntry failed to validate, which is correct!",
//                movieCursor, movieValues);

        // Sixth Step: Close cursor and database
        movieCursor.close();
        dbHelper.close();
    }

    public int bulkInsert(ContentValues[] values) {

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        // Sixth Step: Close database
        dbHelper.close();

        //getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    public void testDeleteNonFavMovies() {
        // First step: Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create movie values
        ContentValues[] movieValues = TestProvider.createBulkInsertMovieValues();

        // Third Step: Insert ContentValues into database
        int insertedRowCount = bulkInsert(movieValues);

        // delete all movies except the ones that have been marked as favorites
        String movieColumns = MovieContract.MovieEntry.COLUMN_FAVORITE + "=?";
        String[] favs = {"false"};

        int deleteCount = db.delete(
            MovieContract.MovieEntry.TABLE_NAME,    // Table to Query
            movieColumns,                           // cols for "where" clause
            favs                                    // values for "where" clause
        );


        assertFalse("Error: No Records deleted from movie database", deleteCount == 0);

        // Sixth Step: Close the database
        dbHelper.close();
    }

    public void testMovieFavorites() {

        // First step: Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create movie values
        ContentValues[] movieValues = TestProvider.createBulkInsertMovieValues();

        // Third Step: Insert ContentValues into database
        int insertedRowCount = bulkInsert(movieValues);

        //long movieRowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, movieValues);
        assertTrue(insertedRowCount == 4);

        // 76757
        String movieColumns = MovieContract.MovieEntry.COLUMN_FAVORITE + "=?";
        String[] favs = {"true"};

        Cursor movieCursor = db.query(
                MovieContract.MovieEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                movieColumns, // cols for "where" clause
                favs, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );


        assertTrue("Error: No Records returned from movie favorite query", movieCursor.moveToFirst());

        int retRec = movieCursor.getCount();
        assertTrue(retRec == 2);

        String mName = movieCursor.getString(5);
        assertEquals("Error: favorite movie name wrong", "Ant-Man", mName);

        movieCursor.moveToNext();
        mName = movieCursor.getString(5);
        assertEquals("Error: favorite movie name wrong", "The Martian", mName);

        //long the_id = movieCursor.getLong(3);

        //assertEquals("Error: Movie ID not found using movie ID Query", the_id, movie_id);

        //assertFalse("Error: More than one record returned from movie ID query",
        //        movieCursor.moveToNext());

        // Sixth Step: Close cursor and database
        movieCursor.close();
        dbHelper.close();
    }



    /*
    This method does an insert and select all
 */
    public void testQueryDateTable() {

        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Second Step: Create movie values
        ContentValues queryDateValues = TestUtilities.createQueryDateValues();

        // Third Step: Insert ContentValues into database and get a row ID back
        // a return value of -1 means the insert failed
        long movieRowId = db.insert(MovieContract.QueryDateEntry.TABLE_NAME, null, queryDateValues);
        assertTrue(movieRowId != -1);

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor queryDateCursor = db.query(
                MovieContract.QueryDateEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        // Move the cursor to the first valid database row and check to see if we have any rows
        assertTrue("Error: No Records returned from movie query", queryDateCursor.moveToFirst());

        // Fifth Step: Validate the movie Query
        TestUtilities.validateCurrentRecord("testInsertReadDb movieEntry failed to validate",
                queryDateCursor, queryDateValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse("Error: More than one record returned from movie query",
                queryDateCursor.moveToNext());

        // Sixth Step: Close cursor and database
        queryDateCursor.close();
        dbHelper.close();
    }
    /*
    This method does an insert and select all
 */
    public void testQueryDateTableUpdate() {

        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues queryDateValues = TestUtilities.createQueryDateValues();

        long movieRowId = db.insert(MovieContract.QueryDateEntry.TABLE_NAME, null, queryDateValues);
        assertTrue(movieRowId != -1);

        Cursor queryDateCursor = db.query(
                MovieContract.QueryDateEntry.TABLE_NAME,  // Table to Query
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null  // sort order
        );

        assertTrue("Error: No Records returned from movie query", queryDateCursor.moveToFirst());

        TestUtilities.validateCurrentRecord("testInsertReadDb movieEntry failed to validate",
                queryDateCursor, queryDateValues);

        assertFalse("Error: More than one record returned from movie query",
                queryDateCursor.moveToNext());



        // look for all records which have Favorite = false and update them to Favorite = true
        String queryDateColumns = MovieContract.QueryDateEntry.COLUMN_SORT_TYPE + "=?";
        String[] sortType = {MovieContract.SORTED_BY_MOST_POPULAR};
        ContentValues movieValues2 = new ContentValues();
        movieValues2.put(MovieContract.QueryDateEntry.COLUMN_QUERY_DATE, (int) SystemClock.elapsedRealtime() / 1000);

        int rowsUpdated = db.update(
                MovieContract.QueryDateEntry.TABLE_NAME,  // Table to Query
                movieValues2, // leaving "columns" null just returns all the columns.
                queryDateColumns, // cols for "where" clause
                sortType // values for "where" clause
        );

        assertEquals("Error: Number of rows updated should be 1", rowsUpdated, 1);





        queryDateCursor.close();
        dbHelper.close();
    }
}
