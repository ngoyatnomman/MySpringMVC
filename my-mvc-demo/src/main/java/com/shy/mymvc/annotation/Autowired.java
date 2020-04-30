package com.shy.mymvc.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)//在属性上的注解
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
    boolean required() default true;
}
