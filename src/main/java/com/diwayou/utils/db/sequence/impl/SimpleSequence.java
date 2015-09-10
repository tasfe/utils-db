package com.diwayou.utils.db.sequence.impl;

import com.diwayou.utils.db.exception.SequenceException;
import com.diwayou.utils.db.sequence.Sequence;
import com.diwayou.utils.db.sequence.SequenceDao;
import com.diwayou.utils.db.sequence.SequenceRange;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Copy from tddl sequence
 *
 * Created by cn40387 on 15/9/10.
 */
public class SimpleSequence implements Sequence {

    private final Lock lock = new ReentrantLock();

    private SequenceDao sequenceDao;

    /**
     * 序列名称
     */
    private String name;

    private volatile SequenceRange currentRange;

    public long nextValue() throws SequenceException {
        if (currentRange == null) {
            lock.lock();
            try {
                if (currentRange == null) {
                    currentRange = sequenceDao.nextRange(name);
                }
            } finally {
                lock.unlock();
            }
        }

        long value = currentRange.getAndIncrement();
        if (value == -1) {
            lock.lock();
            try {
                for (; ; ) {
                    if (currentRange.isOver()) {
                        currentRange = sequenceDao.nextRange(name);
                    }

                    value = currentRange.getAndIncrement();
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
