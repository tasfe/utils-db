package com.diwayou.utils.db.dao.impl;

import com.diwayou.utils.db.dao.ItemDao;
import com.diwayou.utils.db.domain.Item;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by cn40387 on 15/9/11.
 */
public class ItemDaoImplTest {

    private ClassPathXmlApplicationContext classPathXmlApplicationContext;

    private ItemDao itemDao;

    @Before
    public void before() {
        classPathXmlApplicationContext = new ClassPathXmlApplicationContext("classpath:/ctx-root.xml");

        itemDao = classPathXmlApplicationContext.getBean("itemDao", ItemDao.class);
    }

    @After
    public void after() {
        classPathXmlApplicationContext.close();
    }

    @Test
    public void insertTest() {
        Item item = new Item();
        item.setId(2L);
        item.setName("diwayou");
        item.setUrl("123");

        itemDao.insert(item);
    }

    @Test
    public void getTest() {
        Item item = itemDao.get(2L);

        System.out.println(item);
    }

    @Test
    public void distributionTest() {
        Item item = new Item();
        item.setName("diwayou");
        item.setUrl("123");

        for (long id = 1; id < 100; id++) {
            item.setId(id);
            itemDao.insert(item);
        }
    }
}
