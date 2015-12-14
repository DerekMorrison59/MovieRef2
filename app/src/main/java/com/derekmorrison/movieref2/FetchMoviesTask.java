package com.derekmorrison.movieref2;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.derekmorrison.movieref2.Data.DataUtilities;
import com.derekmorrison.movieref2.Data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Moved to a separate file by Derek on 11/15/2015.
 */
// asynchronus task to do the http request / response to get the movie data
public class FetchMoviesTask extends AsyncTask<String, Void, MovieData[]> {

    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

    private String units = "2";

    private Fragment mFragment;
    private FragmentActivity mFragActivity;
    private MovieData[] mResult;

    public FetchMoviesTask(Fragment fragment, FragmentActivity mainActivity){
        mFragActivity = mainActivity;
        //mMovieAdapter = movieAdapter;
        mFragment = fragment;
    }


    // sometimes a field from The Movie Database can come back containing the word 'null'
    // it is more user friendly to show 'N/A' for the string instead of 'null'
    private String ReplaceNull(String in){
        if (in.equals("null")){
            in = mFragActivity.getResources().getString(R.string.not_available_NA);
        }
        return in;
    }

    /**
     * This code was mostly copied from the UDACITY Sunshine project
     *
     * Take the String representing the movies in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private MovieData[] getMovieDataFromJson(String movieDatabaseJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String TMDB_RESULTS = "results";
        final String TMDB_TITLE = "title"; //"original_title";  use title because it's more likely English
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_POSTER_PATH = "poster_path";
        final String TMDB_VOTE = "vote_average";
        final String TMDB_DESCRIPTION = "overview";
        final String TMDB_MOVIE_ID = "id";

        JSONObject forecastJson = new JSONObject(movieDatabaseJsonStr);
        JSONArray movieArray = forecastJson.getJSONArray(TMDB_RESULTS);

        int numMovies = movieArray.length();

        MovieData[] movieDatas = new MovieData[numMovies];

        for(int i = 0; i < movieArray.length(); i++) {

            // Get the JSON object representing one movie
            JSONObject movieInfo = movieArray.getJSONObject(i);

            // replace the word 'null' with 'N/A', it's a little more user friendly
            // while copying the fields we want into a new MovieData instance
            movieDatas[i] = new MovieData(
                    ReplaceNull(movieInfo.getString(TMDB_TITLE)),
                    ReplaceNull(movieInfo.getString(TMDB_RELEASE_DATE)),
                    ReplaceNull(movieInfo.getString(TMDB_POSTER_PATH)),
                    ReplaceNull(movieInfo.getString(TMDB_VOTE)),
                    ReplaceNull(movieInfo.getString(TMDB_DESCRIPTION)),
                    ReplaceNull(movieInfo.getString(TMDB_MOVIE_ID)));
        }
        return movieDatas;
    }

    @Override
    protected MovieData[] doInBackground(String... params) {

        // all of these strings are currently defined by the movie database
        final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/discover/movie";
        final String SORT_PARAM = "sort_by";
        final String VOTE_COUNT = "vote_count.gte";
        final String API_KEY = "api_key";

        final String SORT_BY_POPULAR = "popularity.desc";
        final String SORT_BY_VOTE = "vote_average.desc";

        // Check for working internet connection and remember the status
        boolean connectionOK = hasActiveInternetConnection(mFragActivity.getApplicationContext());
        Globals.getInstance().setDataConnection(connectionOK);

        // no data connection means there is no point in trying to call TMDB
        if (!connectionOK) {
            return null;
        }

        // default sort order is "Most Popular"
        String sort = SORT_BY_POPULAR;

        // sort order can be by vote score
        if (params[0].equals("0"))
            sort = SORT_BY_VOTE;

        String count_minimum = params[1];
        if (count_minimum.isEmpty())
            count_minimum = "0";

        // if there is no API Key for TMDB then there is no point in calling TMDB
        // todo add more advanced tests for the API Key?
        String apiKey = params[2];
        if (apiKey.isEmpty())
            return null;

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieDataJsonStr = null;

        try {
            // Construct the URL for the The Movie Database query
            Uri builtUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, sort)
                    .appendQueryParameter(VOTE_COUNT, count_minimum)
                    .appendQueryParameter(API_KEY, apiKey)
                    .build();

            URL url = new URL(builtUri.toString());

            // open the connection and request the data
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();

            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            movieDataJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attemping
            // to parse it.
            Globals.getInstance().setBadApiKey(true);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            // json data has been collected, now convert it into MovieData objects
            return getMovieDataFromJson(movieDataJsonStr, 0);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the movie data.
        return null;
    }


    /*
     * This code was lifted from a couple of examples found on StackOverflow

     * this method checks for a data connection and then attempts to 'ping' a google server
     * with a very lightweight request / response

     * if the '204' request is successful then 'true' is returned to the caller
     * meaning that the network is available and functioning
     */
    public boolean hasActiveInternetConnection(Context context) {
        if (Utility.isNetworkAvailable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL(mFragActivity.getString(R.string.google_204_url)).openConnection());
                urlc.setRequestProperty("User-Agent", "Android");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 204 &&
                        urlc.getContentLength() == 0);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(LOG_TAG, "No network available!");
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onPostExecute(MovieData[] result) {

        int errorMsgVisibility = View.GONE;
        String errMessageString = mFragActivity.getResources().getString(R.string.no_internet_connection);

        // clear out all old movie data
        //mMovieAdapter.clear();

        if (result != null) {

            // reset the flag to avoid unnecessary updates
            Globals.getInstance().setRefreshNeeded(false);

            String sort_by = "1";
            sort_by = Utility.GetSortOrder(mFragActivity.getApplicationContext());

            // never delete a favorite
            String mostPop = "false";
            String highestVote = "false";

            String movieSortColumn;
            String movieFavoriteColumn = MovieContract.MovieEntry.COLUMN_FAVORITE;
            String movieColumns;
            String[] movieSpecs = {"true", "false"};

            if (sort_by.equals("0")) {
                highestVote = "true";
                movieSortColumn = MovieContract.MovieEntry.COLUMN_HIGHEST_VOTE;
            } else {
                mostPop = "true";
                movieSortColumn = MovieContract.MovieEntry.COLUMN_MOST_POPULAR;
            }

            movieColumns = movieSortColumn+ "=? " +" AND " + movieFavoriteColumn + "=?";

            // delete all non-favorites of this 'sort-type' in the database
            mFragActivity.getApplicationContext().getContentResolver().delete(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieColumns,
                    movieSpecs
            );

            // mark any remaining records of this sort type with 'recent arrival' == false
            movieColumns = movieSortColumn + "=?";
            String[] movieSpecs2 = {"true"};
            ContentValues movieValues = new ContentValues();
            movieValues.put(MovieContract.MovieEntry.COLUMN_RECENT_ARRIVAL, "false");

            mFragActivity.getApplicationContext().getContentResolver().update(
                    MovieContract.MovieEntry.CONTENT_URI,
                    movieValues,
                    movieColumns,
                    movieSpecs2
            );

            // insert new movie data into the database
            int insertCount = mFragActivity.getApplicationContext().getContentResolver().bulkInsert(
                    MovieContract.MovieEntry.CONTENT_URI, DataUtilities.GetMovieContentValues(result, mostPop, highestVote));
        } else {

            // the saved data is no longer valid
            mResult = null;
            //mSortedByText = null;
            Globals.getInstance().setRefreshNeeded(true);

            // show error message to the user
            errorMsgVisibility = View.VISIBLE;

                /*
                 * There are 2 main reasons that the 'result' can be null
                 * - the data connection is not working
                 * - the API Key is bad
                 *
                 *  It's possible that The Movie Database is not available or broken....
                 */
            if (Globals.getInstance().getDataConnection()){
                errMessageString = mFragActivity.getResources().getString(R.string.error_check_API_Key);
            }
        }

        if (mFragment == null){ return; }

        Button retry = null;
        TextView errorMsgTextView = null;
        View thisView = mFragment.getView();
        mFragActivity.getCurrentFocus();

        if (thisView != null) {
            retry = (Button) thisView.findViewById(R.id.retryButton);
            errorMsgTextView = (TextView) thisView.findViewById(R.id.errorTMDB_TextView);
        }

        // the TextView is only visible when there is an error condition
        if (errorMsgTextView != null) {
            errorMsgTextView.setText(errMessageString);
            errorMsgTextView.setVisibility(errorMsgVisibility);
        }

        // the retry button is only visability when the problem is the internet connection
        if (retry != null) {
            int buttonVisibility = View.GONE;
            if (!Globals.getInstance().getDataConnection())
                buttonVisibility = View.VISIBLE;
            retry.setVisibility(buttonVisibility);
        }
    }
}
