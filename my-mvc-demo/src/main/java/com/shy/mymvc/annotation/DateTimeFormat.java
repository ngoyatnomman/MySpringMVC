package com.shy.mymvc.annotation;

import java.lang.annotation.*;

@Documented//注解能被挂载到doc文档中
@Target({ElementType.FIELD,ElementType.PARAMETER})//注解应用类型
@Retention(RetentionPolicy.RUNTIME)// 注解的类型
public @interface DateTimeFormat {
    String value() default "";
}
