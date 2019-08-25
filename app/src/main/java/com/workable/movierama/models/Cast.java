package com.workable.movierama.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * This is a Cast Model class
 * It is used to manufacture Cast objects
 * The reason it implements Parcelable is because it will
 *  help with passing Cast Objects through intents
 */
public class Cast implements Parcelable {

    @SerializedName("name")
    private String castName;
    @SerializedName("character")
    private String character;

    public String getCastName() {
        return castName;
    }
    public String getCharacter() {
        return character;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.castName);
        dest.writeString(this.character);
    }

    private Cast(Parcel in) {
        this.castName = in.readString();
        this.character = in.readString();

    }

    public static final Creator<Cast> CREATOR = new Creator<Cast>() {
        @Override
        public Cast createFromParcel(Parcel source) {
            return new Cast(source);
        }

        @Override
        public Cast[] newArray(int size) {
            return new Cast[size];
        }
    };
}
