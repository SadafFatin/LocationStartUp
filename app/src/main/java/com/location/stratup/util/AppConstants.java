package com.location.stratup.util;

public class AppConstants {
    public static final int COMMON_REQ = 333;
    public static int FILE_WRITE_REQUEST = 111;
    public static int GPS_REQUEST = 999;
    // location updates interval - 10sec
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
}
