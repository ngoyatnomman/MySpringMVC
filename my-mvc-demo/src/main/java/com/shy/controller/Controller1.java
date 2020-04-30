package com.shy.controller;

import com.shy.service.ServiceDemo;
import com.shy.mymvc.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
public class Controller1 {
    @Autowired
    private ServiceDemo service1;//默认通过类名小写注入，也可接受接口注入

    @RequestMapping("/add.do")
    public String add(HttpServletRequest request, HttpServletResponse response, HttpSession session,int id){
        System.out.println("id="+id);//参数id绑定成功
        System.out.println(request.getParameter("id"));//request对象可用
        service1.test();//二层依赖注入成功
        System.out.println("add方法执行完毕");
        return "redirect:index.jsp";//重定向可用
    }

    @RequestMapping("/edit.do")
    @ResponseBody
    public List<Integer> edit(String aaa, @DateTimeFormat("yyyy-MM-dd")Date date){//调用不存在的参数不会报错，日期注解可用
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        System.out.println(date);
        System.out.println("edit方法执行完毕");
        return list;//返回json可用
    }

    @RequestMapping("/delete.do")
    public String delete(@RequestParam("userId") String bbb){
        System.out.println(bbb);//RequestParam注解可用
        System.out.println("delete方法执行完毕");
        return "forward:index.jsp";//转发可用
    }

    @RequestMapping("/check.do")
    public String check(HashMap map){
        System.out.println(map);//map封装可用
        System.out.println("check方法执行完毕");
        return "redirect:index.jsp";
    }
}
