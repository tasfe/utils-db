package com.diwayou.utils.db.sequence.util;

import org.junit.Test;

import java.util.Arrays;

/**
 * Created by cn40387 on 15/9/10.
 */
public class RandomSequenceTest {

    @Test
    public void randomIntSequenceTest() {
        int[] seq = RandomSequence.randomIntSequence(2);

        System.out.println(Arrays.toString(seq));
    }
}
