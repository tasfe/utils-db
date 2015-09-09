package com.diwayou.utils.db.shard;

import com.diwayou.utils.db.shard.route.impl.LongRemainderDbRouteStrategy;
import com.diwayou.utils.db.shard.route.impl.LongRemainderTableRouteStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by cn40387 on 15/9/9.
 */
public class LongRemainderRouteStrategyTest {

    private LongRemainderDbRouteStrategy longRemainderDbRouteStrategy;

    private LongRemainderTableRouteStrategy longRemainderTableRouteStrategy;

    @Before
    public void before() {
        longRemainderDbRouteStrategy = new LongRemainderDbRouteStrategy();

        longRemainderTableRouteStrategy = new LongRemainderTableRouteStrategy();
    }

    @Test
    public void getDbRouteSuffixTest() {
        int dbCount = 16;

        Assert.assertEquals("0003", longRemainderDbRouteStrategy.getRouteSuffix(3, dbCount));

        Assert.assertEquals("0007", longRemainderDbRouteStrategy.getRouteSuffix(1234567, dbCount));
    }

    @Test
    public void getTableRouteSuffixTest() {
        int dbCount = 16;
        int tableCount = 64;

        Assert.assertEquals("0000", longRemainderTableRouteStrategy.getRouteSuffix(3, dbCount, tableCount));

        Assert.assertEquals("0040", longRemainderTableRouteStrategy.getRouteSuffix(1234567, dbCount, tableCount));
    }

    @Test
    public void distributionTest() {
        int dbCount = 4;
        int tableCount = 256;

        for (int i = 1; i < 100; i++) {
            String dbSuffix = longRemainderDbRouteStrategy.getRouteSuffix(i, dbCount);
            String tableSuffix = longRemainderTableRouteStrategy.getRouteSuffix(i, dbCount, tableCount);

            System.out.println(dbSuffix + " " + tableSuffix);
        }
    }
}
