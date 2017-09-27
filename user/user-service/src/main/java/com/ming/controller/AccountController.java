package com.ming.controller;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("account")
public class AccountController {

    @Autowired
    private DiscoveryClient discoveryClient;
    @GetMapping(value = "login")
    public String login(String username) throws InterruptedException {
        System.out.println(username);
        List<Date> list = Lists.newArrayList();
        list.sort(Date::compareTo);
        //长时间休眠  触发熔断
        //Thread.sleep(30000);
        return username+ JSON.toJSONString(discoveryClient.getLocalServiceInstance());
    }
}
