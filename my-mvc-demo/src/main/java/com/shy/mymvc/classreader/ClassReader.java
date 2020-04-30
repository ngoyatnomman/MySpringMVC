package com.shy.mymvc.classreader;

import com.shy.controller.Controller1;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* 通过类名全路径读取class文件找到所有方法形参
* 无法通过反射获取形参名
* */
public class ClassReader {

    private String classFilePath;//class文件路径
    private StringBuffer sb;//字符串缓存
    private byte[] codeBytes;//文件字节数组
    private int index;//文件字节指针
    private Map<Integer,String> constantPoolMap;//常量池Map
    private int loacalVariableIndex;//本地变量索引值
    private Map<String,Integer> methodParamMap;//方法名和参数个数的Map
    private Map<String, String[]> resultMap;//结果Map

    /**
    * 进行一些初始化参数
    * */
    {
        sb = new StringBuffer();//字符串缓存初始化
        index = 10;//跳过magic数，主要版本，次要版本
        constantPoolMap =  new HashMap<>();//常量池初始化
        resultMap = new HashMap<>();//结果Map初始化
    }

    private void initClass(){
        sb.setLength(0);
        constantPoolMap.clear();
        resultMap.clear();
        index = 10;
    }

    /**
    * 遍历分析Class，反射获取参数描述
    * */
    private void getMethodParamMap(Class clazz){
        methodParamMap = new HashMap<>();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if(method.getDeclaringClass()==clazz){//只要本类的方法，不要父类的方法
                int paramCount = method.getParameterCount();
                String methodName = method.getName();
                methodParamMap.put(methodName,paramCount);
            }
        }
    }

    /**
    * 通过类名全路径找到真实的class文件路径
    * */
    private void getClassFilePath(Class clazz){
        String pkg = clazz.getName().replaceAll("\\.","/")+".class";
        URL url = this.getClass().getClassLoader().getResource(pkg);
        classFilePath = url.getFile();
    }

    /**
    * 读取字节数组
    * */
    private void readBytes(){
        try {
            FileInputStream fis = new FileInputStream(classFilePath);
            int fileSize = fis.available();
            codeBytes = new byte[fileSize];
            fis.read(codeBytes);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * 任意个字节的数合并转为int长度
    * */
    private int getLength(int from ,int to) {
        int s = 0;
        for (int i = from; i < to; i++) {
            s <<= 8;
           s |= (codeBytes[i] & 0xff);
        }
        return s;
    }

    /**
    * 合并成字符串
    * */
    private String joinStr(int from , int to) {
        sb.setLength(0);
        for (int i = from; i < to; i++) {
            String s = Integer.toHexString(codeBytes[i] & 0xFF);
            s = s.length() > 1? s:'0'+s;
            sb.append(s);
        }
        return sb.toString();
    }

    /**
    * 获取常量池
    * */
    private void getConstantPoolMap(){
        int poolCount = 1;
        boolean flag = true;
        while(flag){
            switch (codeBytes[index]){
                case 1://CONSTANT_Utf8_info
                    int length = getLength(index+1,index+3);
                    String data = new String(codeBytes,index+3,length);
                    if("LocalVariableTable".equals(data)){
                        loacalVariableIndex = poolCount;
                    }
                    constantPoolMap.put(poolCount,data);
                    index += length+3;
                    poolCount ++;
                    break;
                case 3://CONSTANT_Integer_info
                case 4://CONSTANT_Float_info
                case 9://CONSTANT_Fieldref_info
                case 10://CONSTANT_Methodref_info
                case 11://CONSTANT_InterfaceMethodref_info
                case 12://CONSTANT_NameAndType_info
                case 17://CONSTANT_Dynamic_info
                case 18://CONSTANT_InvokeDynamic_info
                    index += 5;
                    poolCount ++;
                    break;
                case 7://CONSTANT_Class_info
                case 8://CONSTANT_String_info
                case 16://CONSTANT_MethodType_info
                case 19://CONSTANT_Module_info
                case 20://CONSTANT_Package_info
                    index += 3;
                    poolCount ++;
                    break;
                case 5://CONSTANT_Long_info
                case 6://CONSTANT_Double_info
                    index += 9;
                    poolCount ++;
                    break;
                case 15://CONSTANT_MethodHandler_info
                    index += 4;
                    poolCount ++;
                    break;
                default:
                    flag = false;
                    break;
            }
            if(!flag){
                break;
            }
        }
//        for(Map.Entry entry:constantPoolMap.entrySet()){
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue());
//        }
    }

    /**
    * 跳过属性描述
    * */
    private void attributeSkipper(){
        index += 6;
        int attributesCount = getLength(index,index+2);
        index += 2;
        if(attributesCount > 0){
            for (int j = 0; j < attributesCount; j++) {
                index += 2;
                int attributeLength = getLength(index,index+4);
                index += 4+attributeLength;
            }
        }
    }

    /**
    * 获取参数信息
    * */
    public Map<String, String[]> getResultMap(Class clazz){
        initClass();
        getClassFilePath(clazz);
        getMethodParamMap(clazz);
        readBytes();
        getConstantPoolMap();
        index += 8;
        int fieldsCount = getLength(index,index+2);
        index += 2;
        for (int i = 0; i < fieldsCount; i++) {
            attributeSkipper();
        }
        int methodCount = getLength(index,index+2);
        index += 2;
        for (int i = 0; i < methodCount; i++) {
            int nameIndex = getLength(index+2,index+4);
            String methodName = constantPoolMap.get(nameIndex);
            if(methodParamMap.containsKey(methodName)){
                index += 6;
                int attributeCount = getLength(index,index+2);
                index +=2;
                for (int j = 0; j < attributeCount ; j++) {
                    int attributeNameIndex = getLength(index,index+2);
                    String attributeName = constantPoolMap.get(attributeNameIndex);
                    index += 2;
                    if("Code".equals(attributeName)){
                        int paramCount = methodParamMap.get(methodName);
                        int attributeLength = getLength(index,index+4);
                        index += 4;
                        String info = joinStr(index,index+attributeLength);
                        sb.setLength(0);
                        sb.append(Integer.toHexString(loacalVariableIndex));
                        sb.append("[0-9a-f]{12}");
                        for (int k = 0; k < paramCount+1; k++) {
                            sb.append("0000[0-9a-f]{4}([0-9a-f]{4})[0-9a-f]{4}000");
                            sb.append(k);
                        }
                        Pattern pn = Pattern.compile(sb.toString());
                        Matcher matcher = pn.matcher(info);
                        String[] paramNames = new String[paramCount];
                        if(matcher.find()){
                            for (int k = 0; k < paramCount ; k++) {//这里要跳过第一个，因为类方法第一个参数永远为this
                                int paramIndex = Integer.parseInt(matcher.group(k+2),16);
                                String paramName = constantPoolMap.get(paramIndex);
                                paramNames[k] = paramName;
                            }
                        }
                        resultMap.put(methodName,paramNames);
                        index += attributeLength;
                    }else{
                        int attributeLength = getLength(index,index+4);
                        index += attributeLength+4;
                    }
                }
            }else{
                attributeSkipper();
            }
        }

        return resultMap;
    }

    /**
     *测试方法
     */
    public static void main(String[] args) {
        ClassReader reader = new ClassReader();
        Map<String, String[]> resultMap = reader.getResultMap(Controller1.class);
        System.out.println(resultMap);
    }
}
