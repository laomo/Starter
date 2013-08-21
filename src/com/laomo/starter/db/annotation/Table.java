package com.laomo.starter.db.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.TYPE })
public @interface Table {
    /**
     * 表名
     * @return
     */
    public abstract String name();
    
    /**
     * 主键
     * @return
     */
    public abstract String primaryKey();
    
    /**
     * 主键是否自增长
     * @return
     */
    public abstract boolean primaryKeyAutoIncrement() default false;
    /**
     * 外键
     * @return
     */
    public abstract String foreignKey() default "";
    
}
