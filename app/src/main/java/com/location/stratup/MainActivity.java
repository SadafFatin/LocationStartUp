package com.location.stratup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;

import com.location.stratup.util.AppConstants;
import com.location.stratup.util.LocationEnableAndPermissionCheck;

public class MainActivity extends AppCompatActivity implements
        LocationEnableAndPermissionCheck.onGpsListener,
        LocationEnableAndPermissionCheck.OnPermissionListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private boolean isGPS = false;
    private boolean isPermissionEnabled = false;
    LocationEnableAndPermissionCheck locationEnableAndPermissionCheck;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        this.locationEnableAndPermissionCheck = new LocationEnableAndPermissionCheck(MainActivity.this, this, this);
        if (!this.locationEnableAndPermissionCheck.isPermissionGranted()) {
            this.locationEnableAndPermissionCheck.askPermission();
        } else if (this.locationEnableAndPermissionCheck.isProviderEnabled()) {
            startRegistrationActivity();
        } else {
            this.locationEnableAndPermissionCheck.turnOnDailog();
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                this.isGPS = true;
                this.locationEnableAndPermissionCheck.OnGpsResult(this.isGPS);
            }
        } else if (requestCode == AppConstants.GPS_REQUEST) {
            this.isGPS = false;
            this.locationEnableAndPermissionCheck.OnGpsResult(this.isGPS);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 333) {
            if (grantResults.length <= 0 || grantResults[0] != 0) {
                this.locationEnableAndPermissionCheck.OnPermissionResult(false, AppConstants.COMMON_REQ);
            } else {
                this.locationEnableAndPermissionCheck.OnPermissionResult(true, AppConstants.COMMON_REQ);
            }
        }
    }

    public void gpsStatus(boolean isGPSEnable) {
        this.isGPS = isGPSEnable;
        if (!isGPSEnable) {
            this.locationEnableAndPermissionCheck.turnOnDailog();
        } else if (this.isPermissionEnabled) {
            startRegistrationActivity();
        } else {
            this.locationEnableAndPermissionCheck.askPermission();
        }
    }

    public void perMissionGranted(boolean isGranted, int requestCode) {
        this.isPermissionEnabled = isGranted;
        if (this.isGPS && this.isPermissionEnabled) {
            startRegistrationActivity();
        } else if (!this.isPermissionEnabled) {
            this.locationEnableAndPermissionCheck.askPermission();
        } else {
            this.locationEnableAndPermissionCheck.turnOnDailog();
        }
    }

    private void startRegistrationActivity() {
        startActivity(new Intent(this,DashBoardActivity.class));
    }
}
