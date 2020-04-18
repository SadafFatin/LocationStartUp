package com.location.stratup.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.location.stratup.R;
import com.shashank.sony.fancygifdialoglib.FancyGifDialog;
import com.shashank.sony.fancygifdialoglib.FancyGifDialogListener;
import java.util.ArrayList;
import java.util.Iterator;

public class LocationEnableAndPermissionCheck {

        public static String TAG = " Location Permission and Enabled ";
        public static Context context;
        private static LocationManager locationManager;
        private static LocationRequest locationRequest;
        private static LocationSettingsRequest mLocationSettingsRequest;
        private static SettingsClient mSettingsClient;
        private static ArrayList<String> permissions = new ArrayList<>();
        public onGpsListener onGpsListener;
        private OnPermissionListener onPermissionListener;

        public interface OnPermissionListener {
          void perMissionGranted(boolean z, int i);
        }

         public interface onGpsListener {
            void gpsStatus(boolean z);
        }

        public LocationEnableAndPermissionCheck(Context context, onGpsListener onGpsListener, OnPermissionListener onPermissionListener) {
            this.context = context;
            this.onGpsListener = onGpsListener;
            this.onPermissionListener = onPermissionListener;
            permissions.add("android.permission.ACCESS_COARSE_LOCATION");
            permissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
            permissions.add("android.permission.ACCESS_FINE_LOCATION");
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            mSettingsClient = LocationServices.getSettingsClient(context);
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(100);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(2000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            mLocationSettingsRequest = builder.build();
            builder.setAlwaysShow(true);
    }

    public void turnGPSOn() {
        if (isProviderEnabled()) {
            onGpsListener ongpslistener = this.onGpsListener;
            if (ongpslistener != null) {
                ongpslistener.gpsStatus(true);
                return;
            }
            return;
        }
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener((Activity) context, (OnSuccessListener) new OnSuccessListener<LocationSettingsResponse>() {
            @SuppressLint({"MissingPermission"})
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (LocationEnableAndPermissionCheck.this.onGpsListener != null) {
                    LocationEnableAndPermissionCheck.this.onGpsListener.gpsStatus(true);
                }
            }
        }).addOnFailureListener((Activity) context, (OnFailureListener) new OnFailureListener() {
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                if (statusCode == 6) {
                    try {
                        ((ResolvableApiException) e).startResolutionForResult((Activity) LocationEnableAndPermissionCheck.context, AppConstants.GPS_REQUEST);
                    } catch (IntentSender.SendIntentException e2) {
                        Log.d(LocationEnableAndPermissionCheck.TAG, "PendingIntent unable to execute request.");
                    }
                } else if (statusCode == 8502) {
                    LocationEnableAndPermissionCheck.context.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                }
            }
        });
    }

    public boolean isProviderEnabled() {
        return locationManager.isProviderEnabled("gps");
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        }
        Iterator it = permissions.iterator();
        while (it.hasNext()) {
            if (ContextCompat.checkSelfPermission(context, (String) it.next()) != 0) {
                return false;
            }
        }
        return true;
    }

    public void askPermission() {
        Activity activity = (Activity) context;
        ArrayList<String> arrayList = permissions;
        ActivityCompat.requestPermissions(activity, (String[]) arrayList.toArray(new String[arrayList.size()]), AppConstants.COMMON_REQ);
    }

    public void OnPermissionResult(boolean isGranted, int requestCode) {
        this.onPermissionListener.perMissionGranted(isGranted, requestCode);
    }

    public void OnGpsResult(boolean isGranted) {
        this.onGpsListener.gpsStatus(isGranted);
    }

    public void turnOnDailog() {
        new FancyGifDialog.Builder((Activity) context).setGifResource(R.drawable.connectio).setTitle("You must turn on your location to use this app").isCancellable(false).setPositiveBtnText("Turn On location")
                .OnPositiveClicked(new FancyGifDialogListener() {
            public void OnClick() {
                LocationEnableAndPermissionCheck.this.turnGPSOn();
            }}).build();
    }
}