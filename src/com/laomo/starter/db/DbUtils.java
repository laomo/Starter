package com.laomo.starter.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

import com.laomo.starter.db.annotation.Column;
import com.laomo.starter.db.annotation.Table;

public class DbUtils {

    /**
     * 获得clazz类的fields1和父类的fields2去重合计，实现过滤掉非Column字段
     * @param clazz
     * @return
     */
    public static List<Field> joinFields(Class<?> clazz) {
	Field[] fields1 = clazz.getDeclaredFields();
	Field[] fields2 = clazz.getSuperclass().getDeclaredFields();

	Map<String, Field> map = new LinkedHashMap<String, Field>();
	for (Field field : fields1) {
	    // 过滤掉非Column定义的字段
	    if (!field.isAnnotationPresent(Column.class)) {
		continue;
	    }
	    map.put(field.getName(), field);
	}
	for (Field field : fields2) {
	    // 过滤掉非Column定义的字段
	    if (!field.isAnnotationPresent(Column.class)) {
		continue;
	    }
	    if (!map.containsKey(field.getName())) {
		map.put(field.getName(), field);
	    }
	}
	List<Field> list = new ArrayList<Field>();
	list.addAll(map.values());
	return list;
    }

    public static String getLogSql(String sql, Object[] args) {
	if (args == null || args.length == 0) {
	    return sql;
	}
	for (int i = 0; i < args.length; i++) {
	    sql = sql.replaceFirst("\\?", "'" + String.valueOf(args[i]) + "'");
	}
	return sql;
    }

    /**
     * 要建表的bean都要设置@Table注解
     * @see Table
     * @param clazz
     * @return true
     */
    public static boolean isTableAnnotationPresent(Class<?> clazz) {
	if (clazz.isAnnotationPresent(Table.class)) {
	    return true;
	} else {
	    throw new RuntimeException(clazz.getSimpleName() + "未设置@Table注解！");
	}
    }

    /**
     * 内容生成规则：list的item用*##*分隔组装成字符串
     * @param list 需要转成字符串保存的list
     * @return
     */
    public static String getStringFormList(List<?> list) {
	if (isListEmpty(list)) {
	    return "";
	} else {
	    StringBuffer sBuffer = new StringBuffer();
	    int count = list.size();
	    for (int i = 0; i < count; i++) {
		sBuffer.append(list.get(i)).append("####");
	    }
	    int end = sBuffer.length();
	    int start = end - 4;
	    sBuffer.delete(start, end);
	    return sBuffer.toString();
	}

    }

    /**
     * 内容生成规则：用*##*分割成字符串组装list
     * @param <T>
     * @param list 需要转成字符串保存的list
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getListFormString(String string, Class<T> clazz) {
	List<T> list = new ArrayList<T>();
	if (!TextUtils.isEmpty(string)) {
	    String[] strings = string.split("####");
	    if (strings != null) {
		if ((Integer.TYPE == clazz) || (Integer.class == clazz)) {
		    for (String s : strings) {
			list.add((T) Integer.valueOf(s));
		    }
		} else if (String.class == clazz) {
		    list = (List<T>) Arrays.asList(strings);
		} else if ((Long.TYPE == clazz) || (Long.class == clazz)) {
		    for (String s : strings) {
			list.add((T) Long.valueOf(s));
		    }
		} else if ((Float.TYPE == clazz) || (Float.class == clazz)) {
		    for (String s : strings) {
			list.add((T) Float.valueOf(s));
		    }
		} else if ((Short.TYPE == clazz) || (Short.class == clazz)) {
		    for (String s : strings) {
			list.add((T) Short.valueOf(s));
		    }
		} else if ((Double.TYPE == clazz) || (Double.class == clazz)) {
		    for (String s : strings) {
			list.add((T) Double.valueOf(s));
		    }
		}
	    }
	}
	return list;
    }

    /**
     * 外键为空，如果没有设置外键值是正常情况，如果设置外键值说明需要@table注解设置外键或者不要传入外键！ 外键不为空，如果设置过外键值的话，foreignId！=null，但是可以为空串“”
     * @param foreignKey 外键
     * @param foreignId 外键值
     * @return
     */
    public static boolean hasForeignKey(String foreignKey, String foreignId) {
	if (TextUtils.isEmpty(foreignKey)) {
	    if (TextUtils.isEmpty(foreignId)) {
		return false;
	    } else {
		throw new RuntimeException("请用@table注解设置外键或者不要传入外键！");
	    }
	} else {
	    //foreignId允许为空字符串“”
	    if (foreignId == null) {
		return false;
	    }
	}
	return true;
    }

    /**
     * list是否为”空“
     * @param list
     * @return
     */
    public static boolean isListEmpty(List<?> list) {
	return list == null || list.size() == 0;
    }
}
