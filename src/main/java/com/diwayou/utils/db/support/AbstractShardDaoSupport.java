package com.diwayou.utils.db.support;

import com.diwayou.utils.db.exception.ShardDaoException;
import com.diwayou.utils.db.shard.ShardContextHolder;
import com.diwayou.utils.db.shard.route.RouteStrategy;
import com.diwayou.utils.db.shard.rule.DbRule;
import com.diwayou.utils.db.shard.rule.TableRule;
import com.diwayou.utils.db.util.RouteUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by cn40387 on 15/9/9.
 */
public abstract class AbstractShardDaoSupport extends SqlSessionDaoSupport implements ShardSqlSession {

    @Resource
    private Map<String/*table name*/, TableRule> tableRuleRoute;

    @Override
    public <T> T selectOne(String statement, Object routeKey, Map<String, Object> parameter) {
        checkArguments(parameter, routeKey);

        try {
            setRouteInfo(parameter, routeKey);

            return getSqlSession().selectOne(statement, parameter);
        } catch (Exception e) {
            throw new ShardDaoException(e);
        } finally {
            clearRouteInfo();
        }
    }

    @Override
    public <E> List<E> selectList(String statement, Object routeKey, Map<String, Object> parameter) {
        checkArguments(parameter, routeKey);

        try {
            setRouteInfo(parameter, routeKey);

            return getSqlSession().selectList(statement, parameter);
        } catch (Exception e) {
            throw new ShardDaoException(e);
        } finally {
            clearRouteInfo();
        }
    }

    @Override
    public int insert(String statement, Object routeKey, Map<String, Object> parameter) {
        checkArguments(parameter, routeKey);

        try {
            setRouteInfo(parameter, routeKey);

            return getSqlSession().insert(statement, parameter);
        } catch (Exception e) {
            throw new ShardDaoException(e);
        } finally {
            clearRouteInfo();
        }
    }

    @Override
    public int update(String statement, Object routeKey, Map<String, Object> parameter) {
        checkArguments(parameter, routeKey);

        try {
            setRouteInfo(parameter, routeKey);

            return getSqlSession().update(statement, parameter);
        } catch (Exception e) {
            throw new ShardDaoException(e);
        } finally {
            clearRouteInfo();
        }
    }

    @Override
    public int delete(String statement, Object routeKey, Map<String, Object> parameter) {
        checkArguments(parameter, routeKey);

        try {
            setRouteInfo(parameter, routeKey);

            return getSqlSession().delete(statement, parameter);
        } catch (Exception e) {
            throw new ShardDaoException(e);
        } finally {
            clearRouteInfo();
        }
    }

    private void checkArguments(Map<String, Object> parameter, Object routeKey) {
        if (parameter == null) {
            throw new IllegalArgumentException("parameter can't be null.");
        }

        if (StringUtils.isEmpty(getTableName())) {
            throw new IllegalArgumentException("Must set tableName.");
        }

        if (routeKey == null) {
            throw new IllegalArgumentException("routeKey can't be null.");
        }
    }

    private void setRouteInfo(Map<String, Object> parameter, Object routeKey) {
        TableRule tableRule = tableRuleRoute.get(getTableName());

        if (tableRule == null) {
            throw new IllegalArgumentException("can't find route for table=" + getTableName());
        }

        // set table route info
        RouteStrategy tableRouteStrategy = tableRule.getRouteStrategy();
        String tableNameSuffix = tableRouteStrategy.getRouteSuffix(routeKey, tableRule.getTableCount());
        String tableName = RouteUtil.buildTableName(tableRule.getTableName(), tableNameSuffix);

        parameter.put(RouteUtil.SQL_TABLE_NAME, tableName);

        // set db route info
        DbRule dbRule = tableRule.getDbRule();
        if (dbRule == null) {
            throw new IllegalArgumentException("must set dbRule for table=" + getTableName());
        }

        RouteStrategy dbRouteStrategy = dbRule.getRouteStrategy();
        String dbNameSuffix = dbRouteStrategy.getRouteSuffix(routeKey, dbRule.getDbCount());
        String dbName = RouteUtil.buildDbName(dbRule.getDbName(), dbNameSuffix);

        ShardContextHolder.setShardDataSourceName(dbName);
    }

    private void clearRouteInfo() {
        ShardContextHolder.clearShardDataSourceName();
    }

    public void setTableRuleRoute(Map<String, TableRule> tableRuleRoute) {
        this.tableRuleRoute = tableRuleRoute;
    }

    public abstract String getTableName();

    @Override
    @Resource
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }
}
