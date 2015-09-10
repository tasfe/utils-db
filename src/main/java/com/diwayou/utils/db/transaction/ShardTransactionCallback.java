package com.diwayou.utils.db.transaction;

import com.diwayou.utils.db.shard.rule.DbRule;
import org.springframework.transaction.support.TransactionCallback;

/**
 * Created by diwayou on 2015/9/10.
 */
public interface ShardTransactionCallback<T> extends TransactionCallback<T> {

    DbRule getDbRule();
}
