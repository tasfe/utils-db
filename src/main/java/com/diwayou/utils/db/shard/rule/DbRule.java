package com.diwayou.utils.db.shard.rule;

import com.diwayou.utils.db.shard.route.RouteStrategy;

/**
 * Created by cn40387 on 15/9/9.
 */
public class DbRule {

    private String dbName;

    private int dbCount = 1;

    private RouteStrategy routeStrategy;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public int getDbCount() {
        return dbCount;
    }

    public void setDbCount(int dbCount) {
        this.dbCount = dbCount;
    }

    public RouteStrategy getRouteStrategy() {
        return routeStrategy;
    }

    public void setRouteStrategy(RouteStrategy routeStrategy) {
        this.routeStrategy = routeStrategy;
    }
}
