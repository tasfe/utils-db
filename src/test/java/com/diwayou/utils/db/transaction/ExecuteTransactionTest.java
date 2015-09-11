package com.diwayou.utils.db.transaction;

import com.diwayou.utils.db.dao.ItemDao;
import com.diwayou.utils.db.dao.UserDao;
import com.diwayou.utils.db.domain.Item;
import com.diwayou.utils.db.domain.User;
import com.diwayou.utils.db.sequence.impl.ShardSequence;
import com.diwayou.utils.db.shard.rule.DbRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionStatus;

/**
 * Created by diwayou on 2015/9/10.
 */
public class ExecuteTransactionTest {
    private ClassPathXmlApplicationContext classPathXmlApplicationContext;

    private ShardSequence shardSequence;

    private DbRule dbRule;

    private ShardTransactionTemplate shardTransactionTemplate;

    private UserDao userDao;

    private ItemDao itemDao;

    @Before
    public void before() {
        classPathXmlApplicationContext = new ClassPathXmlApplicationContext("classpath:/ctx-root.xml");

        shardSequence = classPathXmlApplicationContext.getBean("shardtestShardSequence", ShardSequence.class);

        dbRule = classPathXmlApplicationContext.getBean("shardtestDbRule", DbRule.class);

        shardTransactionTemplate = classPathXmlApplicationContext.getBean("shardTransactionTemplate", ShardTransactionTemplate.class);

        userDao = classPathXmlApplicationContext.getBean("userDao", UserDao.class);

        itemDao = classPathXmlApplicationContext.getBean("itemDao", ItemDao.class);
    }

    @After
    public void after() {
        classPathXmlApplicationContext.close();
    }

    @Test
    public void singleTableTransactionTest() {
        Long id = shardSequence.nextValue();
        final User user = new User();
        user.setId(id);
        user.setName("diwayou");
        user.setPassword("123");

        Exception e = shardTransactionTemplate.execute(id, new ShardTransactionCallback<Exception>() {
            @Override
            public DbRule getDbRule() {
                return dbRule;
            }

            @Override
            public Exception doInTransaction(TransactionStatus status) {
                try {
                    userDao.insert(user);
                    if (true) {
                        throw new RuntimeException("Simulate throw exception.");
                    }
                } catch (Exception ie) {
                    status.setRollbackOnly();
                    return ie;
                }

                return null;
            }
        });

        if (e != null) {
            e.printStackTrace();
        } else {
            System.out.println("Success");
        }
    }

    @Test
    public void multiTableTransactionTest() {
        Long id = shardSequence.nextValue();

        final User user = new User();
        user.setId(id);
        user.setName("diwayou");
        user.setPassword("123");

        final Item item = new Item();
        item.setId(id);
        item.setName("diwayou");
        item.setUrl("123");

        Exception e = shardTransactionTemplate.execute(id, new ShardTransactionCallback<Exception>() {
            @Override
            public DbRule getDbRule() {
                return dbRule;
            }

            @Override
            public Exception doInTransaction(TransactionStatus status) {
                try {
                    userDao.insert(user);
                    itemDao.insert(item);

                    if (true) {
                        throw new RuntimeException("Simulate throw exception.");
                    }
                } catch (Exception ie) {
                    status.setRollbackOnly();
                    return ie;
                }

                return null;
            }
        });

        if (e != null) {
            e.printStackTrace();
        } else {
            System.out.println("Success");
        }
    }
}
