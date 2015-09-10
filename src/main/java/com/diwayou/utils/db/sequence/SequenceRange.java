package com.diwayou.utils.db.sequence;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cn40387 on 15/9/10.
 */
public class SequenceRange {

    private final long min;
    private final long max;

    private final AtomicLong value;

    private volatile boolean over = false;

    public SequenceRange(long min, long max) {
        this.min = min;
        this.max = max;
        this.value = new AtomicLong(min);
    }

    public long getAndIncrement() {
        long currentValue = value.getAndIncrement();
        if (currentValue > max) {
            over = true;
            return -1;
        }

        return currentValue;
    }

    public long getMin() {
        return min;
    }

    public long getMax() {
        return max;
    }

    public boolean isOver() {
        return over;
    }
}
