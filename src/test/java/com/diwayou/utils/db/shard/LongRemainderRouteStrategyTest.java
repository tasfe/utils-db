package com.diwayou.utils.db.shard;

import com.diwayou.utils.db.shard.route.impl.LongRemainderRouteStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by cn40387 on 15/9/9.
 */
public class LongRemainderRouteStrategyTest {

    private LongRemainderRouteStrategy longRemainderRouteStrategy;

    @Before
    public void before() {
        longRemainderRouteStrategy = new LongRemainderRouteStrategy();
    }

    @Test
    public void getRouteSuffixTest() {
        int shardCount = 16;

        Assert.assertEquals("0003", longRemainderRouteStrategy.getRouteSuffix(3, shardCount));

        Assert.assertEquals("0007", longRemainderRouteStrategy.getRouteSuffix(1234567, shardCount));
    }
}
