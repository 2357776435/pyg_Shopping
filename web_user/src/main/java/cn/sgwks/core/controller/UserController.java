package cn.sgwks.core.controller;

import cn.sgwks.core.pojo.entity.Result;
import cn.sgwks.core.pojo.user.User;
import cn.sgwks.core.service.UserService;
import cn.sgwks.core.util.PhoneFormatCheckUtils;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.regex.PatternSyntaxException;

@RestController
@RequestMapping("/user")
public class UserController {
    @Reference
    private UserService userService;

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return
     */
    @RequestMapping("/sendCode")
    public Result sendCode(String phone) {
        try {
//            if (phone == null || "".equals(phone)) {//如果手机为空或者等于空串
//                return new Result(false, "手机号不能为空!");
//            }
//            if (!PhoneFormatCheckUtils.isPhoneLegal(phone)) {//如果手机格式不匹配执行格式不正确
//                return new Result(false, "手机号格式不正确!");
//            }
            userService.sendCode(phone);
            return new Result(true, "发送成功!");
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
            return new Result(false, "发送失败!");
        }
    }

    /**
     * 添加用户
     *
     * @param user    用户对象
     * @param smsCode 验证码
     * @return
     */
    @RequestMapping("/add")
    public Result add(String smsCode, @RequestBody User user) {
        try {
            //判断用户手机收到的验证码和用户在注册界面输入的验证码是否匹配，匹配返回true，否则返回false
            boolean isCheck = userService.checkSmsCode(smsCode, user.getPhone());//传递表单用户对象中的手机号
            if (!isCheck) {//如果返回false
                return new Result(false, "手机号或者验证码不正确!");
            }
            //先默认设置用户的基本信息
            user.setStatus("Y");//使用状态（Y正常 N非正常）
            user.setCreated(new Date());//创建时间
            user.setUpdated(new Date());//修改时间
            user.setSourceType("1");//会员来源：1:PC，2：H5，3：Android，4：IOS，5：WeChat
            userService.add(user);
            return new Result(true, "用户注册成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "用户注册失败!");
        }
    }
}
