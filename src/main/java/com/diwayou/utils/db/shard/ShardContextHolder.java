package com.diwayou.utils.db.shard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cn40387 on 15/9/8.
 */
public class ShardContextHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardContextHolder.class);

    private static final ThreadLocal<String> SHARD_HOLDER = new ThreadLocal<String>();

    public static void setShardDataSourceName(String dataSourceName) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("setShardDataSourceName : " + dataSourceName);
        }

        SHARD_HOLDER.set(dataSourceName);
    }

    public static String getShardDataSourceName() {
        String shardDataSourceName = SHARD_HOLDER.get();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getShardDataSourceName : " + shardDataSourceName);
        }

        return shardDataSourceName;
    }

    public static void clearShardDataSourceName() {
        SHARD_HOLDER.remove();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("clearShardDataSourceName : " + SHARD_HOLDER.get());
        }
    }

    public static boolean isShardNull() {
        return ShardContextHolder.getShardDataSourceName() == null;
    }
}
