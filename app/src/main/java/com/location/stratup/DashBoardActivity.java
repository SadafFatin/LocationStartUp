    package com.location.stratup;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.fragment.app.FragmentActivity;

    import android.annotation.SuppressLint;
    import android.content.IntentSender;
    import android.location.Location;
    import android.os.Bundle;
    import android.os.Looper;
    import android.util.Log;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.google.android.gms.common.api.ApiException;
    import com.google.android.gms.common.api.ResolvableApiException;
    import com.google.android.gms.location.FusedLocationProviderClient;
    import com.google.android.gms.location.LocationCallback;
    import com.google.android.gms.location.LocationRequest;
    import com.google.android.gms.location.LocationResult;
    import com.google.android.gms.location.LocationServices;
    import com.google.android.gms.location.LocationSettingsRequest;
    import com.google.android.gms.location.LocationSettingsResponse;
    import com.google.android.gms.location.LocationSettingsStatusCodes;
    import com.google.android.gms.location.SettingsClient;
    import com.google.android.gms.maps.CameraUpdateFactory;
    import com.google.android.gms.maps.GoogleMap;
    import com.google.android.gms.maps.OnMapReadyCallback;
    import com.google.android.gms.maps.SupportMapFragment;
    import com.google.android.gms.maps.model.CameraPosition;
    import com.google.android.gms.maps.model.LatLng;
    import com.google.android.gms.maps.model.MarkerOptions;
    import com.google.android.gms.tasks.OnCompleteListener;
    import com.google.android.gms.tasks.OnFailureListener;
    import com.google.android.gms.tasks.OnSuccessListener;
    import com.google.android.gms.tasks.Task;

    import java.text.DateFormat;
    import java.util.Date;

    import static com.location.stratup.util.AppConstants.FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
    import static com.location.stratup.util.AppConstants.UPDATE_INTERVAL_IN_MILLISECONDS;

    public class DashBoardActivity extends FragmentActivity
            implements GoogleMap.OnMyLocationButtonClickListener,
            GoogleMap.OnMyLocationClickListener,
            OnMapReadyCallback {

        private GoogleMap mMap;
        CameraPosition  cameraPosition;

        // Various Location related apis
        private FusedLocationProviderClient mFusedLocationClient;
        private SettingsClient mSettingsClient;
        private LocationRequest mLocationRequest;
        private LocationSettingsRequest mLocationSettingsRequest;
        private LocationCallback mLocationCallback;
        private Location mCurrentLocation;

        // boolean flag to toggle the ui
        private Boolean mRequestingLocationUpdates;


        // location last updated time
        private String mLastUpdateTime;

        //TextViews showing location updates
        TextView locationTextView,locationlastUpdated;
        private String TAG  = "Dashboard Activity";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_dash_board);

            locationlastUpdated = findViewById(R.id.locationLastUpdated);
            locationTextView = findViewById(R.id.location);

            // getting the map ready
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            init();
        }


        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putBoolean("is_requesting_updates", mRequestingLocationUpdates);
            outState.putParcelable("last_known_location", mCurrentLocation);
            outState.putString("last_updated_on", mLastUpdateTime);

        }

        @Override
        public void onResume() {
            super.onResume();
            mRequestingLocationUpdates = true;
            // Resuming location updates depending on button state and
            // allowed permissions
            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }

        }

        @Override
        protected void onPause() {
            super.onPause();
            if (mRequestingLocationUpdates) {
                // pausing location updates
                stopLocationUpdates();
            }
        }


        public void stopLocationUpdates() {
            // Removing location updates
            mRequestingLocationUpdates = false;
            mFusedLocationClient
                    .removeLocationUpdates(mLocationCallback)
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }


        /**
         * Starting location updates
         * Check whether location settings are satisfied and then
         * location updates will be requested
         */
        private void startLocationUpdates() {
            mSettingsClient
                    .checkLocationSettings(mLocationSettingsRequest)
                    .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG, "All location settings are satisfied.");
                            Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();
                            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                    mLocationCallback, Looper.myLooper());
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "Failed at mSettingsClient's checkLocationSettings"+e.getLocalizedMessage());
                            Toast.makeText(getApplicationContext(), "Failed To start location updates!", Toast.LENGTH_SHORT).show();
                            updateLocationUI();
                        }
                    });
        }



        private void init() {

            mRequestingLocationUpdates = false;
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            mLocationSettingsRequest = builder.build();

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mSettingsClient = LocationServices.getSettingsClient(this);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    // location is received
                    mCurrentLocation = locationResult.getLastLocation();
                    mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                    updateLocationUI();
                }
            };


        }


        private void updateLocationUI() {
            if (mCurrentLocation != null) {
                locationTextView.setText(
                        "Lat: " + mCurrentLocation.getLatitude() + ", " +
                                "Lng: " + mCurrentLocation.getLongitude()
                );

                // giving a blink animation on TextView
                locationTextView.setAlpha(0);
                locationTextView.animate().alpha(1).setDuration(300);

                // location last updated time
                locationlastUpdated.setText("Last updated on: " + mLastUpdateTime);

                LatLng myPosition = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(myPosition)
                        .title("Me")).setVisible(true);
                cameraPosition = new CameraPosition.Builder()
                        .target(myPosition)
                        .zoom(17)                   // Sets the zoom
                        .bearing(90)                // Sets the orientation of the camera to east
                        .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



            }
        }







        @Override
        public void onMapReady(GoogleMap map) {
            mMap = map;
            // TODO: Before enabling the My Location layer, you must request
            // location permission from the user. This sample does not include
            // a request for location permission.
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);

        }

        @Override
        public void onMyLocationClick(@NonNull Location location) {
            Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
        }

        @Override
        public boolean onMyLocationButtonClick() {
            Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
            // Return false so that we don't consume the event and the default behavior still occurs
            // (the camera animates to the user's current position).
            return false;
        }





    }
