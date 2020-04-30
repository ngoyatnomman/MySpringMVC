package com.shy.mymvc.handler;

import com.shy.mymvc.annotation.RequestParam;
import com.shy.mymvc.pojo.MappedMethod;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Parameter;
import java.util.*;

/***
 *按照不同的类型将值注入参数
 */
public class ParameterTypeHandler {

    private CommonHandler commonHandler;

    public ParameterTypeHandler(){
        this.commonHandler = new CommonHandler();
    }

    /**
     * 按照不同的类型将值写入数组
     * @return Object[] invoke执行所需的参数
     * */
    public Object[] getArgs(MappedMethod mappedMethod, HttpServletRequest request, HttpServletResponse response){
        Map<String, String[]> parameterMap = request.getParameterMap();//获取所有请求参数
        Parameter[] parameters = mappedMethod.getMethod().getParameters();
        Object[] acParams = new Object[parameters.length];//实参列表
        String[] paramNames = mappedMethod.getParamNames();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class type = param.getType();
            String paramName = paramNames[i];//参数名称
            if(param.isAnnotationPresent(RequestParam.class)){
                paramName = param.getAnnotation(RequestParam.class).value();
            }
            String[] requestValues = parameterMap.get(paramName);//字符串形式的参数值

            Object acParam = null;

            if(ServletRequest.class.isAssignableFrom(type)){//request类型
                acParam = request;
            }else if(ServletResponse.class.isAssignableFrom(type)){//response类型
                acParam = response;
            }else if(HttpSession.class.isAssignableFrom(type)){//session类型
                acParam = request.getSession();
            }else if(Map.class.isAssignableFrom(type)){//Map类型
                acParam = commonHandler.mapTypeHandler(parameterMap);
            }else if(List.class.isAssignableFrom(type)) {//List类型

            }else if(commonHandler.isBasicType(type)){//基本数据类型，包装类，String或Object
                if(requestValues != null && requestValues.length == 1){
                    acParam = commonHandler.basicTypeHandler(requestValues[0], type);
                }
            }else if(Date.class.isAssignableFrom(type)){//Date类型
                if(requestValues != null && requestValues.length == 1){
                    acParam = commonHandler.dateTypeHandler(requestValues[0],param);
                }
            }else{//实体类
                BeanInjectHandler beanInjectHandler = new BeanInjectHandler(this.commonHandler);
                acParam = beanInjectHandler.injectBean(parameterMap, type);
            }
            acParams[i] = acParam;
        }
        return acParams;
    }

}
