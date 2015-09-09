package com.diwayou.utils.db.dao.impl;

import com.diwayou.utils.db.dao.UserDao;
import com.diwayou.utils.db.domain.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by cn40387 on 15/9/9.
 */
public class UserDaoImplTest {

    private ClassPathXmlApplicationContext classPathXmlApplicationContext;

    private UserDao userDao;

    @Before
    public void before() {
        classPathXmlApplicationContext = new ClassPathXmlApplicationContext("classpath:/ctx-root.xml");

        userDao = classPathXmlApplicationContext.getBean("userDao", UserDao.class);
    }

    @After
    public void after() {
        classPathXmlApplicationContext.close();
    }

    @Test
    public void insertTest() {
        User user = new User();
        user.setId(1L);
        user.setName("diwayou");
        user.setPassword("123");

        userDao.insert(user);
    }

    @Test
    public void getTest() {
        User user = userDao.get(1L);

        System.out.println(user);
    }
}
