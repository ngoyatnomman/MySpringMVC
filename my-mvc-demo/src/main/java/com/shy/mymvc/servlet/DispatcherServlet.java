package com.shy.mymvc.servlet;

import com.shy.mymvc.annotation.*;
import com.shy.mymvc.classreader.ClassReader;
import com.shy.mymvc.handler.ParameterTypeHandler;
import com.shy.mymvc.handler.ResultHandler;
import com.shy.mymvc.pojo.MappedMethod;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
* 负责拦截请求，并分发下去
* */
public class DispatcherServlet extends HttpServlet {

    private Properties config = new Properties();
    private List<String> classNames = new ArrayList<>();
    private Map<String,Object> beans = new HashMap<>();//beans就是IOC容器
    private Map<String,MappedMethod> handlerMap = new HashMap<>();

    @Override
    public void init()  {
        //读取配置文件
        loadProperty("mymvcConfig.properties");
        String basePack = (String) config.get("context.basePackage");
        String mapperPack = (String)config.get("mapperScanner.basePackage");
        //扫描所有类
        scanPackage(basePack);
        //实例化bean
        doInstance(mapperPack);
        //依赖注入
        doAutoWired();
        //地址映射
        urlMapping();
    }

    @SuppressWarnings("unchecked")
    private Object getBeanByInterface(Class interfaceClass){
        for(Map.Entry<String,Object> entry:beans.entrySet()){
            if(interfaceClass.isAssignableFrom(entry.getValue().getClass())){
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 依赖注入
     * */
    private void doAutoWired() {
        for(Map.Entry<String,Object> entry:beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if(field.isAnnotationPresent(Autowired.class)){
                    Object bean;
                    if(field.getType().isInterface()){//如果属性是接口
                        bean = getBeanByInterface(field.getType());
                    }else {
                        String key = getDefaultBeanName(field.getType());
                        bean = beans.get(key);
                    }
                    if(bean == null){
                        throw new RuntimeException("找不到要注入的类！");
                    }
                    field.setAccessible(true);
                    try {
                        field.set(instance,bean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @param  propertyName 属性文件名称
     * 加载properties文件
     */
    private void loadProperty(String propertyName){
        InputStream inputStream = this.getClass().getResourceAsStream("/"+propertyName);
        try {
            config.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param basePackage 要扫描的包名
     *扫描包下的类
     * */
    private void scanPackage(String basePackage) {
        URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/"));
        String fileStr = url.getFile();
        File file = new File(fileStr);
        String[] filesStr = file.list();
        for (String path : filesStr) {
            File filePath = new File(fileStr,path);
            if(filePath.isDirectory()){
                scanPackage(basePackage+"."+path);
            }else{
                classNames.add(basePackage+"."+filePath.getName());
            }
        }
    }

    /**
     * 实例化每个bean
     * */
    private void doInstance(String mapperPack) {
        for (String className : classNames) {
            //.class后缀去掉
            String cn = className.substring(0,className.length() - 6);
            try {
                Class<?> clazz = Class.forName(cn);
                if(clazz.isAnnotationPresent(Controller.class)){
                    //就是Controller类
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    String key = getDefaultBeanName(clazz);
                    beans.put(key,instance);
                }else if(clazz.isAnnotationPresent(Service.class)){
                    //就是Service类
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    String key = getDefaultBeanName(clazz);
                    beans.put(key,instance);
                }else if(clazz.getPackage().getName().equals(mapperPack)){
                    //mapper包下的类
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    String key = getDefaultBeanName(clazz);
                    beans.put(key,instance);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param  clazz Class对象
     * 获取默认的bean名
     * */
    private String getDefaultBeanName(Class clazz){
        String simpleName = clazz.getSimpleName();
        return simpleName.substring(0,1).toLowerCase()+simpleName.substring(1);
    }

    /**
     * 映射URL到方法
     * */
    private void urlMapping() {
        ClassReader reader = new ClassReader();
        for(Map.Entry<String,Object> entry:beans.entrySet()){
            Object instance = entry.getValue();

            Class<?> clazz = instance.getClass();

            if(clazz.isAnnotationPresent(Controller.class)){
                Map<String, String[]> paramNameMap = reader.getResultMap(clazz);

                String classPath = "";
                if(clazz.isAnnotationPresent(RequestMapping.class)){
                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                    classPath = requestMapping.value();//类上面的路径
                }

                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if(method.isAnnotationPresent(RequestMapping.class)){
                        RequestMapping mreq = method.getAnnotation(RequestMapping.class);
                        String methodPath = mreq.value();
                        String[] paramNames = paramNameMap.get(method.getName());
                        MappedMethod mappedMethod = new MappedMethod(method,paramNames,instance);
                        handlerMap.put(classPath+methodPath,mappedMethod);
                        System.out.println("映射路径"+classPath+methodPath+"到方法"+method.toString());
                    }
                }
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request,response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getServletPath();

        MappedMethod mappedMethod = handlerMap.get(url);

        ParameterTypeHandler paramTypeHandler = new ParameterTypeHandler();

        Object[] args = paramTypeHandler.getArgs(mappedMethod,request,response);//获取参数列表

        Method method = mappedMethod.getMethod();

        Object o = mappedMethod.getBean();

        Object result = null;
        try {
            result = method.invoke(o, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        ResultHandler resultHandler = new ResultHandler();
        if(method.isAnnotationPresent(ResponseBody.class)){
            resultHandler.jsonHandler(result,response);
        }else{
            resultHandler.jumpHandler((String)result,request,response);
        }
    }
}
