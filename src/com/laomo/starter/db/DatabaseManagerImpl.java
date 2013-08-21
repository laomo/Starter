package com.laomo.starter.db;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import roboguice.util.Ln;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.laomo.starter.db.annotation.Extra;
import com.laomo.starter.db.annotation.ForeignListField;
import com.laomo.starter.db.annotation.Table;

/**
 * 所有的表都和用户相关，即每个表都有accountId字段，model层不用处理
 * @author rgz
 * @date 2013-4-25
 */
public class DatabaseManagerImpl<T> extends DatabaseManager<T> {
    /**
     * sql log开关，开发阶段打开用来打印sql语句，发布时关闭
     */
    private final static boolean debug = false;
    private SQLiteOpenHelper mDbHelper;
    private SQLiteDatabase mDatabase = null;
    private Class<T> mClass;
    private List<Field> allFields;
    private String tableName;
    private String primaryKey;
    private String foreignKey;
    private boolean primaryKeyAutoIncrement;

    private String foreignId = null;
    private String accountId;

    private static final int METHOD_INSERT = 0;
    private static final int METHOD_UPDATE = 1;

    @SuppressWarnings("unchecked")
    public DatabaseManagerImpl(SQLiteOpenHelper sqLiteOpenHelper, Class<T> clazz) {
	mDbHelper = sqLiteOpenHelper;
	if (clazz == null) {
	    mClass = ((Class<T>) ((java.lang.reflect.ParameterizedType) super.getClass().getGenericSuperclass())
		.getActualTypeArguments()[0]);
	} else {
	    mClass = clazz;
	}

	if (DbUtils.isTableAnnotationPresent(mClass)) {
	    Table table = (Table) mClass.getAnnotation(Table.class);
	    tableName = table.name();
	    primaryKey = table.primaryKey();
	    foreignKey = table.foreignKey();
	    primaryKeyAutoIncrement = table.primaryKeyAutoIncrement();
	}
	allFields = DatabaseHelper.getClassFields(mClass);
	if (debug) {
	    LogUtils.log("clazz:" + mClass + " tableName:" + tableName + " primaryKey:" + primaryKey);
	}

	if (!DbConfig.sHasAccount) {
	    accountId = "laomo";
	} else {
	    //TODO:use account
	}
	if (TextUtils.isEmpty(accountId)) {
	    throw new RuntimeException("accountId不能为空！");
	}
    }

    public SQLiteDatabase getSqLiteDatabase() {
	if (mDatabase == null || !mDatabase.isOpen()) {
	    mDatabase = mDbHelper.getWritableDatabase();
	}
	return mDatabase;
    }

    public void destroy() {
	if (mDatabase == null || mDatabase.isOpen()) {
	    mDatabase.close();
	    mDatabase = null;
	}
    }

    public DatabaseManagerImpl(String foreignId, SQLiteOpenHelper sqLiteOpenHelper, Class<T> clazz) {
	this(sqLiteOpenHelper, clazz);
	this.foreignId = foreignId;
    }

    /**
     * 根据外键查询数据获得外键相关的数据Id
     */
    private List<String> findId() {
	List<String> idList = new ArrayList<String>();
	Cursor cursor = null;
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("select ").append(primaryKey).append(" from ").append(tableName).append(" where accountId='")
	    .append(accountId).append("' ");
	try {
	    String selectionArgs[] = null;
	    if (DbUtils.hasForeignKey(foreignKey, foreignId)) {
		sqlBuffer.append(" and ").append(foreignKey).append(" = ? ");
		selectionArgs = new String[] { foreignId };
		cursor = getSqLiteDatabase().rawQuery(sqlBuffer.toString(), selectionArgs);
		if (cursor != null) {
		    while (cursor.moveToNext()) {
			idList.add(cursor.getString(0));
		    }
		}
	    }
	} catch (Exception e) {
	    Ln.e("[findId] from DB Exception");
	    Ln.e(e);
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return idList;
    }

    public List<T> find(HashMap<String, String> keyValues, String orderby, boolean isDesc) {
	List<T> list = new ArrayList<T>();
	Cursor cursor = null;
	String selectionArgs[] = new String[keyValues.size() + 1];
	int selection = 0;
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("select * from ").append(tableName).append(" where accountId='").append(accountId)
	    .append("' ");
	try {
	    if (DbUtils.hasForeignKey(foreignKey, foreignId)) {
		sqlBuffer.append(" and ").append(foreignKey).append(" = ? ");
		selectionArgs[selection++] = foreignId;
	    }
	    for (Entry<String, String> entry : keyValues.entrySet()) {
		sqlBuffer.append(" and ").append(entry.getKey()).append(" = ? ");
		selectionArgs[selection++] = entry.getValue();
	    }
	    sqlBuffer.append(" order by ");
	    if (TextUtils.isEmpty(orderby)) {
		sqlBuffer.append(primaryKey);
	    } else {
		sqlBuffer.append(orderby);
	    }
	    if (isDesc) {
		sqlBuffer.append(" desc");
	    }
	    cursor = getSqLiteDatabase().rawQuery(sqlBuffer.toString(), selectionArgs);
	    getListFromCursorSelfTable(list, cursor);
	} catch (Exception e) {
	    Ln.e("[find] from DB Exception");
	    Ln.e(e);
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}

	return list;
    }

    public List<T> findWithConditionAndOrder(String condition, String orderby, boolean isDesc) {
	List<T> list = new ArrayList<T>();
	Cursor cursor = null;
	String selectionArgs[] = null;
	try {
	    StringBuffer sqlBuffer = new StringBuffer();
	    sqlBuffer.append("select * from ").append(tableName).append(" where accountId='").append(accountId)
		.append("' ");
	    if (!TextUtils.isEmpty(condition)) {
		sqlBuffer.append(" and ").append(condition);
	    }
	    if (DbUtils.hasForeignKey(foreignKey, foreignId)) {
		sqlBuffer.append(" and ").append(foreignKey).append(" = ? ");
		selectionArgs = new String[] { foreignId };
	    }

	    if (!TextUtils.isEmpty(orderby)) {
		sqlBuffer.append(" order by ").append(orderby);
		if (isDesc) {
		    sqlBuffer.append(" desc");
		}
	    }
	    cursor = getSqLiteDatabase().rawQuery(sqlBuffer.toString(), selectionArgs);
	    getListFromCursor(list, cursor);
	} catch (Exception e) {
	    Ln.e(e);
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}

	return list;
    }

    protected List<T> find(String primaryId) {

	List<T> list = new ArrayList<T>();
	Cursor cursor = null;
	String selectionArgs[] = null;
	StringBuffer sqlBuffer = new StringBuffer();
	sqlBuffer.append("select * from ").append(tableName).append(" where accountId='").append(accountId)
	    .append("' ").append(" and ").append(primaryKey).append(" = ? ");
	try {
	    if (DbUtils.hasForeignKey(foreignKey, foreignId)) {
		sqlBuffer.append(" and ").append(foreignKey).append(" = ? ");
		selectionArgs = new String[] { primaryId, foreignId };
	    } else {
		selectionArgs = new String[] { primaryId };
	    }
	    cursor = getSqLiteDatabase().rawQuery(sqlBuffer.toString(), selectionArgs);
	    getListFromCursor(list, cursor);
	} catch (Exception e) {
	    Ln.e(e);
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}

	return list;
    }

    /**
     * 从Cursor中获取数据，同时获取保存在额外的表里的字段，保证数据完整性
     * @param list
     * @param cursor
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void getListFromCursor(List<T> list, Cursor cursor) throws IllegalAccessException, InstantiationException {
	while (cursor.moveToNext()) {
	    T entity = mClass.newInstance();
	    String primaryId = cursor.getString(cursor.getColumnIndex(primaryKey));
	    if (TextUtils.isEmpty(primaryId)) {
		throw new RuntimeException("主键不能为空！");
	    }
	    for (Field field : allFields) {

		field.setAccessible(true);
		Class<?> fieldType = field.getType();
		if (field.isAnnotationPresent(Extra.class)) {//此类型保存在额外的表里
		    if (field.isAnnotationPresent(ForeignListField.class)) {//列表
			ForeignListField foreignListField = field.getAnnotation(ForeignListField.class);
			Class<?> foreignClass = foreignListField.item();
			/**
			 * if { 基本类型的列表，保存在本表，查询获得成字符串然后转成List; }else{ 非基本类型的列表保存在额外的表里。 }
			 * 基本类型的列表表中列名字生成规则：List+field的名字 内容生成规则：list的item用*##*分隔组装成字符串
			 */
			if (foreignClass.isPrimitive() || foreignClass == String.class) {
			    String result = cursor.getString(cursor.getColumnIndex("List" + field.getName()));
			    field.set(entity, DbUtils.getListFormString(result, foreignClass));
			} else if (DbUtils.isTableAnnotationPresent(foreignClass)) {
			    DatabaseManager databaseManager = new DatabaseManagerImpl(primaryId, mDbHelper,
				foreignClass);
			    List foreignList = databaseManager.find();
			    field.set(entity, foreignList);
			}
		    } else {//普通实体
			if (DbUtils.isTableAnnotationPresent(fieldType)) {
			    DatabaseManager databaseManager = new DatabaseManagerImpl(primaryId, mDbHelper, fieldType);
			    Object obj = databaseManager.get();
			    field.set(entity, obj);
			}
		    }
		} else {//基本数据类型
		    int c = cursor.getColumnIndex(field.getName());
		    if (c < 0) {
			continue; // 如果不存则循环下个属性值
		    } else if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {
			field.set(entity, cursor.getInt(c));
		    } else if (String.class == fieldType) {
			field.set(entity, cursor.getString(c));
		    } else if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {
			field.set(entity, Long.valueOf(cursor.getLong(c)));
		    } else if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {
			field.set(entity, Float.valueOf(cursor.getFloat(c)));
		    } else if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {
			field.set(entity, Short.valueOf(cursor.getShort(c)));
		    } else if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {
			field.set(entity, Double.valueOf(cursor.getDouble(c)));
		    } else if (Date.class == fieldType) {
			Date date = new Date();
			date.setTime(cursor.getLong(c));
			field.set(entity, date);
		    } else if (Blob.class == fieldType) {
			field.set(entity, cursor.getBlob(c));
		    } else if (Character.TYPE == fieldType) {
			String fieldValue = cursor.getString(c);

			if ((fieldValue != null) && (fieldValue.length() > 0)) {
			    field.set(entity, Character.valueOf(fieldValue.charAt(0)));
			}
		    }
		}
	    }

	    list.add((T) entity);
	}
    }

    /**
     * 从Cursor中获取数据，不获取保存在额外的表里的字段
     * @param list
     * @param cursor
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void getListFromCursorSelfTable(List<T> list, Cursor cursor) throws IllegalAccessException,
	InstantiationException {
	while (cursor.moveToNext()) {
	    T entity = mClass.newInstance();
	    for (Field field : allFields) {

		field.setAccessible(true);
		Class<?> fieldType = field.getType();
		if (field.isAnnotationPresent(Extra.class)) {
		    continue;
		} else {//基本数据类型
		    int c = cursor.getColumnIndex(field.getName());
		    if (c < 0) {
			continue; // 如果不存则循环下个属性值
		    } else if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {
			field.set(entity, cursor.getInt(c));
		    } else if (String.class == fieldType) {
			field.set(entity, cursor.getString(c));
		    } else if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {
			field.set(entity, Long.valueOf(cursor.getLong(c)));
		    } else if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {
			field.set(entity, Float.valueOf(cursor.getFloat(c)));
		    } else if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {
			field.set(entity, Short.valueOf(cursor.getShort(c)));
		    } else if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {
			field.set(entity, Double.valueOf(cursor.getDouble(c)));
		    } else if (Date.class == fieldType) {
			Date date = new Date();
			date.setTime(cursor.getLong(c));
			field.set(entity, date);
		    } else if (Blob.class == fieldType) {
			field.set(entity, cursor.getBlob(c));
		    } else if (Character.TYPE == fieldType) {
			String fieldValue = cursor.getString(c);

			if ((fieldValue != null) && (fieldValue.length() > 0)) {
			    field.set(entity, Character.valueOf(fieldValue.charAt(0)));
			}
		    }
		}
	    }

	    list.add((T) entity);
	}
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public long insert(T entity) {
	HashMap<Class<?>, List> insertMap = new HashMap<Class<?>, List>();
	long primaryId = 0L;
	try {
	    getSqLiteDatabase().beginTransaction();
	    primaryId = getSqLiteDatabase().insert(tableName, null, setContentValues(entity, METHOD_INSERT, insertMap));
	    if (primaryKeyAutoIncrement) {
		Iterator<Entry<Class<?>, List>> iterator = insertMap.entrySet().iterator();
		while (iterator.hasNext()) {
		    Entry<Class<?>, List> e = iterator.next();
		    DatabaseManager databaseManager = new DatabaseManagerImpl(String.valueOf(primaryId), mDbHelper,
			e.getKey());
		    databaseManager.insertList(e.getValue());
		}
	    }
	    getSqLiteDatabase().setTransactionSuccessful();
	} catch (Exception e) {
	    Ln.d("[insert] into DB Exception:" + e.getMessage());
	} finally {
	    getSqLiteDatabase().endTransaction();
	}
	return primaryId;
    }

    /**
     * 删除存在额外表里的信息
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void deleteExtra(List<String> idList) {
	List<Class<?>> list = DatabaseHelper.getClassExtraTableClass(mClass, allFields);
	for (Class<?> clazz : list) {
	    for (String id : idList) {
		DatabaseManager databaseManager = new DatabaseManagerImpl(id, mDbHelper, clazz);
		databaseManager.delete();
	    }
	}
    }

    /**
     * 根据外键删除
     */
    public void delete() {
	List<String> idList = findId();
	String whereArgs[] = null;
	StringBuffer whereBuffer = new StringBuffer();
	whereBuffer.append("accountId='").append(accountId).append("' ");
	if (DbUtils.hasForeignKey(foreignKey, foreignId)) {
	    whereBuffer.append(" and ").append(foreignKey).append(" = ? ");
	    whereArgs = new String[] { foreignId };
	}
	try {
	    getSqLiteDatabase().beginTransaction();
	    getSqLiteDatabase().delete(tableName, whereBuffer.toString(), whereArgs);
	    deleteExtra(idList);
	    getSqLiteDatabase().setTransactionSuccessful();
	} finally {
	    getSqLiteDatabase().endTransaction();
	}
    }

    public void delete(T entity) {
	String primaryId = getPrimaryId(entity);
	deleteById(primaryId);
    }

    public void deleteById(String id) {
	StringBuffer whereBuffer = new StringBuffer();
	whereBuffer.append("accountId='").append(accountId).append("' and ").append(primaryKey).append(" = ? ");
	if (debug) {
	    Ln.d("[delete]: " + whereBuffer.toString());
	}
	try {
	    getSqLiteDatabase().beginTransaction();
	    getSqLiteDatabase().delete(tableName, whereBuffer.toString(), new String[] { id });
	    List<String> idList = new ArrayList<String>();
	    idList.add(id);
	    deleteExtra(idList);
	    getSqLiteDatabase().setTransactionSuccessful();
	} finally {
	    getSqLiteDatabase().endTransaction();
	}
    }

    public void deleteByIdList(List<String> idList) {
	int count = idList.size();
	if (count > 0) {
	    StringBuffer sqlBuffer = new StringBuffer();
	    sqlBuffer.append("delete from ").append(tableName).append(" where accountId='").append(accountId)
		.append("' ").append(" and ").append(primaryKey).append(" in (");
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < count; i++) {
		sb.append('?').append(',');
	    }
	    sb.deleteCharAt(sb.length() - 1);
	    sqlBuffer.append(sb).append(")");
	    String sql = "delete from " + tableName + " where " + primaryKey + " in (" + sb + ")";
	    if (debug) {
		Ln.d("[delete]: " + DbUtils.getLogSql(sql, idList.toArray()));
	    }
	    try {
		getSqLiteDatabase().beginTransaction();
		getSqLiteDatabase().execSQL(sql, idList.toArray());
		deleteExtra(idList);
		getSqLiteDatabase().setTransactionSuccessful();
	    } finally {
		getSqLiteDatabase().endTransaction();
	    }
	}
    }

    public void update(T entity) {
	try {
	    ContentValues values = setContentValues(entity, METHOD_UPDATE, null);
	    StringBuffer whereBuffer = new StringBuffer();
	    whereBuffer.append("accountId='").append(accountId).append("' and ").append(primaryKey).append(" = ? ");
	    String id = values.getAsString(primaryKey);
	    values.remove(primaryKey);
	    String[] whereValue = { id };
	    getSqLiteDatabase().update(tableName, values, whereBuffer.toString(), whereValue);
	} catch (Exception e) {
	    Ln.e(e);
	}
    }

    @Override
    public void update(String id, ContentValues values) {
	try {
	    StringBuffer whereBuffer = new StringBuffer();
	    whereBuffer.append("accountId='").append(accountId).append("' and ").append(primaryKey).append(" = ? ");
	    String[] whereValue = { id };
	    getSqLiteDatabase().update(tableName, values, whereBuffer.toString(), whereValue);
	} catch (Exception e) {
	    Ln.e(e);
	}
    }

    private String getPrimaryId(T entity) {
	Field primaryKeyField = DatabaseHelper.getClassPrimaryKeyField(mClass, allFields, primaryKey);
	primaryKeyField.setAccessible(true);
	String primaryId = null;
	try {
	    primaryId = String.valueOf(primaryKeyField.get(entity));
	} catch (IllegalArgumentException e) {
	    Ln.e(e);
	} catch (IllegalAccessException e) {
	    Ln.e(e);
	}
	return primaryId;
    }

    /**
     * 设置插入或者更新方法的ContentValues，同时将需要额外的表来保存的信息保存保证数据完整性
     * @param entity
     * @param type
     * @param method
     * @return
     * @throws IllegalAccessException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ContentValues setContentValues(T entity, int method, HashMap<Class<?>, List> insertMap)
	throws IllegalAccessException {
	ContentValues values = new ContentValues();
	StringBuffer strField = new StringBuffer("(");
	StringBuffer strValue = new StringBuffer(" values(");
	StringBuffer strUpdate = new StringBuffer(" ");
	for (Field field : allFields) {
	    field.setAccessible(true);
	    Object fieldValue = field.get(entity);
	    if (fieldValue == null)
		continue;
	    /**
	     * 主键自增长，insert方法values中不添加主键，但是如果是update方法就要添加
	     */
	    if (primaryKeyAutoIncrement && field.getName().equals(primaryKey) && method == METHOD_INSERT) {
		continue;
	    }
	    // 需要额外的表来保存信息
	    if (field.isAnnotationPresent(Extra.class)) {
		Field primaryKeyField = DatabaseHelper.getClassPrimaryKeyField(mClass, allFields, primaryKey);
		primaryKeyField.setAccessible(true);
		String primaryId = String.valueOf(primaryKeyField.get(entity));
		// 需要保存的是个列表
		if (field.isAnnotationPresent(ForeignListField.class)) {
		    // 对象列表
		    List list = (List) fieldValue;
		    ForeignListField foreignListField = field.getAnnotation(ForeignListField.class);
		    Class<?> itemClass = foreignListField.item();
		    /**
		     * if{ 基本类型的列表，转换成字符串保存到本表; }else{ 非基本类型的列表保存在额外的表里。 } 基本类型的列表表中列名字生成规则：List+field的名字
		     * 内容生成规则：list的item用*##*分隔组装成字符串
		     */
		    if (itemClass.isPrimitive() || itemClass == String.class) {
			values.put("List" + field.getName(), DbUtils.getStringFormList(list));
		    } else {
			DatabaseManager dbManager = new DatabaseManagerImpl(primaryId, mDbHelper, itemClass);
			if (method == METHOD_INSERT) {
			    /**
			     * 主键自增长，先插入主表，再去插入子表，此处先保存，否则在此直接插入
			     */
			    if (primaryKeyAutoIncrement) {
				insertMap.put(itemClass, list);
			    } else {
				dbManager.insertList(list);
			    }
			} else {
			    dbManager.updateList(list);
			}
		    }
		} else {// 实体类
		    Class<?> subClass = field.getType();
		    DatabaseManager dbManager = new DatabaseManagerImpl(primaryId, mDbHelper, subClass);
		    // 对象
		    if (method == METHOD_INSERT) {
			/**
			 * 主键自增长，先插入主表，再去插入子表，此处先保存，否则在此直接插入
			 */
			if (primaryKeyAutoIncrement) {
			    List beanList = new ArrayList();
			    beanList.add(fieldValue);
			    insertMap.put(subClass, beanList);
			} else {
			    dbManager.insert(fieldValue);
			}

		    } else {
			dbManager.update(fieldValue);
		    }
		}
		continue;
	    }

	    if (Date.class.equals(field.getType())) {
		values.put(field.getName(), ((Date) fieldValue).getTime());
		continue;
	    }
	    String value = String.valueOf(fieldValue);
	    if (TextUtils.isEmpty(value)) {
		continue;
	    }
	    values.put(field.getName(), value);
	    if (debug) {
		if (method == METHOD_INSERT) {
		    strField.append(field.getName()).append(",");
		    strValue.append("'").append(value).append("',");
		} else {
		    strUpdate.append(field.getName()).append("=").append("'").append(value).append("',");
		}
	    }

	}
	if (!TextUtils.isEmpty(foreignKey)) {
	    values.put(foreignKey, foreignId);
	    if (debug) {
		strField.append(foreignKey).append(",");
		strValue.append("'").append(foreignId).append("',");
	    }
	}
	values.put("accountId", accountId);
	if (debug) {
	    strField.append("accountId,");
	    strValue.append("'").append(accountId).append("',");
	    String sql = "";
	    if (method == METHOD_INSERT) {
		strField.deleteCharAt(strField.length() - 1).append(")");
		strValue.deleteCharAt(strValue.length() - 1).append(")");
		sql = strField.toString() + strValue.toString();
		Ln.d("[insert]: insert into " + tableName + " " + sql);
	    } else {
		sql = strUpdate.deleteCharAt(strUpdate.length() - 1).append(" ").toString();
		Field primaryKeyField = DatabaseHelper.getClassPrimaryKeyField(mClass, allFields, primaryKey);
		primaryKeyField.setAccessible(true);
		String primaryId = String.valueOf(primaryKeyField.get(entity));
		String where = primaryKey + " = " + primaryId;
		Ln.d("[update]: update " + tableName + " set " + sql + " where " + where);
	    }
	}
	return values;
    }
}