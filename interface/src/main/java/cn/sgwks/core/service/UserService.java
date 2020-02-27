package cn.sgwks.core.service;

import cn.sgwks.core.pojo.user.User;

public interface UserService {
    //发送短信验证码
    void sendCode(String phone);
    //判断用户手机收到的验证码和用户在注册界面输入的验证码是否匹配，匹配返回true，否则返回false
    boolean checkSmsCode(String smsCode, String phone);
    //前面2个要求都完成了就添加数据到用户表中
    void add(User user);
}
