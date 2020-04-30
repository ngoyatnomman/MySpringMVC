package com.shy.mymvc.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)//在类上的注解
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    String value() default "";
}
