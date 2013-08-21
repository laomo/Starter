package com.laomo.starter.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.laomo.starter.db.annotation.Extra;
import com.laomo.starter.db.annotation.ForeignListField;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper mDbManager = null;

    /**
     * 缓存Class的所有Fields
     */
    private static HashMap<Class<?>, List<Field>> mClassFieldsCache = new HashMap<Class<?>, List<Field>>();

    /**
     * 缓存Class的主键Field
     */
    private static HashMap<Class<?>, Field> mClassPrimaryKeyFieldCache = new HashMap<Class<?>, Field>();

    /**
     * 缓存Class的所有额外保存的表对应的类
     */
    private static HashMap<Class<?>, List<Class<?>>> mClassExtraTableClassCache = new HashMap<Class<?>, List<Class<?>>>();

    /**
     * 从缓存里获取clazz所有@Column注解的Fileds，即用来建表的字段
     * @param clazz
     * @return
     */
    public static List<Field> getClassFields(Class<?> clazz) {
	List<Field> list = mClassFieldsCache.get(clazz);
	if (list == null || list.size() == 0) {
	    list = DbUtils.joinFields(clazz);
	    mClassFieldsCache.put(clazz, list);
	}
	return list;
    }

    /**
     * 从缓存里获取clazz的主键Filed
     * @param clazz
     * @param list
     * @param primaryKey
     * @return
     */
    public static Field getClassPrimaryKeyField(Class<?> clazz, List<Field> allFields, String primaryKey) {
	Field primaryKeyField = mClassPrimaryKeyFieldCache.get(clazz);
	if (primaryKeyField == null) {
	    for (Field field : allFields) {
		if (primaryKey.equals(field.getName())) {
		    primaryKeyField = field;
		    break;
		}
	    }
	    mClassPrimaryKeyFieldCache.put(clazz, primaryKeyField);
	}
	return primaryKeyField;
    }

    /**
     * 从缓存里获取clazz关联的model，从而获得关联的表
     * @param clazz
     * @param list
     * @param primaryKey
     * @return
     */
    public static List<Class<?>> getClassExtraTableClass(Class<?> clazz, List<Field> allFields) {
	List<Class<?>> list = mClassExtraTableClassCache.get(clazz);
	HashSet<Class<?>> hashSet = new HashSet<Class<?>>();//去重
	if (list == null) {
	    list = new ArrayList<Class<?>>();
	    for (Field field : allFields) {
		Class<?> fieldType = field.getType();
		if (field.isAnnotationPresent(Extra.class)) {//此类型保存在额外的表里
		    if (field.isAnnotationPresent(ForeignListField.class)) {//列表
			ForeignListField foreignListField = field.getAnnotation(ForeignListField.class);
			Class<?> itemClass = foreignListField.item();
			/**
			 * 基本类型列表保存在本表中，非基本类型列表才需要保存在外额的表里
			 */
			if (!itemClass.isPrimitive() && itemClass != String.class) {
			    hashSet.add(itemClass);
			}
		    } else {//普通实体
			hashSet.add(fieldType);
		    }
		}
	    }
	    list.addAll(hashSet);
	    mClassExtraTableClassCache.put(clazz, list);
	}
	return list;
    }

    public static DatabaseHelper getInstance(Context context) {
	if (mDbManager == null) {
	    mDbManager = new DatabaseHelper(context);
	}
	return mDbManager;
    }

    private DatabaseHelper(Context context) {
	super(context, DbConfig.DBNAME, null, DbConfig.DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
	TableHelper.getInstance(DbConfig.mClazzes).createTablesByClasses(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	TableHelper.getInstance(DbConfig.mClazzes).dropTablesByClasses(db);
	onCreate(db);
    }
}