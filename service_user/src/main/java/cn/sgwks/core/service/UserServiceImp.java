package cn.sgwks.core.service;

import cn.sgwks.core.dao.user.UserDao;
import cn.sgwks.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ActiveMQQueue smsDestination;//点对点消息中中间件的对象名,里面的值是sms
    //获取Common里面properties里面的sms配置文件template_code
    @Value("${template_code}")
    private String template_code;//模版CODE
    @Value("${sign_name}")
    private String sign_name;//签名名称
    @Autowired
    private UserDao userDao;

    /**
     * 发送短信验证码
     *
     * @param phone 手机号
     * @return
     */
    @Override
    public void sendCode(String phone) {
        //1. 生成一个随机6位的数字, 作为验证码
        StringBuffer sb = new StringBuffer();
        for (int i = 1; i < 7; i++) {
            int s = new Random().nextInt(10);
            sb.append(s);
        }
        final String smsCode = sb.toString();//设置常量生成后不允许被修改
        //2. 手机号作为key, 验证码作为value保存到redis中, 生存时间为5分钟,计时单位为秒
        redisTemplate.boundValueOps(phone).set(sb.toString(), 5 * 60, TimeUnit.SECONDS);
        //3. 将手机号, 短信内容, 模板编号, 签名封装成map消息发送给消息服务器
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile", phone);
                mapMessage.setString("template_code", template_code);//模板编码
                mapMessage.setString("sign_name", sign_name);//签名
                Map map = new HashMap();
                map.put("code", smsCode);//验证码
                mapMessage.setString("param", JSON.toJSONString(map));
                return (Message) mapMessage;
            }
        });
    }

    /**
     * 判断用户手机收到的验证码和用户在注册界面输入的验证码是否匹配，匹配返回true，否则返回false
     *
     * @param smsCode 用户输入的验证按
     * @param phone   根据redis数据中的验证码
     * @return
     */
    @Override
    public boolean checkSmsCode(String smsCode, String phone) {
        //判断手机不能为空，或者验证码不能为空，获取手机不能为空串，验证码不能为空串
        if (phone == null || smsCode == null || "".equals(phone) || "".equals(smsCode)) {
            return false;
        }
        //1. 根据手机号到redis中获取我们自己存的验证码
        String redisSmsCode = (String) redisTemplate.boundValueOps(phone).get();
        //2. 判断页面传入的验证码和我们自己存的验证码是否一致
        if (smsCode.equals(redisSmsCode)) {
            return true;
        }
        return false;
    }

    /**
     * 完成注册
     *
     * @param user
     */
    @Override
    public void add(User user) {
        userDao.insertSelective(user);
    }
}
