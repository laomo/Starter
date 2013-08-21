package com.laomo.starter.db;

import android.util.Log;

public class LogUtils {
    //private static boolean isLog = false;
    private static boolean isLog = true;

    public static void log(String msg) {
	if (isLog) {
	    Log.d("laomo", msg);
	}
    }
    
    public static void log(Exception e) {
	if (isLog) {
	    Log.d("laomo", e.getMessage(), e);
	}
    }
}