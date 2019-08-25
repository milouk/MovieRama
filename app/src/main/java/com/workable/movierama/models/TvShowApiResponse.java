package com.workable.movierama.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TvShowApiResponse implements Parcelable {

    @SerializedName("page")
    private int page;
    @SerializedName("results")
    private List<TvShow> results;
    @SerializedName("total_results")
    private int totalResults;
    @SerializedName("total_pages")
    private int totalPages;

    public List<TvShow> getTvShows() {
        return results;
    }


    protected TvShowApiResponse(Parcel in) {
        this.page = in.readInt();
        this.results = in.createTypedArrayList(TvShow.CREATOR);
        this.totalResults = in.readInt();
        this.totalPages = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.page);
        dest.writeTypedList(this.results);
        dest.writeInt(this.totalResults);
        dest.writeInt(this.totalPages);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TvShowApiResponse> CREATOR = new Creator<TvShowApiResponse>() {
        @Override
        public TvShowApiResponse createFromParcel(Parcel in) {
            return new TvShowApiResponse(in);
        }

        @Override
        public TvShowApiResponse[] newArray(int size) {
            return new TvShowApiResponse[size];
        }
    };
}
