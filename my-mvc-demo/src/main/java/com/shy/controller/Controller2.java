package com.shy.controller;

import com.shy.pojo.User;
import com.shy.mymvc.annotation.Controller;
import com.shy.mymvc.annotation.RequestMapping;
import com.shy.mymvc.annotation.ResponseBody;
import java.util.Date;

@Controller
public class Controller2 {

    @RequestMapping("/show.do")
    public String show2(String id,String username,Date date){
        return "redirect:edit.do";//可以重定向到别的路径
    }

    @RequestMapping("/postForm.do")
    @ResponseBody
    public int postForm(User user){
        System.out.println(user);//接受表单提交并封装到实体类中
        return 1;
    }

}
