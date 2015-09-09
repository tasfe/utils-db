package com.diwayou.utils.db.support;

import java.util.List;
import java.util.Map;

/**
 * 只能使用Map作为parameter参数，因为需要通过Map来传入真正的表名
 *
 * Created by cn40387 on 15/9/9.
 */
public interface ShardSqlSession {

    <T> T selectOne(String statement, Object routeKey, Map<String, Object> parameter);

    <E> List<E> selectList(String statement, Object routeKey, Map<String, Object> parameter);

    int insert(String statement, Object routeKey, Map<String, Object> parameter);

    int update(String statement, Object routeKey, Map<String, Object> parameter);

    int delete(String statement, Object routeKey, Map<String, Object> parameter);
}
