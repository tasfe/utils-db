package com.diwayou.utils.db.util;

/**
 * Created by cn40387 on 15/9/9.
 */
public class RouteUtil {

    public static final String SQL_TABLE_NAME = "tname";

    public static final String SEPARATOR = "_";

    public static String buildDbName(String dbName, String suffix) {
        return dbName + SEPARATOR + suffix;
    }

    public static String buildTableName(String tableName, String suffix) {
        return tableName + SEPARATOR + suffix;
    }

    public static String formatRouteSuffix(Long key) {
        return String.format("%04d", key);
    }
}
