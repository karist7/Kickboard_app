package com.example.scooter2.server;

import com.google.gson.annotations.SerializedName;

public class Markers {
    @SerializedName("id")
    private String id;
    @SerializedName("date")
    private String date;
    @SerializedName("latitude")
    private String latitude;
    @SerializedName("longitude")
    private String longitude;
    @SerializedName("count")
    private String count;

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getCount() {
        return count;
    }
}
