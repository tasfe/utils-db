package com.diwayou.utils.db.exception;

/**
 * Created by cn40387 on 15/9/9.
 */
public class ShardDaoException extends RuntimeException {

    public ShardDaoException() {
    }

    public ShardDaoException(String message) {
        super(message);
    }

    public ShardDaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShardDaoException(Throwable cause) {
        super(cause);
    }
}
