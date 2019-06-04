package com.workable.movierama.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ReviewApiResponse implements Parcelable {

    @SerializedName("page")
    private int page;
    @SerializedName("results")
    private List<Review> results;
    @SerializedName("total_results")
    private int totalResults;
    @SerializedName("total_pages")
    private int totalPages;

    public List<Review> getReviews() {
        return results;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.page);
        dest.writeTypedList(this.results);
        dest.writeInt(this.totalResults);
        dest.writeInt(this.totalPages);
    }

    private ReviewApiResponse(Parcel in) {
        this.page = in.readInt();
        this.results = in.createTypedArrayList(Review.CREATOR);
        this.totalResults = in.readInt();
        this.totalPages = in.readInt();
    }

    public static final Creator<ReviewApiResponse> CREATOR = new Creator<ReviewApiResponse>() {
        @Override
        public ReviewApiResponse createFromParcel(Parcel source) {
            return new ReviewApiResponse(source);
        }

        @Override
        public ReviewApiResponse[] newArray(int size) {
            return new ReviewApiResponse[size];
        }
    };


}
