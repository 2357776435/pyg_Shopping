package cn.sgwks.core.controller;

import cn.sgwks.core.pojo.entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class UserLoginController {
    @RequestMapping("/showName")
    public Map showName(){
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<Object, Object> map = new HashMap<>();
        map.put("loginName",loginName);
        return map;
    }
}
