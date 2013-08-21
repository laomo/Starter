package com.laomo.starter.model;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.laomo.starter.db.annotation.Column;
import com.laomo.starter.db.annotation.Table;

@Table(name = "app_info", primaryKey = "packageName")
public class AppInfo {

    @Column
    public String packageName;
    @Column
    public String appName;
    @Column
    public String versionName;
    @Column
    public int versionCode;
    @Column
    public int appIcon;

    /**
     * 由于使用了现在的数据库框架，所以必须有无参数构造函数
     */
    public AppInfo() {

    }

    public AppInfo(PackageInfo info, PackageManager pm) {
	appName = info.applicationInfo.loadLabel(pm).toString();
	packageName = info.packageName;
	versionName = info.versionName;
	versionCode = info.versionCode;
	appIcon = info.applicationInfo.icon;
    }

    @Override
    public String toString() {
	return "AppInfo [appName=" + appName + ", packageName=" + packageName + ", versionName=" + versionName
	    + ", versionCode=" + versionCode + ", appIcon=" + appIcon + "]";
    }

}
