package com.shy.mymvc.pojo;

import java.lang.reflect.Method;
import java.util.Arrays;

public class MappedMethod {

    private Method method;//method对象
    private String[] paramNames;//形参名数组
    private Object bean;//bean对象

    public MappedMethod(Method method, String[] paramNames, Object bean) {
        this.method = method;
        this.paramNames = paramNames;
        this.bean = bean;
    }

    public MappedMethod() {
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String[] getParamNames() {
        return paramNames;
    }

    public void setParamNames(String[] paramNames) {
        this.paramNames = paramNames;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    @Override
    public String toString() {
        return "MappedMethod{" +
                "method=" + method +
                ", paramNames=" + Arrays.toString(paramNames) +
                ", bean=" + bean +
                '}';
    }
}
