package com.workable.movierama.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CreditApiResponse implements Parcelable {

    @SerializedName("cast")
    private List<Cast> castResults;
    @SerializedName("crew")
    private List<Crew> crewResults;

    public List<Cast> getCast() {
        return castResults;
    }

    public List<Crew> getCrew() {
        return crewResults;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.castResults);
        dest.writeTypedList(this.crewResults);
    }

    private CreditApiResponse(Parcel in) {
        this.castResults = in.createTypedArrayList(Cast.CREATOR);
        this.crewResults = in.createTypedArrayList(Crew.CREATOR);
    }

    public static final Creator<CreditApiResponse> CREATOR = new Creator<CreditApiResponse>() {
        @Override
        public CreditApiResponse createFromParcel(Parcel source) {
            return new CreditApiResponse(source);
        }

        @Override
        public CreditApiResponse[] newArray(int size) {
            return new CreditApiResponse[size];
        }
    };
}
