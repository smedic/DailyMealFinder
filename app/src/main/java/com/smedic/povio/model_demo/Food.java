
package com.smedic.povio.model_demo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Food implements Parcelable {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("price")
    @Expose
    private Double price;
    @SerializedName("category")
    @Expose
    private String category;
    public final static Parcelable.Creator<Food> CREATOR = new Creator<Food>() {

        @SuppressWarnings({
                "unchecked"
        })
        public Food createFromParcel(Parcel in) {
            Food instance = new Food();
            instance.name = ((String) in.readValue((String.class.getClassLoader())));
            instance.price = ((Double) in.readValue((Double.class.getClassLoader())));
            instance.category = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public Food[] newArray(int size) {
            return (new Food[size]);
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(name);
        dest.writeValue(price);
        dest.writeValue(category);
    }

    public int describeContents() {
        return 0;
    }

}
