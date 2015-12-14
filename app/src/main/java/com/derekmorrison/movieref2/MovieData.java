package com.derekmorrison.movieref2;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * Created by Derek on 10/17/2015.
 *
 * The addition of 'Parcelable' was largely taken from: http://shri.blog.kraya.co.uk/
 *
 * http://shri.blog.kraya.co.uk/2010/04/26/android-parcel-data-to-pass-between-activities-using-parcelable-classes/
 */
public class MovieData implements Parcelable {

    private String mTitle;
    private String mReleaseDate;
    private String mPosterPath;
    private String mRating;
    private String mOverview;
    private String mMovieId;


    public MovieData(String mTitle, String mReleaseDate, String mPosterPath, String mRating, String mOverview, String mMovieId) {
        this.setmRating(mRating);
        this.setmOverview(mOverview);
        this.setmTitle(mTitle);
        this.setmReleaseDate(mReleaseDate);
        this.setmPosterPath(mPosterPath);
        this.setmMovieId(mMovieId);
    }

    /*
     * Constructor to use when re-constructing object from a parcel
     *
     * @param in a parcel from which to read this object
     */
    public MovieData(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        // Write each field into the parcel in a specific order
        dest.writeString(getmTitle());
        dest.writeString(getmReleaseDate());
        dest.writeString(getmPosterPath());
        dest.writeString(getmRating());
        dest.writeString(getmOverview());
        dest.writeString(getmMovieId());
    }

    /*
     * Called from the constructor to create this object from a parcel.
     *
     * @param in parcel from which to re-create object
     */
    private void readFromParcel(Parcel in) {

        // Read back each field in the order that it was written to the parcel
        setmTitle(in.readString());
        setmReleaseDate(in.readString());
        setmPosterPath(in.readString());
        setmRating(in.readString());
        setmOverview(in.readString());
        setmMovieId(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * This field is needed for Android to be able to
     * create new objects, individually or as arrays.
     *
     * This also means that you can use use the default
     * constructor to create the object and use another
     * method to hyrdate it as necessary.
     *
     */
    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public MovieData createFromParcel(Parcel in) {
                    return new MovieData(in);
                }

                public MovieData[] newArray(int size) {
                    return new MovieData[size];
                }
            };

    // getters and setters for the data members
    public String getmTitle() {
        return mTitle;
    }
    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmReleaseDate() {
        return mReleaseDate;
    }
    public void setmReleaseDate(String mReleaseDate) {
        this.mReleaseDate = mReleaseDate;
    }

    public String getmPosterPath() {
        return mPosterPath;
    }
    public void setmPosterPath(String mPosterPath) {
        this.mPosterPath = mPosterPath;
    }

    public String getmRating() {
        return mRating;
    }
    public void setmRating(String mRating) {
        this.mRating = mRating;
    }

    public String getmOverview() {
        return mOverview;
    }
    public void setmOverview(String mOverview) {
        this.mOverview = mOverview;
    }

    public String getmMovieId() {
        return mMovieId;
    }
    public void setmMovieId(String mMovieId) {
        this.mMovieId = mMovieId;
    }
}

