package com.laomo.starter.db;

import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.laomo.starter.db.annotation.Extra;
import com.laomo.starter.db.annotation.ForeignListField;
import com.laomo.starter.db.annotation.Table;

public class TableHelper {

    private static TableHelper mTableHelper = null;
    private static List<Class<?>> mClasses;

    public static TableHelper getInstance(List<Class<?>> clazzes) {
	if (mTableHelper == null) {
	    mTableHelper = new TableHelper(clazzes);
	}
	return mTableHelper;
    }

    private TableHelper(List<Class<?>> clazzes) {
	mClasses = clazzes;
    }

    public void createTablesByClasses(SQLiteDatabase db) {
	for (Class<?> clazz : mClasses) {
	    db.execSQL(getCreateTableSql(clazz));
	}
    }

    public void dropTablesByClasses(SQLiteDatabase db) {
	for (Class<?> clazz : mClasses) {
	    db.execSQL(getDropTableSql(clazz));
	}
    }

    public static <T> String getCreateTableSql(Class<T> clazz) {
	Table table = null;
	String tableName = "";
	String primaryKey = "";
	String foreignKey = "";
	//主键是否自增长
	boolean primaryKeyAutoIncrement = false;
	if (DbUtils.isTableAnnotationPresent(clazz)) {
	    table = (Table) clazz.getAnnotation(Table.class);
	    tableName = table.name();
	    primaryKey = table.primaryKey();
	    primaryKeyAutoIncrement = table.primaryKeyAutoIncrement();
	    foreignKey = table.foreignKey();
	    //TODO:外键要不要强制命名，保证和主键不会重名？
	    if (foreignKey.equals("_id") || foreignKey.equals("id")) {
		throw new RuntimeException("外键名不可以和主键名相同！");
	    }
	}

	StringBuilder sb = new StringBuilder();
	sb.append("create table if not exists ").append(tableName).append(" (");

	List<Field> allFields = DatabaseHelper.getClassFields(clazz);

	for (Field field : allFields) {
	    if (field.isAnnotationPresent(ForeignListField.class)) {
		ForeignListField foreignListField = field.getAnnotation(ForeignListField.class);
		Class<?> itemClass = foreignListField.item();
		/**
		 * 基本类型的列表，转换成字符串保存到本表;非基本类型的列表保存在额外的表里，此处不做处理 基本类型的列表表中列名字生成规则：List+field的名字
		 */
		if (itemClass.isPrimitive() || itemClass == String.class) {
		    sb.append("List").append(field.getName()).append(" Text,");
		}
		continue;
	    } else if (field.isAnnotationPresent(Extra.class)) {
		//保存在额外的表中，此处不做处理
		continue;
	    }

	    String columnType = getColumnType(field.getType());
	    String columnName = field.getName();
	    sb.append(columnName + " " + columnType);

	    if (columnName.equals(primaryKey)) {
		if (primaryKeyAutoIncrement) {
		    sb.append(" primary key autoincrement,");
		} else {
		    /**
		     * 按照SQL标准, 主键(PRIMARY KEY)应该暗含NOT NULL, 不幸地是,在SQLite中, 因为长久以来的编码疏忽, 导致 主键可以是NULL的; 所以在此强制设置为不为空
		     */
		    sb.append(" no null,");
		}
	    } else {
		sb.append(",");
	    }
	}

	if (!TextUtils.isEmpty(foreignKey)) {//有外键，添加sql
	    //如果还没有添加foreignKey,在此处添加
	    if (sb.indexOf(foreignKey) == -1) {
		sb.append(foreignKey + " TEXT,");
	    }
	}

	//每张表都有accountId字段，保证数据独立
	sb.append("accountId TEXT");
	//联合主键保证多帐号登录数据唯一性
	if (primaryKeyAutoIncrement) {
	    sb.append(")");
	} else {
	    sb.append(",primary key (").append(primaryKey).append(",accountId))");
	}
	String sql = sb.toString();

	//Log.d("crate table [" + tableName + "]: " + sql);

	return sql;
    }

    public static <T> String getDropTableSql(Class<T> clazz) {
	String tableName = "";
	if (DbUtils.isTableAnnotationPresent(clazz)) {
	    Table table = (Table) clazz.getAnnotation(Table.class);
	    tableName = table.name();
	}
	String sql = "DROP TABLE IF EXISTS " + tableName;
	//Ln.d("dropTable[" + tableName + "]:" + sql);
	return sql;
    }

    private static String getColumnType(Class<?> fieldType) {
	if (String.class == fieldType) {
	    return "TEXT";
	}
	if ((Integer.TYPE == fieldType) || (Integer.class == fieldType)) {
	    return "INTEGER";
	}
	if ((Long.TYPE == fieldType) || (Long.class == fieldType)) {
	    return "INTEGER";
	}
	if ((Float.TYPE == fieldType) || (Float.class == fieldType)) {
	    return "FLOAT";
	}
	if ((Short.TYPE == fieldType) || (Short.class == fieldType)) {
	    return "INT";
	}
	if ((Double.TYPE == fieldType) || (Double.class == fieldType)) {
	    return "DOUBLE";
	}
	if (Blob.class == fieldType) {
	    return "BLOB";
	}

	return "TEXT";
    }

}