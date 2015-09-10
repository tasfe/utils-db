package com.diwayou.utils.db.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by cn40387 on 15/9/8.
 */
public class TransactionContextHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionContextHolder.class);

    private static final ThreadLocal<Boolean> TRANSACTION_HOLER = new ThreadLocal<Boolean>();

    public static void setInTransaction(Boolean inTransaction) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("setInTransaction : " + inTransaction);
        }

        TRANSACTION_HOLER.set(inTransaction);
    }

    public static Boolean getInTransaction() {
        Boolean inTransaction = TRANSACTION_HOLER.get();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getInTransaction : " + inTransaction);
        }

        return inTransaction;
    }

    public static void clearInTransaction() {
        TRANSACTION_HOLER.remove();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("clearInTransaction : " + TRANSACTION_HOLER.get());
        }
    }

    public static boolean isInTransaction() {
        return TransactionContextHolder.getInTransaction() != null;
    }
}
