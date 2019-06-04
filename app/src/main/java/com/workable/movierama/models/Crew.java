package com.workable.movierama.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * This is a Crew Model class
 * It is used to manufacture Crew objects
 * The reason it implements Parcelable is because it will
 * help with passing Crew Objects through intents
 */
public class Crew implements Parcelable {

    @SerializedName("job")
    private String job;
    @SerializedName("name")
    private String crewName;

    public String getCrewName() {
        return crewName;
    }

    public String getCrewJob() {
        return job;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.job);
        dest.writeString(this.crewName);
    }

    private Crew(Parcel in) {
        this.job = in.readString();
        this.crewName = in.readString();
    }

    public static final Creator<Crew> CREATOR = new Creator<Crew>() {
        @Override
        public Crew createFromParcel(Parcel source) {
            return new Crew(source);
        }

        @Override
        public Crew[] newArray(int size) {
            return new Crew[size];
        }
    };
}
