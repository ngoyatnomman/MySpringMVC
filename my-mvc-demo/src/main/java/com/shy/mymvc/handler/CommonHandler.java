package com.shy.mymvc.handler;

import com.shy.mymvc.annotation.DateTimeFormat;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 一些公共的处理方法
 * */
class CommonHandler {
    /**
     * @param type Class类型
     * 获取基本类型的包装类
     * */
    private Class getBasicType(Class type){
        Class[] packageClasses = {Integer.class,Double.class,Boolean.class,Byte.class,Long.class,Float.class};
        for (Class packageClass : packageClasses) {
            try {
                if(type == packageClass || type == packageClass.getField("TYPE").get(null)){
                    return packageClass;
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * @param type Class类型
     * 判断是否是基本类型
     * */
    boolean isBasicType(Class type){
        Class[] packageClasses = {Integer.class,Double.class,Boolean.class,Byte.class,Long.class,Float.class};
        for (Class packageClass : packageClasses) {
            try {
                if(type == packageClass || type == packageClass.getField("TYPE").get(null)){
                    return true;
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return type == String.class || type == Object.class;
    }

    /**
     * @param  source 当前value值(字符串)
     *  @param type Class类型
     * 基本类型处理
     * */
    Object basicTypeHandler(String source, Class type) {
        try {
            Class basicType = getBasicType(type);
            if(basicType != null){
                return basicType.getDeclaredConstructor(String.class).newInstance(source);
            }else if(type == String.class || type == Object.class){
                return source;
            }
        } catch (InstantiationException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param element 可以被注解的元素，在这里可以是方法参数或者类属性
     * @param source 请求值
     *日期类型处理
     * */
    Date dateTypeHandler(String source, AnnotatedElement element){
        if (element.isAnnotationPresent(DateTimeFormat.class)) {
            DateTimeFormat annotation = element.getAnnotation(DateTimeFormat.class);
            String pattern = annotation.value();
            SimpleDateFormat formater = new SimpleDateFormat(pattern);
            Date date = null;
            try {
                date = formater.parse(source);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            return date;
        }
        return null;
    }

    /**
     * Map类型处理
     * */
    Map<String,Object> mapTypeHandler(Map<String, String[]> parameterMap) {
        Map<String,Object> requestMap = new HashMap<>();
        for (Map.Entry entry:parameterMap.entrySet()){
            String key = (String) entry.getKey();
            String[] values = (String[])entry.getValue();
            if(values.length == 1){
                requestMap.put(key,values[0]);
            }else if(values.length == 0){
                requestMap.put(key,null);
            }else {
                requestMap.put(key,values);
            }
        }
        return requestMap;
    }
}
