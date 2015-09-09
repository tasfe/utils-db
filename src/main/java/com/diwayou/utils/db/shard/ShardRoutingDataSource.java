package com.diwayou.utils.db.shard;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Created by cn40387 on 15/9/8.
 */
public class ShardRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return ShardContextHolder.getShardDataSourceName();
    }
}
