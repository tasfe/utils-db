package com.diwayou.utils.db.sequence;

import com.diwayou.utils.db.sequence.impl.ShardSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by cn40387 on 15/9/10.
 */
public class ShardSequenceTest {

    private ClassPathXmlApplicationContext classPathXmlApplicationContext;

    private ShardSequence shardSequence;

    @Before
    public void before() {
        classPathXmlApplicationContext = new ClassPathXmlApplicationContext("classpath:/ctx-root.xml");

        shardSequence = classPathXmlApplicationContext.getBean("shardtestShardSequence", ShardSequence.class);
    }

    @After
    public void after() {
        classPathXmlApplicationContext.close();
    }

    @Test
    public void generateSequenceTest() {
        for (int i = 0; i < 4050; i++) {
            System.out.printf(shardSequence.nextValue() + " ");
            if (i % 20 == 0) {
                System.out.println();
            }
        }
    }
}
