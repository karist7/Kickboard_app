package com.example.scooter2;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    public static String getDetailedAddressFromLocation(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String detailedAddress = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);


                String fullAddressLine = address.getAddressLine(0); // 대한민국 충청남도 아산시 신창면 순천향로 22 순천향대 학교 앙뜨레 프레 너관
                Log.d("fullAddress", fullAddressLine);

// fullAddressLine에서 thoroughfare를 제거하여 22 부분을 추출
                String numberPart = fullAddressLine.replaceAll("[^0-9]", "");
                String featureName = address.getFeatureName();
                String Throughfare = address.getThoroughfare();
                String locality = address.getLocality();
                String adminArea = address.getAdminArea();

                detailedAddress = adminArea+" "+locality+ " "+Throughfare+ " "+featureName+" "+numberPart;



                // 조합하여 출력

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return detailedAddress;
    }
}
