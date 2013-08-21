package com.laomo.starter.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.laomo.starter.model.AppInfo;

public class AppUtil {

    public static ArrayList<AppInfo> getAllApps(Context context, boolean withSystemApp) {

	// Use ArrayList to store the installed apps
	ArrayList<AppInfo> appList = new ArrayList<AppInfo>();
	PackageManager pm = context.getPackageManager();
	List<PackageInfo> packages = pm.getInstalledPackages(0);
	boolean hasRemoveSelf = false;
	String packageName = context.getPackageName();
	Intent intent;

	for (int i = 0; i < packages.size(); i++) {
	    PackageInfo packageInfo = packages.get(i);
	    // Only display the non-system app info
	    if (!withSystemApp && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
		continue;
	    }
	    //No display self
	    if (!hasRemoveSelf && packageName.equals(packageInfo.packageName)) {
		hasRemoveSelf = true;
		continue;
	    }

	    //No display the app that no entrance，such as plug-ins,skins
	    intent = pm.getLaunchIntentForPackage(packageInfo.packageName);
	    if (intent == null) {
		continue;
	    }
	    appList.add(new AppInfo(packageInfo, pm));
	}
	return appList;
    }

    public static Drawable loadIcon(Context context, int icon, String packageName) {
	PackageManager pm = context.getPackageManager();
	if (icon != 0) {
	    Drawable dr = pm.getDrawable(packageName, icon, null);
	    if (dr != null) {
		return dr;
	    }
	}
	return pm.getDefaultActivityIcon();
    }
}
