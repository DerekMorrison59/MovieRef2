package com.derekmorrison.movieref2;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.derekmorrison.movieref2.Data.MovieContract;
import com.derekmorrison.movieref2.rest.MovieCursorAdapter;

/**
 *
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>  {

    private MovieCursorAdapter mMovieCursorAdapter;

    public static final int MOVIE_LOADER = 0;
    private int mPosition = GridView.INVALID_POSITION;

    public static final String[] MOVIE_COLUMNS = {
        // In this case the id needs to be fully qualified with a table name, since
        // the content provider joins the location & weather tables in the background
        // (both have an _id column)
        // On the one hand, that's annoying.  On the other, you can search the weather table
        // using the location set by the user, which is only in the Location table.
        // So the convenience is worth it.
        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
        MovieContract.MovieEntry.COLUMN_MOVIE_ID,
        MovieContract.MovieEntry.COLUMN_OVERVIEW,
        MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
        MovieContract.MovieEntry.COLUMN_POSTER_PATH,
        MovieContract.MovieEntry.COLUMN_TITLE,
        MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE,
        MovieContract.MovieEntry.COLUMN_FAVORITE,
        MovieContract.MovieEntry.COLUMN_MOST_POPULAR,
        MovieContract.MovieEntry.COLUMN_HIGHEST_VOTE,
        MovieContract.MovieEntry.COLUMN_RECENT_ARRIVAL
    };

    private static final String[] QUERY_DATE_COLUMNS = {
        MovieContract.QueryDateEntry.TABLE_NAME + "." + MovieContract.QueryDateEntry._ID,
        MovieContract.QueryDateEntry.COLUMN_QUERY_DATE,
        MovieContract.QueryDateEntry.COLUMN_SORT_TYPE
    };

    public static final int COL_MOVIE_ROW_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_OVERVIEW = 2;
    public static final int COL_RELEASE_DATE = 3;
    public static final int COL_POSTER_PATH = 4;
    public static final int COL_TITLE = 5;
    public static final int COL_VOTE_AVERAGE = 6;
    public static final int COL_FAVORITE = 7;
    public static final int COL_MOST_POPULAR = 8;
    public static final int COL_HIGHEST_VOTE = 9;
    public static final int COL_RECENT_ARRIVAL = 10;

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* the savedInstanceState is being used to save / restore the following:
         *   - all movie data from the last call to TMDB
         *   - the state information of the GridView that holds the poster thumbnails
         *   - the string that is being displayed that indicates the sort method used
         *     when requesting the movie data
         */

        if (savedInstanceState != null) {
        }

        // this fragment should handle menu events.
        setHasOptionsMenu(true);
    }

    private void checkForNewInstall(){
        // if there are no date records then force an update
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // TAKEN FROM: http://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing
    public class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);



//        if (mSortedByText != null){
//            TextView tSort = (TextView)rootView.findViewById(R.id.selectedBytextView);
//            tSort.setText(mSortedByText);
//        }

//        // get a reference to the Retry Connection button and setup a listener
//        Button rButton = (Button) rootView.findViewById(R.id.retryButton);
//        rButton.setOnClickListener(new View.OnClickListener() {
//                                       public void onClick(View v) {
//                                           updateMovieList();
//                                       }
//                                   }
//        );


        // get a reference to the recycler view and attach the MovieCursorAdapter to it
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.poster_recycler_view);
        recyclerView.setLayoutManager(
                new GridLayoutManager(recyclerView.getContext(),2)
        );

        mMovieCursorAdapter = new MovieCursorAdapter(getActivity(), null);
        recyclerView.setAdapter(mMovieCursorAdapter);

        int NUM_COLUMNS = 2;
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE){
            NUM_COLUMNS = 3;
        }

        recyclerView.setLayoutManager(new GridLayoutManager(rootView.getContext(), NUM_COLUMNS));
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(rootView.getContext(), R.dimen.item_offset);
        recyclerView.addItemDecoration(itemDecoration);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        // there are several reasons why it might be time to update the database from TMDB
        if (Globals.getInstance().anyReasonToUpdate()){
            updateMovieList();
            updateLoader();
        }
    }

    public void updateLoader(){
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    public void updateMovieList() {

        final String LEGIT_MIN_VOTES = "50";
        //final String HIGHEST_RATED = "0";

        // read the current preferences stored on this device
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        String sort_by = sharedPref.getString(getString(R.string.pref_sort_order), "1");  // 1 = Most Popular, 0 = Highest Vote
        String api_key = sharedPref.getString("pref_api_key", "");


        // check the API Key stored in the preferences
        if (api_key.isEmpty() || api_key.length() != 32) {
            Globals.getInstance().setBadApiKey(true);
            showDialog();
            return;
        } else {
            Globals.getInstance().setBadApiKey(false);
        }

        // don't make the call to TMDB if we don't need to at this time
        if (RefreshNeeded(sort_by) == false) return;

        // get the parameters from the pref storage and pass them to the async thread that calls TMDB
        String[] pList = new String[3];
        pList[0] = sort_by;

        // according to The Movie Database, a movie needs at least 50 votes to be legitimately popular
        //pList[1] = min_count ? LEGIT_MIN_VOTES : NO_MIN_VOTES;
        pList[1] = LEGIT_MIN_VOTES;

        // private API Key issued by TMDB
        pList[2] = api_key;

        // create a new async task to fetch the movie data from TMDB and launch it
        FetchMoviesTask moviesTask = new FetchMoviesTask(this, this.getActivity());
        moviesTask.execute(pList);
    }

    private boolean RefreshNeeded(String sort_by) {

        boolean _RefreshNeeded = true;

        // special case - the current sort type (display type) is favorites
        // all the movie data needed is already in the database
        if (sort_by.equals("2")) {
            Globals.getInstance().setManualRefresh(false);
            return false;
        }

        // special case - user has requested a refresh
        if (Globals.getInstance().getManualRefresh()) {
            Globals.getInstance().setManualRefresh(false);
            return _RefreshNeeded;
        }

        String sort_type;
        String movieSortColumn;

        // first use the sort type according to the preferences
        if (sort_by.equals("0")) {
            movieSortColumn = MovieContract.MovieEntry.COLUMN_HIGHEST_VOTE;
            sort_type = MovieContract.SORTED_BY_HIGHEST_VOTE;
        } else {
            movieSortColumn = MovieContract.MovieEntry.COLUMN_MOST_POPULAR;
            sort_type = MovieContract.SORTED_BY_MOST_POPULAR;
        }

        String movieColumns = movieSortColumn + "=? " ;
        String[] movieSpecs = {"true"};

        // check for full list of movies in the database ( => 20 right now)
        Cursor movieCursor = getContext().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                movieColumns,
                movieSpecs,
                null);

        int movieCount = 0;
        if (movieCursor != null) {
            movieCount = movieCursor.getCount();
            movieCursor.close();
        }

        if (movieCount < 20) {
            return _RefreshNeeded;
        }

        String dateColumns = MovieContract.QueryDateEntry.COLUMN_SORT_TYPE + "=? " ;
        String[] dateSpecs = {sort_type};

        Cursor dateCursor = getContext().getContentResolver().query(
                MovieContract.QueryDateEntry.CONTENT_URI,
                null,
                dateColumns,
                dateSpecs,
                null);

        if (dateCursor != null) {

            // get the current time in minutes since the epoch
            long ctm = System.currentTimeMillis();
            int current_time = Utility.safeLongToInt(ctm / 60000);

            // was a date record found for this sort type?
            dateCursor.moveToFirst();
            if (dateCursor.getCount() == 0) {
                // no existing record, insert a record now
                ContentValues cv = new ContentValues(2);
                cv.put(MovieContract.QueryDateEntry.COLUMN_QUERY_DATE, current_time);
                cv.put(MovieContract.QueryDateEntry.COLUMN_SORT_TYPE, sort_type);
                getContext().getContentResolver().insert(MovieContract.QueryDateEntry.CONTENT_URI, cv);
                Globals.getInstance().setRefreshNeeded(true);
            } else {
                // how long since the last time we called TMDB for these 'sort_type' results
                int last_call_time = dateCursor.getInt(1);

                // number of minutes to wait before updating from TMDB (12 hours?) should be a setting
                int waitTime = 60 * 12;

                if ((current_time - last_call_time) < waitTime) {
                    // it has been less than the set time period, so don't bother calling TMDB
                    _RefreshNeeded = false;
                } else {
                    ContentValues cv = new ContentValues(1);
                    cv.put(MovieContract.QueryDateEntry.COLUMN_QUERY_DATE, current_time);

                    String whereClause = MovieContract.QueryDateEntry.COLUMN_SORT_TYPE + "=?";
                    String[] whereArgs = {sort_type};

                    // update the last call time stored in the database
                    getContext().getContentResolver().update(
                            MovieContract.QueryDateEntry.CONTENT_URI,
                            cv,
                            whereClause,
                            whereArgs);
                }
            }
            dateCursor.close();
        }
        return _RefreshNeeded;
    }

    // this method is used to inform the user that there is no proper API Key for TMDB stored in the prefs
    // the choices are to exit this app or enter the API key now via the settings activity
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle(R.string.no_key_title);
        //builder.setInverseBackgroundForced(true);
        builder.setPositiveButton(R.string.no_key_positive_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        dialog.dismiss();

                        // use an Intent to launch the SettingsActivity
                        // Adding these EXTRAs tells the O/S to skip over the HEADER page and go straight to the GeneralPreferenceFragment
                        Intent settingsIntent = new Intent(getActivity(), SettingsActivity.class);
                        settingsIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true);
                        settingsIntent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
                        startActivity(settingsIntent);
                    }
                });

        builder.setNegativeButton(R.string.no_key_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().finish();
                    }
                });

        builder.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // Sort order:
        String sortOrder = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";

        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;

        String movieColumns;
        String[] movieSettings;

        String sort_by = Utility.GetSortOrder(getContext());

        if (Globals.getInstance().getShowFavorites() || sort_by.equals("2")) {
            movieColumns = MovieContract.MovieEntry.COLUMN_FAVORITE + "=?";
            movieSettings = new String[] {"true"};
        } else {
            // ignore the setting of 'Favorite'
            String movieSortColumn;
            if (sort_by.equals("0")) {
                movieSortColumn = MovieContract.MovieEntry.COLUMN_HIGHEST_VOTE;
            } else {
                movieSortColumn = MovieContract.MovieEntry.COLUMN_MOST_POPULAR;
            }

            // show all the movies from the database for this 'sort type'
            // that are recent arrivals
            movieColumns = movieSortColumn + "=? AND " + MovieContract.MovieEntry.COLUMN_RECENT_ARRIVAL + "=?";
            movieSettings = new String[] {"true", "true"};
        }

        return new CursorLoader(getActivity(),
                movieUri,
                MOVIE_COLUMNS,
                movieColumns,
                movieSettings,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMovieCursorAdapter.swapCursor(null);
    }

}