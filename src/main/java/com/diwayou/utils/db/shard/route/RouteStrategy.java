package com.diwayou.utils.db.shard.route;

/**
 * Created by cn40387 on 15/9/9.
 */
public interface RouteStrategy {

    String getRouteSuffix(Object routeKey, int dbCount);
}
