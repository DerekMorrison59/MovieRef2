package com.derekmorrison.movieref2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Derek on 12/1/2015.
 * The addition of 'Parcelable' was largely taken from: http://shri.blog.kraya.co.uk/
 * http://shri.blog.kraya.co.uk/2010/04/26/android-parcel-data-to-pass-between-activities-using-parcelable-classes/
 *
 * {"id":"55910381c3a36807f900065d","author":"jonlikesmoviesthatdontsuck","content":"I was a huge fan of the original 3 movies, they were out when I was younger, and I grew up loving dinosaurs because of them. This movie was awesome, and I think it can stand as a testimonial piece towards the capabilities that Christopher Pratt has. He nailed it. The graphics were awesome, the supporting cast did great and the t rex saved the child in me. 10\\5 stars, four thumbs up, and I hope that star wars episode VII doesn't disappoint,","url":"http://j.mp/1GHgSxi"}
 */
public class MovieReview implements Parcelable {
    private String mAuthor;
    private String mContent;
    private String mURL;

    public MovieReview(String mAuthor, String mContent, String mURL) {
        this.setmAuthor(mAuthor);
        this.setmContent(mContent);
        this.setmURL(mURL);
    }

    /*
     * Constructor to use when re-constructing object from a parcel
     *
     * @param in a parcel from which to read this object
     */
    public MovieReview(Parcel in) {
        readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        // Write each field into the parcel in a specific order
        dest.writeString(getmAuthor());
        dest.writeString(getmContent());
        dest.writeString(getmURL());
    }

    /*
     * Called from the constructor to create this object from a parcel.
     *
     * @param in parcel from which to re-create object
     */
    private void readFromParcel(Parcel in) {

        // Read back each field in the order that it was written to the parcel
        setmAuthor(in.readString());
        setmContent(in.readString());
        setmURL(in.readString());
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
    public String getmAuthor() {
        return mAuthor;
    }
    public void setmAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }

    public String getmContent() {
        return mContent;
    }
    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public String getmURL() {
        return mURL;
    }
    public void setmURL(String mURL) {
        this.mURL = mURL;
    }
}
