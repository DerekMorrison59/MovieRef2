package com.derekmorrison.movieref2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Derek on 11/25/2015.
 * The addition of 'Parcelable' was largely taken from: http://shri.blog.kraya.co.uk/
 * http://shri.blog.kraya.co.uk/2010/04/26/android-parcel-data-to-pass-between-activities-using-parcelable-classes/
 * 
 * {"id":"5474d2339251416e58002ae1","iso_639_1":"en","key":"RFinNxS5KN4","name":"Official Trailer","site":"YouTube","size":1080,"type":"Trailer"}
 */
public class MovieTrailer implements Parcelable {
    private String mName;
    private String mKey;
    private String mSite;
    private String mType;

    final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";

    public MovieTrailer(String mName, String mKey, String mSite, String mType) {
        this.setmType(mType);
        this.setmName(mName);
        this.setmKey(mKey);
        this.setmSite(mSite);
    }

    /*
     * Constructor to use when re-constructing object from a parcel
     *
     * @param in a parcel from which to read this object
     */
    public MovieTrailer(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        // Write each field into the parcel in a specific order
        dest.writeString(getmName());
        dest.writeString(getmKey());
        dest.writeString(getmSite());
        dest.writeString(getmType());
    }

    /*
     * Called from the constructor to create this object from a parcel.
     *
     * @param in parcel from which to re-create object
     */
    private void readFromParcel(Parcel in) {

        // Read back each field in the order that it was written to the parcel
        setmName(in.readString());
        setmKey(in.readString());
        setmSite(in.readString());
        setmType(in.readString());
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

    public String getURL() {
        return YOUTUBE_URL + mKey;
    }
    // getters and setters for the data members
    public String getmName() {
        return mName;
    }
    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmKey() {
        return mKey;
    }
    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public String getmSite() {
        return mSite;
    }
    public void setmSite(String mSite) {
        this.mSite = mSite;
    }

    public String getmType() {
        return mType;
    }
    public void setmType(String mType) {
        this.mType = mType;
    }
}
