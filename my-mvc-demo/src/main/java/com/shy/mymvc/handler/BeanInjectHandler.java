package com.shy.mymvc.handler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

/**
* 注入实体类
* */
public class BeanInjectHandler {
    private CommonHandler commonHandler;

    public BeanInjectHandler(CommonHandler commonHandler) {
        this.commonHandler = commonHandler;
    }

    /**
     * @param fieldName 属性名字符串
     * @return 方法名
     * 通过属性名获取对应的方法名
     * */
    private String getMethodName(String fieldName){
        return "set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
    }

    /**
     * @return 实例化并注入完成的对象
     * */
    @SuppressWarnings("unchecked")
    public Object injectBean(Map<String, String[]> parameterMap, Class beanClass){
        Object bean = null;
        try {
            bean = beanClass.getDeclaredConstructor().newInstance();
            Field[] declaredFields = beanClass.getDeclaredFields();
            for (Field field : declaredFields) {
                String fieldName = field.getName();
                String methodName = getMethodName(field.getName());
                Method method = null;
                Class type = field.getType();
                try {
                    method = beanClass.getMethod(methodName,type);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                Object target = null;
                String[] requestValues = parameterMap.get(fieldName);
                if(method != null){
                    if(commonHandler.isBasicType(type)){
                        if(requestValues != null && requestValues.length == 1){
                            target = commonHandler.basicTypeHandler(requestValues[0],type);
                        }
                    }else if (Date.class.isAssignableFrom(type)) {
                        if(requestValues != null && requestValues.length == 1){
                            target = commonHandler.dateTypeHandler(requestValues[0],field);
                        }
                    }
                    method.invoke(bean,target);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return bean;
    }

}
