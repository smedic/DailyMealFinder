
package com.smedic.povio.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpeningHours implements Parcelable {

    @SerializedName("exceptional_date")
    @Expose
    private List<Object> exceptionalDate = null;
    @SerializedName("open_now")
    @Expose
    private Boolean openNow;
    @SerializedName("weekday_text")
    @Expose
    private List<Object> weekdayText = null;
    public final static Parcelable.Creator<OpeningHours> CREATOR = new Creator<OpeningHours>() {

        @SuppressWarnings({
                "unchecked"
        })
        public OpeningHours createFromParcel(Parcel in) {
            OpeningHours instance = new OpeningHours();
            in.readList(instance.exceptionalDate, (java.lang.Object.class.getClassLoader()));
            instance.openNow = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
            in.readList(instance.weekdayText, (java.lang.Object.class.getClassLoader()));
            return instance;
        }

        public OpeningHours[] newArray(int size) {
            return (new OpeningHours[size]);
        }

    };

    public List<Object> getExceptionalDate() {
        return exceptionalDate;
    }

    public void setExceptionalDate(List<Object> exceptionalDate) {
        this.exceptionalDate = exceptionalDate;
    }

    public Boolean getOpenNow() {
        return openNow;
    }

    public void setOpenNow(Boolean openNow) {
        this.openNow = openNow;
    }

    public List<Object> getWeekdayText() {
        return weekdayText;
    }

    public void setWeekdayText(List<Object> weekdayText) {
        this.weekdayText = weekdayText;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(exceptionalDate);
        dest.writeValue(openNow);
        dest.writeList(weekdayText);
    }

    public int describeContents() {
        return 0;
    }

}
