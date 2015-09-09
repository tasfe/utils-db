package com.diwayou.utils.db.shard.rule;

import com.diwayou.utils.db.shard.route.DbRouteStrategy;

/**
 * Created by cn40387 on 15/9/9.
 */
public class DbRule {

    private String dbName;

    private int dbCount = 1;

    private DbRouteStrategy dbRouteStrategy;

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

    public DbRouteStrategy getDbRouteStrategy() {
        return dbRouteStrategy;
    }

    public void setDbRouteStrategy(DbRouteStrategy dbRouteStrategy) {
        this.dbRouteStrategy = dbRouteStrategy;
    }
}
