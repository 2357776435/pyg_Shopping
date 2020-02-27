package cn.sgwks;

import cn.sgwks.core.dao.user.UserDao;
import cn.sgwks.core.pojo.user.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/applicationContext-dao.xml")
public class TestDao {
    @Autowired
    private UserDao userDao;
    @Test
    public void testOne(){
        List<User> userList = userDao.selectByExample(null);
        for (User user : userList) {
            System.out.println(user);
        }
    }
}
