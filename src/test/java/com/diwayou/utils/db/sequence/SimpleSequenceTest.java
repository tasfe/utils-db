package com.diwayou.utils.db.sequence;

import com.diwayou.utils.db.sequence.impl.SimpleSequence;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by cn40387 on 15/9/10.
 */
public class SimpleSequenceTest {

    private ClassPathXmlApplicationContext classPathXmlApplicationContext;

    private SimpleSequence simpleSequence;

    @Before
    public void before() {
        classPathXmlApplicationContext = new ClassPathXmlApplicationContext("classpath:/ctx-root.xml");

        simpleSequence = classPathXmlApplicationContext.getBean("shardtestSequence", SimpleSequence.class);
    }

    @After
    public void after() {
        classPathXmlApplicationContext.close();
    }

    @Test
    public void generateSequenceTest() {
        for (int i = 0; i < 2050; i++) {
            System.out.printf(simpleSequence.nextValue() + " ");
            if (i % 20 == 0) {
                System.out.println();
            }
        }
    }
}
