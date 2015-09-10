package com.diwayou.utils.db.sequence.impl;

import com.diwayou.utils.db.exception.SequenceException;
import com.diwayou.utils.db.sequence.Sequence;
import com.diwayou.utils.db.sequence.SequenceDao;
import com.diwayou.utils.db.sequence.SequenceRange;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cn40387 on 15/9/10.
 */
public class ShardSequence implements Sequence {

    private final Lock lock = new ReentrantLock();
    private SequenceDao sequenceDao;

    private String name;
    private volatile SequenceRange currentRange;

    /**
     * 初始化一下，如果name不存在，则给其初始值<br>
     *
     * @throws SequenceException
     * @throws java.sql.SQLException
     */
    @PostConstruct
    public void init() throws SequenceException, SQLException {
        if (!(sequenceDao instanceof ShardSequenceDao)) {
            throw new SequenceException("please use  GroupSequenceDao!");
        }
        ShardSequenceDao shardSequenceDao = (ShardSequenceDao) sequenceDao;
        synchronized (this) // 为了保证安全，
        {
            shardSequenceDao.adjust(name);
        }
    }

    public long nextValue() throws SequenceException {
        boolean isTest = false;
        if (getSequenceRange(isTest) == null) {
            lock.lock();
            try {
                if (getSequenceRange(isTest) == null) {
                    setSequenceRange(sequenceDao.nextRange(name), isTest);
                }
            } finally {
                lock.unlock();
            }
        }

        long value = getSequenceRange(isTest).getAndIncrement();
        if (value == -1) {
            lock.lock();
            try {
                for (; ; ) {
                    if (getSequenceRange(isTest).isOver()) {
                        setSequenceRange(sequenceDao.nextRange(name), isTest);
                    }

                    value = getSequenceRange(isTest).getAndIncrement();
                    if (value == -1) {
                        continue;
                    }

                    break;
                }
            } finally {
                lock.unlock();
            }
        }

        if (value < 0) {
            throw new SequenceException("Sequence value overflow, value = " + value);
        }

        return value;
    }

    private SequenceRange getSequenceRange(boolean isTest) {
        return this.currentRange;
    }

    private void setSequenceRange(SequenceRange range, boolean isTest) {
        this.currentRange = range;
    }

    public SequenceDao getSequenceDao() {
        return sequenceDao;
    }

    public void setSequenceDao(SequenceDao sequenceDao) {
        this.sequenceDao = sequenceDao;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
