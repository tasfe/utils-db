package com.diwayou.utils.db.shard.route.impl;

import com.diwayou.utils.db.shard.route.DbRouteStrategy;
import com.diwayou.utils.db.util.RouteUtil;

/**
 * Created by cn40387 on 15/9/9.
 */
public class LongRemainderDbRouteStrategy implements DbRouteStrategy {

    @Override
    public String getRouteSuffix(Object routeKey, int dbCount) {
        if (dbCount <= 0 || dbCount > 9999) {
            throw new IllegalArgumentException("dbCount must between (0, 9999]");
        }

        Long key = null;

        if (routeKey instanceof Long) {
            key = (Long)routeKey;
        } else if (routeKey instanceof Integer) {
            key = Long.valueOf((Integer)routeKey);
        } else {
            throw new IllegalArgumentException("routeKey must be Integer or Long");
        }

        if (key <= 0) {
            throw new IllegalArgumentException("routeKey must > 0");
        }

        return RouteUtil.formatRouteSuffix(key % dbCount);
    }
}
