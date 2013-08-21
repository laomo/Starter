package com.laomo.starter.db;

import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public abstract class DatabaseManager<T> {

    public abstract SQLiteDatabase getSqLiteDatabase();
    /**
     * 插入对象t
     */
    public abstract long insert(T t);

    /**
     * 插入对象t 列表
     */
    public void insertList(List<T> list) {
	try {
	    getSqLiteDatabase().beginTransaction();
	    for (T t : list) {
		insert(t);
	    }
	    getSqLiteDatabase().setTransactionSuccessful();
	} finally {
	    getSqLiteDatabase().endTransaction();
	}
    }

    public abstract void delete();

    /**
     * 删除对象t
     */
    public abstract void delete(T t);

    /**
     * 根据id 删除一条数据
     */
    public abstract void deleteById(String id);

    /**
     * 根据id 批量删除数据
     */
    public abstract void deleteByIdList(List<String> idList);

    /**
     * 更新一条条数据
     */
    public abstract void update(T t);

    /**
     * 更新多条数据
     */
    public void updateList(List<T> list) {
	try {
	    getSqLiteDatabase().beginTransaction();
	    for (T t : list) {
		update(t);
	    }
	    getSqLiteDatabase().setTransactionSuccessful();
	} finally {
	    getSqLiteDatabase().endTransaction();
	}
    }

    /**
     * 插入一条数据 到数据库，如果存在则更新数据库
     */
    public void insertOrUpdate(T t) {
	try {
	    getSqLiteDatabase().beginTransaction();
	    delete(t);
	    insert(t);
	    getSqLiteDatabase().setTransactionSuccessful();
	} finally {
	    getSqLiteDatabase().endTransaction();
	}
    }

    /**
     * 插入多条数据 到数据库，如果存在则更新数据库
     */
    public void insertOrUpdate(List<T> list) {
	try {
	    getSqLiteDatabase().beginTransaction();
	    for (T t : list) {
		insertOrUpdate(t);
	    }
	    getSqLiteDatabase().setTransactionSuccessful();
	} finally {
	    getSqLiteDatabase().endTransaction();
	}
    }

    /**
     * 适用于更新字段较少的情况
     * @param id
     * @param values
     */
    public abstract void update(String id, ContentValues values);

    public T get() {
	List<T> list = find();
	if ((list != null) && (list.size() > 0)) {
	    return (T) list.get(0);
	}
	return null;
    }

    public T get(String id) {
	List<T> list = find(id);
	if ((list != null) && (list.size() > 0)) {
	    return (T) list.get(0);
	}
	return null;
    }

    /**
     * 同时根据外键和自定义的键值对查询数据获取列表,可定义查询排序
     * @param keyValues 多键值对查询
     * @param orderby
     * @param isDesc
     * @return
     */
    public abstract List<T> find(HashMap<String, String> keyValues, String orderby, boolean isDesc);

    /**
     * 根据外键查询数据获取列表，默认排序
     * @return
     */
    public List<T> find() {
	return findWithOrder(null);
    }

    /**
     * 根据外键查询数据获取列表
     * @param orderby 查询排序
     * @return
     */
    public List<T> findWithOrder(String orderby) {
	return findWithOrderAndDesc(orderby, false);
    }

    /**
     * 根据外键查询数据获取列表
     * @param orderby 查询排序
     * @param isDesc 是否倒序查询
     * @return
     */
    public List<T> findWithOrderAndDesc(String orderby, boolean isDesc) {
	return findWithConditionAndOrder(null, orderby, isDesc);
    }

    /**
     * 根据查询条件查询
     * @param condition
     * @param orderby
     * @param isDesc
     * @return
     */
    public abstract List<T> findWithConditionAndOrder(String condition, String orderby, boolean isDesc);
    
    /**
     * 同时根据主键和外键查询数据
     * @param primaryId 主键
     * @return
     */
    protected abstract List<T> find(String primaryId);
    
}
