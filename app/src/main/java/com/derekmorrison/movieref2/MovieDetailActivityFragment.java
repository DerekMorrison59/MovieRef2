package com.derekmorrison.movieref2;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.derekmorrison.movieref2.Data.MovieContract;
import com.squareup.picasso.Picasso;

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
 *
 */
public class MovieDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int MOVIE_DETAIL_LOADER = 1;
    private MovieTrailer mMovieTrailer = null;
    private ShareActionProvider mShareActionProvider;
    private String mMovieId;
    private String mMovieTitle;
    private View mRootView;

    public MovieDetailActivityFragment() {
        setHasOptionsMenu(true);
        mMovieTrailer = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_movie_detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mMovieTrailer != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    // the share intent code was taken from the Sunshine 2 project
    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);

        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this trailer for " + mMovieTitle +" : " + mMovieTrailer.getmName());
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovieTrailer.getURL());
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                // User chose the "Share" item, use a share intent to send the first trailer link
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        mMovieId = "11"; // original Star Wars
        Bundle bun = getActivity().getIntent().getExtras();
        if (bun != null) {
            // the intent passes the data this way
            mMovieId = bun.getString(getString(R.string.movie_id_param));
        } else {
            bun = this.getArguments();
            if (bun != null) {
                // if we are in two pane mode then the movie ID is passed via the arguments
                mMovieId = bun.getString(getString(R.string.movie_id_param), "11");
            }
        }

        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        if (mMovieId != null) {
            Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;

            // there should only be one record in the database with this ID
            final String selectColumns = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=? ";
            final String[] movieSpecs = {mMovieId};

            return new CursorLoader(getActivity(),
                    movieUri,
                    MainActivityFragment.MOVIE_COLUMNS,
                    selectColumns,
                    movieSpecs,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor movieCursor) {
        if (movieCursor != null && movieCursor.getCount() > 0) {
            movieCursor.moveToFirst();

            // find and populate the various widgets with data from the MovieData object
            TextView title = (TextView) mRootView.findViewById(R.id.movie_title_textview);
            mMovieTitle = movieCursor.getString(MainActivityFragment.COL_TITLE);
            title.setText(mMovieTitle);

            TextView rating_prefix = (TextView) mRootView.findViewById(R.id.movie_rating_prefix_textview);
            rating_prefix.setText(getString(R.string.rating_prefix));

            TextView rating = (TextView) mRootView.findViewById(R.id.movie_rating_textview);
            rating.setText(movieCursor.getString(MainActivityFragment.COL_VOTE_AVERAGE));

            TextView releasePrefix = (TextView) mRootView.findViewById(R.id.movie_release_prefix_textview);
            releasePrefix.setText(getString(R.string.date_prefix));

            String relDate = movieCursor.getString(MainActivityFragment.COL_RELEASE_DATE);

            TextView releaseYear = (TextView) mRootView.findViewById(R.id.movie_release_year_textview);
            if (releaseYear != null && relDate.length() > 4) {
                releaseYear.setText(relDate.substring(0, 4));
            }

            TextView releaseDate = (TextView) mRootView.findViewById(R.id.movie_release_textview);
            releaseDate.setText(relDate);

            TextView overView = (TextView) mRootView.findViewById(R.id.movie_overview_textview);
            overView.setText(movieCursor.getString(MainActivityFragment.COL_OVERVIEW));

            String isFav = movieCursor.getString(MainActivityFragment.COL_FAVORITE);
            ImageView favImageView = (ImageView) mRootView.findViewById(R.id.favoriteImageView);

            // set up the code to toggle the favorite and change the image to match
            favImageView.setOnClickListener(
                    new View.OnClickListener() {
                        public void onClick(View view) {
                            final String selectColumns = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=? ";
                            final String[] movieSpecs = {mMovieId};

                            // get the current value from the database
                            Cursor movieCursor = getContext().getContentResolver().query(
                                    MovieContract.MovieEntry.CONTENT_URI,
                                    null,
                                    selectColumns,
                                    movieSpecs,
                                    null);

                            if (movieCursor == null || movieCursor.getCount() == 0) {
                                return;
                            }

                            movieCursor.moveToFirst();
                            String isFavNow = movieCursor.getString(MainActivityFragment.COL_FAVORITE);

                            // update the record by setting favorite to the opposite
                            String newFav = "true";
                            if (isFavNow.equals("true")) {
                                newFav = "false";
                            }

                            ContentValues movieValues = new ContentValues();
                            movieValues.put(MovieContract.MovieEntry.COLUMN_FAVORITE, newFav);

                            //String selectColumns = MovieContract.MovieEntry.COLUMN_MOVIE_ID + "=? ";
                            //String[] movieSpecs = {mMovieId};

                            getContext().getContentResolver().update(
                                    MovieContract.MovieEntry.CONTENT_URI,
                                    movieValues,
                                    selectColumns,
                                    movieSpecs
                            );

                            // update the image to match the new setting
                            ImageView favImageView = (ImageView) view.findViewById(R.id.favoriteImageView);

                            int favID = R.drawable.gray_favorite;
                            if (newFav.equals("true")) {
                                favID = R.drawable.color_favorite;
                            }

                            Picasso
                                    .with(view.getContext())
                                    .load(favID)
                                    .into(favImageView);

                            movieCursor.close();
                        }
                    }
            );

            // set the icon to gray scale  if not a favorite and full color if it is a favorite
            int favID = R.drawable.gray_favorite;
            if (isFav.equals("true")) {
                favID = R.drawable.color_favorite;
            }

            Picasso
                    .with(mRootView.getContext())
                    .load(favID)
                    .into(favImageView);

            ImageView imageView = (ImageView) mRootView.findViewById(R.id.movie_poster_imageview);

            // make it the same as the main gridview image (reduce download)
            final String TMDB_BASE = "http://image.tmdb.org/t/p/";
            final String TMDB_IMAGE_SIZE = "w185";

            if (movieCursor.getString(MainActivityFragment.COL_POSTER_PATH).equals(getResources().getString(R.string.not_available_NA))) {

                // load the "Image Not Available" image because there is no poster on TMDB website
                Picasso
                        .with(mRootView.getContext())
                        .load(R.drawable.image_not_avail)
                        .error(R.drawable.error_image_not_loaded)
                        .into(imageView);
            } else {

                // create the URL for the image
                StringBuilder imageURL = new StringBuilder(TMDB_BASE);
                imageURL.append(TMDB_IMAGE_SIZE);
                imageURL.append(movieCursor.getString(MainActivityFragment.COL_POSTER_PATH));

                // use the Picasso library to do image handling
                Picasso
                        .with(mRootView.getContext())
                        .load(imageURL.toString())
                        .error(R.drawable.error_image_not_loaded)  // Picasso was unable to download the poster image
                        .into(imageView);
            }

            getTrailers(mMovieId);
            getReviews(mMovieId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //mMovieCursorAdapter.swapCursor(null);
    }

//    public void setUseTodayLayout(boolean useTodayLayout) {
//        mUseTodayLayout = useTodayLayout;
//        if (mForecastAdapter != null) {
//            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
//        }
//    }

    private void getTrailers(String movieID) {

        if (Utility.isNetworkAvailable(getContext()) == false){

            TextView trailerLabel = (TextView) mRootView.findViewById(R.id.movie_trailer_textview);
            trailerLabel.setText(R.string.no_data_connection);
            return;
        }

        // read the current preferences stored on this device
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        String api_key = sharedPref.getString("pref_api_key", "");

        // check the API Key stored in the preferences, at this point just use empty vs. not empty
        if (api_key.isEmpty()) {
            return;
        }

        // get the parameters from the pref storage and pass them to the async thread that calls TMDB
        String[] pList = new String[2];
        pList[0] = movieID;

        // private API Key issued by TMDB
        pList[1] = api_key;

        // create a new async task to fetch the movie data from TMDB and launch it
        FetchMovieTrailersTask trailersTask = new FetchMovieTrailersTask();
        trailersTask.execute(pList);
    }


    // asynchronus task to do the http request / response to get the movie trailer URLs
    public class FetchMovieTrailersTask extends AsyncTask<String, Void, MovieTrailer[]> {

        private final String LOG_TAG = FetchMovieTrailersTask.class.getSimpleName();

        /**
         * This code was mostly copied from the UDACITY Sunshine project
         * <p/>
         * Take the String representing the movies in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private MovieTrailer[] getMovieDataFromJson(String movieDatabaseJsonStr)
                throws JSONException {

            // 	{"id":"5474d2339251416e58002ae1","iso_639_1":"en","key":"RFinNxS5KN4","name":"Official Trailer","site":"YouTube","size":1080,"type":"Trailer"}

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_KEY = "key";
            final String TMDB_NAME = "name";
            final String TMDB_TYPE = "type";
            final String TMDB_SITE = "site";

            JSONObject forecastJson = new JSONObject(movieDatabaseJsonStr);
            JSONArray trailerArray = forecastJson.getJSONArray(TMDB_RESULTS);

            int numTrailers = trailerArray.length();
            MovieTrailer[] movieTrailers = new MovieTrailer[numTrailers];

            for (int i = 0; i < numTrailers; i++) {

                // Get the JSON object representing one movie
                JSONObject movieInfo = trailerArray.getJSONObject(i);

                // MovieTrailer(String mName, String mKey, String mSite, String mType)
                // copy the movie data into a new MovieTrailer object
                movieTrailers[i] = new MovieTrailer(
                        movieInfo.getString(TMDB_NAME),
                        movieInfo.getString(TMDB_KEY),
                        movieInfo.getString(TMDB_SITE),
                        movieInfo.getString(TMDB_TYPE)
                );
            }
            return movieTrailers;
        }

        @Override
        protected MovieTrailer[] doInBackground(String... params) {

            // all of these strings are currently defined by the movie database
            // https://api.themoviedb.org/3/movie/135397/videos?api_key=02c414cf4d7c0aa93e8987c708414571
            final String TMDB_URL_1 = "https://api.themoviedb.org/3/movie/";
            final String TMDB_URL_2 = "/videos";
            final String API_KEY = "api_key";

            // no data connection means there is no point in trying to call TMDB
            if (Utility.isNetworkAvailable(getContext()) == false) {
                return null;
            }

            // if there is no API Key for TMDB then there is no point in calling TMDB
            String apiKey = params[1];
            if (apiKey.isEmpty())
                return null;

            String movieID = params[0];
            if (movieID.isEmpty())
                return null;

            String trailerURL = TMDB_URL_1 + movieID + TMDB_URL_2;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieDataJsonStr = null;

            try {
                // Construct the URL for the The Movie Database query
                Uri builtUri = Uri.parse(trailerURL).buildUpon()
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
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
                return getMovieDataFromJson(movieDataJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movie data.
            return null;
        }

        @Override
        protected void onPostExecute(MovieTrailer[] result) {

            View thisView = getView();
            if (thisView == null) return;

            LinearLayout trailerLayout = (LinearLayout) thisView.findViewById(R.id.movie_trailer_layout);
            int childCount = trailerLayout.getChildCount();

            // do not reload the trailer links when the user clicks on the favorite icon (star)
            // do not count the header for the review layout
            if (childCount > 1) {
                return;
            }

            TextView trailerLabel = (TextView) thisView.findViewById(R.id.movie_trailer_textview);
            trailerLabel.setText(R.string.label_for_trailers);

            String trailerURL = "https://www.youtube.com/watch?v=DLzxrzFCyOs";
            String trailerName = "Click for Rick";

            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lParams.setMargins(16, 0, 16, 32);

            LinearLayout.LayoutParams movieBoxParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.HORIZONTAL
            );
            movieBoxParams.setMargins(16, 0, 16, 32);

            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            ivParams.gravity = Gravity.CENTER_VERTICAL;
            //ivParams.setMargins(16,0,0,0);

            if (result != null && result.length > 0) {

                int linkCount = result.length;

                for (int i=0; i < linkCount; i++){
                    MovieTrailer movieTrailer = result[i];
                    trailerURL = movieTrailer.getURL();
                    trailerName = movieTrailer.getmName();

                    if (mMovieTrailer == null){
                        mMovieTrailer = movieTrailer;
                    }

                    LinearLayout movieBox = new LinearLayout(getContext());
                    movieBox.setLayoutParams(movieBoxParams);
                    movieBox.setBackgroundColor(Color.parseColor("#ff009688"));

                    ImageView iv = new ImageView(getContext());
                    iv.setImageResource(R.drawable.utubeplay);
                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    iv.setPadding(16,24,0,0);
                    //iv.setMinimumHeight();


                    TextView newTV = new TextView(getContext());
                    newTV.setLayoutParams(lParams);
                    //newTV.setHeight(30);
                    newTV.setPadding(16, 32, 0, 32);
                    newTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    //newTV.setBackgroundColor(Color.parseColor("#80d0d0"));
                    newTV.setText(trailerName);

                    final String _url = trailerURL;

                    movieBox.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View view) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(_url)));
                                }
                            }
                    );

                    movieBox.addView(iv);
                    movieBox.addView(newTV);
                    trailerLayout.addView(movieBox);
                }
            } else {
                trailerLabel.setText(R.string.label_for_trailers);

            }


            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null && mMovieTrailer != null) {
                mShareActionProvider.setShareIntent(createShareForecastIntent());
            }
        }
    }

    private void getReviews(String movieID) {

        if (Utility.isNetworkAvailable(getContext()) == false){

            TextView reviewLabel = (TextView) mRootView.findViewById(R.id.movie_review_textview);
            reviewLabel.setText(R.string.no_data_connection);
            return;
        }

        // read the current preferences stored on this device
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        String api_key = sharedPref.getString("pref_api_key", "");

        // check the API Key stored in the preferences, at this point just use empty vs. not empty
        if (api_key.isEmpty()) {
            return;
        }

        // get the parameters from the pref storage and pass them to the async thread that calls TMDB
        String[] pList = new String[2];
        pList[0] = movieID;

        // private API Key issued by TMDB
        pList[1] = api_key;

        // create a new async task to fetch the movie data from TMDB and launch it
        FetchMovieReviewsTask trailersTask = new FetchMovieReviewsTask();
        trailersTask.execute(pList);
    }


    // asynchronus task to do the http request / response to get the movie trailer URLs
    public class FetchMovieReviewsTask extends AsyncTask<String, Void, MovieReview[]> {

        private final String LOG_TAG = FetchMovieReviewsTask.class.getSimpleName();

        /**
         * This code was mostly copied from the UDACITY Sunshine project
         * <p/>
         * Take the String representing the movies in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private MovieReview[] getMovieDataFromJson(String movieDatabaseJsonStr)
                throws JSONException {

            // 	{"id":"55910381c3a36807f900065d","author":"jonlikesmoviesthatdontsuck",
            //     "content":"I was a huge fan of the original 3 movies, they were out when I was younger, and I grew up loving dinosaurs because of them. This movie was awesome, and I think it can stand as a testimonial piece towards the capabilities that Christopher Pratt has. He nailed it. The graphics were awesome, the supporting cast did great and the t rex saved the child in me. 10\\5 stars, four thumbs up, and I hope that star wars episode VII doesn't disappoint,",
            //     "url":"http://j.mp/1GHgSxi"}

            // These are the names of the JSON objects that need to be extracted.
            final String TMDB_RESULTS = "results";
            final String TMDB_AUTHOR = "author";
            final String TMDB_CONTENT = "content";
            final String TMDB_URL = "url";

            JSONObject forecastJson = new JSONObject(movieDatabaseJsonStr);
            JSONArray trailerArray = forecastJson.getJSONArray(TMDB_RESULTS);

            int numTrailers = trailerArray.length();
            MovieReview[] MovieReviews = new MovieReview[numTrailers];

            for (int i = 0; i < numTrailers; i++) {

                // Get the JSON object representing one movie
                JSONObject movieInfo = trailerArray.getJSONObject(i);

                // MovieReview(String mAuthor, String mContent, String mURL)
                // copy the movie data into a new MovieReview object
                MovieReviews[i] = new MovieReview(
                        movieInfo.getString(TMDB_AUTHOR),
                        movieInfo.getString(TMDB_CONTENT),
                        movieInfo.getString(TMDB_URL)
                );
            }
            return MovieReviews;
        }

        @Override
        protected MovieReview[] doInBackground(String... params) {

            // all of these strings are currently defined by the movie database
            // https://api.themoviedb.org/3/movie/135397/videos?api_key=02c414cf4d7c0aa93e8987c708414571
            final String TMDB_URL_1 = "https://api.themoviedb.org/3/movie/";
            final String TMDB_URL_2 = "/reviews";
            final String API_KEY = "api_key";

            // no data connection means there is no point in trying to call TMDB
            if (Utility.isNetworkAvailable(getContext()) == false) {
                return null;
            }

            // if there is no API Key for TMDB then there is no point in calling TMDB
            String apiKey = params[1];
            if (apiKey.isEmpty())
                return null;

            String movieID = params[0];
            if (movieID.isEmpty())
                return null;

            String trailerURL = TMDB_URL_1 + movieID + TMDB_URL_2;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieDataJsonStr = null;

            try {
                // Construct the URL for the The Movie Database query
                Uri builtUri = Uri.parse(trailerURL).buildUpon()
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
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
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
                return getMovieDataFromJson(movieDataJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the movie data.
            return null;
        }


        @Override
        protected void onPostExecute(MovieReview[] result) {

            View thisView = getView();
            if (thisView == null) return;

            LinearLayout reviewLayout = (LinearLayout) thisView.findViewById(R.id.movie_review_layout);
            int childCount = reviewLayout.getChildCount();

            // don't load the movie reviews more than once
            if (childCount > 1){
                return;
            }

            TextView reviewLabel = (TextView) thisView.findViewById(R.id.movie_review_textview);
            reviewLabel.setText(R.string.movie_review_label);

            String trailerURL = "https://www.youtube.com/watch?v=DLzxrzFCyOs";
            String trailerName = "Click for Rick";

            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lParams.setMargins(16, 0, 16, 32);

            LinearLayout.LayoutParams movieBoxParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.HORIZONTAL
            );
            movieBoxParams.setMargins(16, 0, 16, 32);

            LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            ivParams.gravity = Gravity.CENTER_VERTICAL;
            //ivParams.setMargins(16,0,0,0);

            if (result != null && result.length > 0) {

                int linkCount = result.length;

                for (int i=0; i < linkCount; i++){
                    MovieReview movieReview = result[i];
                    trailerURL = movieReview.getmURL();
                    trailerName = movieReview.getmAuthor();

                    LinearLayout movieBox = new LinearLayout(getContext());
                    movieBox.setLayoutParams(movieBoxParams);
                    movieBox.setBackgroundColor(Color.parseColor("#ff009688"));
                    movieBox.setVerticalGravity(Gravity.CENTER_VERTICAL);

                    ImageView iv = new ImageView(getContext());
                    iv.setImageResource(R.drawable.thumbs_up_down);

                    iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    //iv.setPadding(16,24,0,0);

                    TextView newTV = new TextView(getContext());
                    newTV.setLayoutParams(lParams);
                    newTV.setPadding(16, 32, 0, 32);
                    newTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    newTV.setText(trailerName);

                    final String _url = trailerURL;

                    movieBox.setOnClickListener(
                            new View.OnClickListener() {
                                public void onClick(View view) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(_url)));
                                }
                            }
                    );

                    movieBox.addView(iv);
                    movieBox.addView(newTV);
                    reviewLayout.addView(movieBox);
                }
            }

        }
    }
}
