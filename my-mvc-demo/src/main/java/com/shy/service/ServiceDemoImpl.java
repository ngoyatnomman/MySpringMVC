package com.shy.service;

import com.shy.mapper.Mapper1;
import com.shy.mymvc.annotation.Autowired;
import com.shy.mymvc.annotation.Service;

@Service
public class ServiceDemoImpl implements ServiceDemo {
    @Autowired
    private Mapper1 mapper1;

    public void test2(){
        System.out.println("哈哈哈哈哈哈哈");
    }

    @Override
    public void test(){
        mapper1.test();
    }
}
