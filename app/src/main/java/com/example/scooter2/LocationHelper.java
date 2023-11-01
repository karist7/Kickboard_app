package com.example.scooter2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    public static String getAddressFromLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String newAddress="";
        String addressText = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {

                Address address = addresses.get(0);
                String[] info = address.toString().split(" ");

                newAddress = info[1]+" "+info[2]+" "+info[3]+" "+info[4]+" "+info[5];

            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        return newAddress;
    }
}
