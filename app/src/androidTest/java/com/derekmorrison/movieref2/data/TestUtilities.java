package com.derekmorrison.movieref2.data;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.test.AndroidTestCase;

import com.derekmorrison.movieref2.Data.MovieContract;
import com.derekmorrison.movieref2.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by Derek on 11/13/2015.
 */
public class TestUtilities extends AndroidTestCase {

    static final long TEST_MOVIE_ID = 76757L;

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            String dbValue = valueCursor.getString(idx);

            // floats must be handled differently than strings (rounding issues like 7.0 becomes 7)
            if (valueCursor.getType(idx) == Cursor.FIELD_TYPE_FLOAT) {
                float dbValueFloat = Float.parseFloat(dbValue);
                float expValue = Float.parseFloat(expectedValue);
                float delta = (float)0.1;
                assertEquals("Error: Float value'" + dbValueFloat +
                        "' not within 0.1 of expected value '" +
                        expValue + "'  ", expValue, dbValueFloat, delta);
            } else {
                assertEquals("Value '" + dbValue +
                        "' did not match the expected value '" +
                        expectedValue + "'. " + error, expectedValue, dbValue);
            }
        }
    }

    public static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, "false");
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, 76757);
        movieValues.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, "In a universe where human genetic material is the most precious commodity, an impoverished young Earth woman becomes the key to strategic maneuvers and internal strife within a powerful dynasty…");
        movieValues.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, "/aMEsvTUklw0uZ3gk3Q6lAj6302a.jpg");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, "2015-03-20");
        movieValues.put(MovieContract.MovieEntry.COLUMN_TITLE, "Jupiter Ascending");
        movieValues.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, 5.4);
        movieValues.put(MovieContract.MovieEntry.COLUMN_MOST_POPULAR, "true");
        movieValues.put(MovieContract.MovieEntry.COLUMN_HIGHEST_VOTE, "false");
        movieValues.put(MovieContract.MovieEntry.COLUMN_RECENT_ARRIVAL, "true");

        return movieValues;
    }

    public static ContentValues createQueryDateValues() {

        // get the number of seconds since the last system boot
        int timeSeconds = (int) SystemClock.elapsedRealtime() / 1000;

        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieContract.QueryDateEntry.COLUMN_QUERY_DATE, timeSeconds);
        movieValues.put(MovieContract.QueryDateEntry.COLUMN_SORT_TYPE, MovieContract.SORTED_BY_MOST_POPULAR);
        return movieValues;
    }
    /*
        Students: The functions we provide inside of TestProvider use this utility class to test
        the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
