package com.diwayou.utils.db.shard.route.impl;

import com.diwayou.utils.db.shard.route.TableRouteStrategy;
import com.diwayou.utils.db.util.RouteUtil;

/**
 * Created by cn40387 on 15/9/9.
 */
public class LongRemainderTableRouteStrategy implements TableRouteStrategy {

    @Override
    public String getRouteSuffix(Object routeKey, int dbCount, int tableCount) {
        if (tableCount <= 0 || tableCount > 9999) {
            throw new IllegalArgumentException("tableCount must between (0, 9999]");
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

        return RouteUtil.formatRouteSuffix(key / dbCount % tableCount);
    }
}
