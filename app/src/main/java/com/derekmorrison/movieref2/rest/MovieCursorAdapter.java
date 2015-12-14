package com.derekmorrison.movieref2.rest;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.derekmorrison.movieref2.Globals;
import com.derekmorrison.movieref2.MainActivityFragment;
import com.derekmorrison.movieref2.MovieDetailActivity;
import com.derekmorrison.movieref2.MovieDetailActivityFragment;
import com.derekmorrison.movieref2.R;
import com.derekmorrison.movieref2.Utility;
import com.squareup.picasso.Picasso;

/**
 * Created by Derek on 11/30/2015.
 **
 * Created by sam_chordas on 8/12/15.
 * Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */

public class MovieCursorAdapter extends CursorRecyclerViewAdapter<MovieCursorAdapter.ViewHolder>{

    Context mContext;
    FragmentActivity mFragmentActivity;
    ViewHolder mVh;
    //boolean veryFirstTime;

    public MovieCursorAdapter(Context context, Cursor cursor){
        super(context, cursor);
        mFragmentActivity = (FragmentActivity) context;
        mContext = context;
        //veryFirstTime = true;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView moviePoster;
        public final ImageView favImage;
        public final TextView movieTitle;

        public ViewHolder(View view) {
        super(view);
            moviePoster = (ImageView) view.findViewById(R.id.list_item_movie_poster_imageview);
            favImage = (ImageView) view.findViewById(R.id.list_item_favorite_imageview);
            movieTitle = (TextView) view.findViewById(R.id.grid_movie_title_textView);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_movie_poster, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        mVh = vh;
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor){
        //DatabaseUtils.dumpCursor(cursor);

        String movieID = cursor.getString(MainActivityFragment.COL_MOVIE_ID);
        String posterPath = cursor.getString(MainActivityFragment.COL_POSTER_PATH);

//        if (Globals.getInstance().getTwoPane() && veryFirstTime) {
//            veryFirstTime = false;
//            setDetailMovieID(movieID);
//        }

        if (Globals.getInstance().getTwoPane() && Globals.getInstance().getIsNewList()) {
            Globals.getInstance().setIsNewList(false);
            setDetailMovieID(movieID);
        }

        if (Utility.isNetworkAvailable(mContext) == false){

            // load the 'image not loaded' PNG and show the movie title instead
            Picasso
                    .with(mContext)
                    .load(R.drawable.no_image_down)
                    .error(R.drawable.error_image_not_loaded)
                    .into(viewHolder.moviePoster);

            viewHolder.movieTitle.setVisibility(View.VISIBLE);
            viewHolder.movieTitle.setText(cursor.getString(MainActivityFragment.COL_TITLE));
        } else {
            viewHolder.movieTitle.setVisibility(View.GONE);
            viewHolder.movieTitle.setText("");

            // get the image from TMDB via Picasso
            final String url_base = "http://image.tmdb.org/t/p/";
            final String image_thumbnail_size = "w185";

            String imageURL = url_base + image_thumbnail_size + posterPath;
            Picasso
                    .with(mContext)
                    .load(imageURL)
                    .error(R.drawable.error_image_not_loaded)
                    .into(viewHolder.moviePoster);
        }

        // dislay a gold star if this is a favorite movie
        String isFav = cursor.getString(MainActivityFragment.COL_FAVORITE);

        if (isFav.equals("true")){
            // make sure the image view is displayed
            viewHolder.favImage.setVisibility(View.VISIBLE);
            Picasso
                    .with(mContext)
                    .load(R.drawable.color_favorite)
                    .into(viewHolder.favImage);
        } else {
            // hide the image view completely
            viewHolder.favImage.setVisibility(View.GONE);
        }

        if (movieID == null || movieID.isEmpty()){
            return;
        }

        // hack to get a 'final' version of the movie ID required for the listener
        final String mid = movieID;

        // attach a Listener to launch the MovieDetailActivity when a movie poster is tapped
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (Globals.getInstance().getTwoPane() == true) {
                    setDetailMovieID(mid);
                } else {
                    // create a new intent to launch the movie detail activity
                    Intent detailIntent = new Intent(view.getContext(), MovieDetailActivity.class);

                    // Pass the movie ID to the MovieDetailActivity using the intent Bundle
                    detailIntent.putExtra(mContext.getString(R.string.movie_id_param), mid);

                    view.getContext().startActivity(detailIntent);
                }
            }
        });
    }

    // this method is called when the app is in 'two-pane' mode and the user taps on a movie poster
    // the details fragment on the right is replaced new one using the selected movie ID
    private void setDetailMovieID(String mid) {

        // pass the movie ID via the 'arguments' bundle
        Bundle args = new Bundle();
        args.putString(mContext.getString(R.string.movie_id_param), mid);

        android.support.v4.app.FragmentTransaction transaction = mFragmentActivity.getSupportFragmentManager().beginTransaction();

        MovieDetailActivityFragment mMovieDetailActivityFragment = new MovieDetailActivityFragment();
        mMovieDetailActivityFragment.setArguments(args);

        transaction.replace(R.id.movie_detail_container, mMovieDetailActivityFragment);
        transaction.commit();
    }
}
