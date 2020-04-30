package com.shy.mymvc.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Controller执行结果处理
 * */
public class ResultHandler {
    /**
     * 返回json处理器
     * */
    public void jsonHandler(Object data, HttpServletResponse response) throws IOException {
        //创建json文件转换的对象
        ObjectMapper om=new ObjectMapper();
        //将对象转换为json格式的字符串
        String str=om.writeValueAsString(data);
        //       创建输出流对象
        PrintWriter out=response.getWriter();
        out.print(str);
    }

    /**
     * 跳转指令处理器
     * */
    public void jumpHandler(String jumpCode, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if(jumpCode.startsWith("redirect:")){
            response.sendRedirect(jumpCode.replace("redirect:",""));
        }else if(jumpCode.startsWith("forward:")){
            request.getRequestDispatcher(jumpCode.replace("forward:","")).forward(request,response);
        }
    }
}
