package cn.sgwks.core.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
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
        if ("anonymousUser".equals(loginName)){
            map.put("loginName","");
        }else {
            map.put("loginName",loginName);
        }
        return map;
    }
}
