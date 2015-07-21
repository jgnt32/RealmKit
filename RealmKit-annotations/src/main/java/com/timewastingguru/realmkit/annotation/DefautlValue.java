package com.timewastingguru.realmkit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by artoymtkachenko on 15.07.15.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface DefautlValue {

    String defaultStringValue() default "";
    int defaultIntValue() default 0;
    boolean defaulBooltValue() default false;
    long defaulLongtValue() default 0L;
    char defaulCharValue() default ' ';
    int[] getDefaultIntArray() default {};
    String[] getDefaultString() default {};

}
