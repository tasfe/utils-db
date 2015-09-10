package com.diwayou.utils.db.sequence;

import com.diwayou.utils.db.exception.SequenceException;

/**
 * Created by cn40387 on 15/9/10.
 */
public interface Sequence {

    long nextValue() throws SequenceException;
}
