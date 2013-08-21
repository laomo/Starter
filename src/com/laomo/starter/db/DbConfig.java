package com.laomo.starter.db;

import java.util.ArrayList;
import java.util.List;

import com.laomo.starter.model.AppInfo;

public class DbConfig {

    public static boolean sHasAccount = false;
    public static final String DBNAME = "welife.db";// 数据库名
    public static final int DBVERSION = 1;

    /**
     * 用来注册需要建表的类
     */
    static List<Class<?>> mClazzes = new ArrayList<Class<?>>();

    /**
     * 注册需要建表的类
     */
    static {
	mClazzes.add(AppInfo.class);
    }
}
