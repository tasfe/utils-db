package com.diwayou.utils.db.shard.rule;

import com.diwayou.utils.db.shard.route.RouteStrategy;

/**
 * Created by cn40387 on 15/9/9.
 */
public class TableRule {

    private String tableName;

    private int tableCount = 1;

    private RouteStrategy routeStrategy;

    private DbRule dbRule;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getTableCount() {
        return tableCount;
    }

    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }

    public RouteStrategy getRouteStrategy() {
        return routeStrategy;
    }

    public void setRouteStrategy(RouteStrategy routeStrategy) {
        this.routeStrategy = routeStrategy;
    }

    public DbRule getDbRule() {
        return dbRule;
    }

    public void setDbRule(DbRule dbRule) {
        this.dbRule = dbRule;
    }
}
