package com.diwayou.utils.db.transaction;

import com.diwayou.utils.db.exception.ShardDaoException;
import com.diwayou.utils.db.shard.ShardContextHolder;
import com.diwayou.utils.db.shard.route.DbRouteStrategy;
import com.diwayou.utils.db.shard.rule.DbRule;
import com.diwayou.utils.db.util.RouteUtil;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Created by diwayou on 2015/9/10.
 */
public class ShardTransactionTemplate extends TransactionTemplate {

    @Override
    public <T> T execute(TransactionCallback<T> action) throws TransactionException {
        throw new UnsupportedOperationException();
    }

    public <T> T execute(Object routeKey, ShardTransactionCallback<T> action) throws TransactionException {
        DbRule dbRule = action.getDbRule();
        if (dbRule == null) {
            throw new ShardDaoException("Must set dbRule.");
        }

        DbRouteStrategy dbRouteStrategy = dbRule.getDbRouteStrategy();
        String dbNameSuffix = dbRouteStrategy.getRouteSuffix(routeKey, dbRule.getDbCount());
        String dbName = RouteUtil.buildDbName(dbRule.getDbName(), dbNameSuffix);

        try {
            TransactionContextHolder.setInTransaction(Boolean.TRUE);
            ShardContextHolder.setShardDataSourceName(dbName);

            return super.execute(action);
        } finally {
            TransactionContextHolder.clearInTransaction();
            ShardContextHolder.clearShardDataSourceName();
        }
    }
}
