package com.diwayou.utils.db.sequence.impl;

import com.diwayou.utils.db.exception.SequenceException;
import com.diwayou.utils.db.sequence.SequenceDao;
import com.diwayou.utils.db.sequence.SequenceRange;
import com.diwayou.utils.db.sequence.util.RandomSequence;
import com.diwayou.utils.db.shard.ShardContextHolder;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by cn40387 on 15/9/10.
 */
public class ShardSequenceDao implements SequenceDao {

    private static final Logger logger = LoggerFactory.getLogger(ShardSequenceDao.class);

    private static final int DEFAULT_INNER_STEP = 1000;

    private static final int DEFAULT_RETRY_TIMES = 2;

    private static final String DEFAULT_TABLE_NAME = "sequence";

    private static final String DEFAULT_NAME_COLUMN_NAME = "name";
    private static final String DEFAULT_VALUE_COLUMN_NAME = "value";
    private static final String DEFAULT_GMT_MODIFIED_COLUMN_NAME = "gmt_modified";

    private static final int DEFAULT_DSCOUNT = 2;                                              // 默认
    private static final Boolean DEFAULT_ADJUST = false;

    protected static final long DELTA = 100000000L;


    /**
     * 每个数据库的名字，在shardDataSource中按照这个key进行路由
     */
    protected List<String> dbRouteKeys;

    /**
     * 支持sharding的DataSource[ShardRoutingDataSource]
     */
    private DataSource dataSource;

    /**
     * 自适应开关
     */
    protected boolean adjust = DEFAULT_ADJUST;

    /**
     * 重试次数
     */
    protected int retryTimes = DEFAULT_RETRY_TIMES;

    /**
     * 数据源个数
     */
    protected int dscount = DEFAULT_DSCOUNT;

    /**
     * 内步长
     */
    protected int innerStep = DEFAULT_INNER_STEP;

    /**
     * 外步长
     */
    protected int outStep = DEFAULT_INNER_STEP;

    /**
     * 序列所在的表名
     */
    protected String tableName = DEFAULT_TABLE_NAME;

    /**
     * 存储序列名称的列名
     */
    protected String nameColumnName = DEFAULT_NAME_COLUMN_NAME;

    /**
     * 存储序列值的列名
     */
    protected String valueColumnName = DEFAULT_VALUE_COLUMN_NAME;

    /**
     * 存储序列最后更新时间的列名
     */
    protected String gmtModifiedColumnName = DEFAULT_GMT_MODIFIED_COLUMN_NAME;

    /**
     * 初试化
     *
     * @throws SequenceException
     */
    @PostConstruct
    public void init() throws SequenceException {
        if (CollectionUtils.isEmpty(dbRouteKeys)) {
            throw new SequenceException("Must set dbRouteKeys.");
        }

        if (dscount != dbRouteKeys.size()) {
            dscount = dbRouteKeys.size();
        }

        outStep = innerStep * dscount;// 计算外步长

        outputInitResult();
    }

    /**
     * 初始化完打印配置信息
     */
    private void outputInitResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("GroupSequenceDao初始化完成：\r\n ");
        sb.append("innerStep:").append(this.innerStep).append("\r\n");
        sb.append("dataSource:").append(dscount).append("个:");
        for (String str : dbRouteKeys) {
            sb.append("[").append(str).append("]、");
        }
        sb.append("\r\n");
        sb.append("adjust：").append(adjust).append("\r\n");
        sb.append("retryTimes:").append(retryTimes).append("\r\n");
        sb.append("tableName:").append(tableName).append("\r\n");
        sb.append("nameColumnName:").append(nameColumnName).append("\r\n");
        sb.append("valueColumnName:").append(valueColumnName).append("\r\n");
        sb.append("gmtModifiedColumnName:").append(gmtModifiedColumnName).append("\r\n");
        logger.info(sb.toString());
    }

    /**
     * @param index group内的序号，从0开始
     * @param value 当前取的值
     * @return
     */
    private boolean check(int index, long value) {
        return (value % outStep) == (index * innerStep);
    }

    /**
     * <pre>
     * 检查并初试某个sequence。
     *
     * 1、如果sequence不存在，插入值，并初始化值。
     * 2、如果已经存在，但有重叠，重新生成。
     * 3、如果已经存在，且无重叠。
     *
     * @throws SequenceException </pre>
     */
    public void adjust(String name) throws SequenceException, SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        for (int i = 0; i < dbRouteKeys.size(); i++) {
            ShardContextHolder.setShardDataSourceName(dbRouteKeys.get(i));

            try {
                conn = dataSource.getConnection();
                stmt = conn.prepareStatement(getSelectSql());
                stmt.setString(1, name);
                rs = stmt.executeQuery();
                int item = 0;
                while (rs.next()) {
                    item++;
                    long val = rs.getLong(this.getValueColumnName());
                    if (!check(i, val)) // 检验初值
                    {
                        if (this.isAdjust()) {
                            this.adjustUpdate(i, val, name);
                        } else {
                            logger.error("数据库中配置的初值出错！请调整你的数据库，或者启动adjust开关");
                            throw new SequenceException("数据库中配置的初值出错！请调整你的数据库，或者启动adjust开关");
                        }
                    }
                }
                if (item == 0)// 不存在,插入这条记录
                {
                    if (this.isAdjust()) {
                        this.adjustInsert(i, name);
                    } else {
                        logger.error("数据库中未配置该sequence！请往数据库中插入sequence记录，或者启动adjust开关");
                        throw new SequenceException("数据库中未配置该sequence！请往数据库中插入sequence记录，或者启动adjust开关");
                    }
                }
            } catch (SQLException e) {// 吞掉SQL异常，我们允许不可用的库存在
                logger.error("初值校验和自适应过程中出错.", e);
                throw e;
            } finally {
                closeDbResource(rs, stmt, conn);
            }
        }
    }

    /**
     * 更新
     *
     * @param index
     * @param value
     * @param name
     * @throws SequenceException
     * @throws SQLException
     */
    private void adjustUpdate(int index, long value, String name) throws SequenceException, SQLException {
        long newValue = (value - value % outStep) + outStep + index * innerStep;// 设置成新的调整值
        ShardContextHolder.setShardDataSourceName(dbRouteKeys.get(index));

        Connection conn = null;
        PreparedStatement stmt = null;
        // ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(getUpdateSql());
            stmt.setLong(1, newValue);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setString(3, name);
            stmt.setLong(4, value);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SequenceException("faild to auto adjust init value at  " + name + " update affectedRow =0");
            }
            logger.info(dbRouteKeys.get(index) + "更新初值成功!" + "sequence Name：" + name + "更新过程：" + value + "-->"
                    + newValue);
        } catch (SQLException e) { // 吃掉SQL异常，抛Sequence异常
            logger.error("由于SQLException,更新初值自适应失败！dbGroupIndex:" + dbRouteKeys.get(index) + "，sequence Name：" + name
                    + "更新过程：" + value + "-->" + newValue, e);
            throw new SequenceException("由于SQLException,更新初值自适应失败！dbGroupIndex:" + dbRouteKeys.get(index)
                    + "，sequence Name：" + name + "更新过程：" + value + "-->" + newValue, e);
        } finally {
            closeDbResource(null, stmt, conn);
        }
    }

    /**
     * 插入新值
     *
     * @param index
     * @param name
     * @return
     * @throws SequenceException
     * @throws SQLException
     */
    private void adjustInsert(int index, String name) throws SequenceException, SQLException {
        ShardContextHolder.setShardDataSourceName(dbRouteKeys.get(index));

        long newValue = index * innerStep;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(getInsertSql());
            stmt.setString(1, name);
            stmt.setLong(2, newValue);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SequenceException("faild to auto adjust init value at  " + name + " update affectedRow =0");
            }
            logger.info(dbRouteKeys.get(index) + "   name:" + name + "插入初值:" + name + "value:" + newValue);

        } catch (SQLException e) {
            logger.error("由于SQLException,插入初值自适应失败！dbGroupIndex:" + dbRouteKeys.get(index) + "，sequence Name：" + name
                    + "   value:" + newValue, e);
            throw new SequenceException("由于SQLException,插入初值自适应失败！dbGroupIndex:" + dbRouteKeys.get(index)
                    + "，sequence Name：" + name + "   value:" + newValue, e);
        } finally {
            closeDbResource(rs, stmt, conn);
        }
    }

    private ConcurrentHashMap<Integer/* ds index */, AtomicInteger/* 掠过次数 */> excludedKeyCount = new ConcurrentHashMap<Integer, AtomicInteger>(dscount);
    // 最大略过次数后恢复
    private int maxSkipCount = 10;
    // 使用慢速数据库保护
    private boolean useSlowProtect = false;
    // 保护的时间
    private int protectMilliseconds = 50;

    private ExecutorService exec = Executors.newFixedThreadPool(1);

    protected Lock configLock = new ReentrantLock();

    /**
     * 检查groupKey对象是否已经关闭
     *
     * @param groupKey
     * @return
     */
    protected boolean isOffState(String groupKey) {
        return groupKey.toUpperCase().endsWith("-OFF");
    }

    /**
     * 检查是否被exclude,如果有尝试恢复
     *
     * @param index
     * @return
     */
    protected boolean recoverFromExcludes(int index) {
        boolean result = true;
        if (excludedKeyCount.get(index) != null) {
            if (excludedKeyCount.get(index).incrementAndGet() > maxSkipCount) {
                excludedKeyCount.remove(index);
                logger.error(maxSkipCount + "次数已过，index为" + index + "的数据源后续重新尝试取序列");
            } else {
                result = false;
            }
        }
        return result;
    }

    protected long queryOldValue(DataSource dataSource, String keyName) throws SQLException, SequenceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(getSelectSql());
            stmt.setString(1, keyName);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SequenceException("找不到对应的sequence记录，请检查sequence : " + keyName);
            }
        } finally {
            closeDbResource(rs, stmt, conn);
        }
    }

    /**
     * CAS更新sequence值
     *
     * @param dataSource
     * @param keyName
     * @param oldValue
     * @param newValue
     * @return
     * @throws SQLException
     */
    protected int updateNewValue(DataSource dataSource, String keyName, long oldValue, long newValue)
            throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            stmt = conn.prepareStatement(getUpdateSql());
            stmt.setLong(1, newValue);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setString(3, keyName);
            stmt.setLong(4, oldValue);
            return stmt.executeUpdate();
        } finally {
            closeDbResource(rs, stmt, conn);
        }
    }

    /**
     * 从指定的数据库中获取sequence值
     *
     * @param dataSource
     * @param keyName
     * @return
     * @throws SQLException
     * @throws SequenceException
     */
    protected long getOldValue(final DataSource dataSource, final String keyName) throws SQLException,
            SequenceException {
        long result = 0;

        // 如果未使用超时保护或者已经只剩下了1个数据源，无论怎么样去拿
        if (!useSlowProtect || excludedKeyCount.size() >= (dscount - 1)) {
            result = queryOldValue(dataSource, keyName);
        } else {
            FutureTask<Long> future = new FutureTask<Long>(new Callable<Long>() {

                @Override
                public Long call() throws Exception {
                    return queryOldValue(dataSource, keyName);
                }
            });
            try {
                exec.submit(future);
                result = future.get(protectMilliseconds, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new SQLException("[SEQUENCE SLOW-PROTECTED MODE]:InterruptedException", e);
            } catch (ExecutionException e) {
                throw new SQLException("[SEQUENCE SLOW-PROTECTED MODE]:ExecutionException", e);
            } catch (TimeoutException e) {
                throw new SQLException("[SEQUENCE SLOW-PROTECTED MODE]:TimeoutException,当前设置超时时间为"
                        + protectMilliseconds, e);
            }
        }
        return result;
    }

    /**
     * 生成oldValue生成newValue
     *
     * @param index
     * @param oldValue
     * @param keyName
     * @return
     * @throws SequenceException
     */
    protected long generateNewValue(int index, long oldValue, String keyName) throws SequenceException {
        long newValue = oldValue + outStep;
        if (!check(index, newValue)) // 新算出来的值有问题
        {
            if (this.isAdjust()) {
                newValue = adjustNewValue(index, newValue);
            } else {
                throwErrorRangeException(index, keyName);
            }
        }
        return newValue;
    }

    protected long adjustNewValue(int index, long newValue) {
        return (newValue - newValue % outStep) + outStep + index * innerStep;// 设置成新的调整值
    }

    protected void throwErrorRangeException(int index, String keyName) throws SequenceException {
        String errorMsg = dbRouteKeys.get(index) + ":" + keyName + "的值得错误，覆盖到其他范围段了！请修改数据库，或者开启adjust开关！";
        throw new SequenceException(errorMsg);
    }

    /**
     * 检查该sequence值是否在正常范围内
     *
     * @return
     */
    protected boolean isOldValueFixed(long oldValue) {
        boolean result = true;
        StringBuilder message = new StringBuilder();
        if (oldValue < 0) {
            message.append("Sequence value cannot be less than zero.");
            result = false;
        } else if (oldValue > Long.MAX_VALUE - DELTA) {
            message.append("Sequence value overflow.");
            result = false;
        }
        if (!result) {
            message.append(" Sequence value  = ").append(oldValue);
            message.append(", please check table ").append(getTableName());
            logger.info(message.toString());
        }
        return result;
    }

    /**
     * 将该数据源排除到sequence可选数据源以外
     *
     * @param index
     */
    protected void excludeDataSource(int index) {
        // 如果数据源只剩下了最后一个，就不要排除了
        if (excludedKeyCount.size() < (dscount - 1)) {
            excludedKeyCount.put(index, new AtomicInteger(0));
            logger.error("暂时踢除index为" + index + "的数据源，" + maxSkipCount + "次后重新尝试");
        }
    }

    public SequenceRange nextRange(final String name) throws SequenceException {
        if (name == null) {
            logger.error("序列名为空！");
            throw new IllegalArgumentException("序列名称不能为空");
        }

        configLock.lock();
        try {
            int[] randomIntSequence = RandomSequence.randomIntSequence(dscount);
            for (int i = 0; i < retryTimes; i++) {
                for (int j = 0; j < dscount; j++) {
                    int index = randomIntSequence[j];
                    if (!recoverFromExcludes(index)) {
                        continue;
                    }

                    ShardContextHolder.setShardDataSourceName(dbRouteKeys.get(index));
                    long oldValue;
                    // 查询，只在这里做数据库挂掉保护和慢速数据库保护
                    try {
                        oldValue = getOldValue(dataSource, name);
                        if (!isOldValueFixed(oldValue)) {
                            continue;
                        }
                    } catch (SQLException e) {
                        logger.error("取范围过程中--查询出错！" + dbRouteKeys.get(index) + ":" + name, e);
                        excludeDataSource(index);
                        continue;
                    }

                    long newValue = generateNewValue(index, oldValue, name);
                    try {
                        if (0 == updateNewValue(dataSource, name, oldValue, newValue)) {
                            continue;
                        }
                    } catch (SQLException e) {
                        logger.error("取范围过程中--更新出错！" + dbRouteKeys.get(index) + ":" + name, e);
                        continue;
                    }

                    return new SequenceRange(newValue + 1, newValue + innerStep);

                }
                // 当还有最后一次重试机会时,清空excludedMap,让其有最后一次机会
                if (i == (retryTimes - 2)) {
                    excludedKeyCount.clear();
                }
            }
            logger.error("所有数据源都不可用！且重试" + this.retryTimes + "次后，仍然失败!");
            throw new SequenceException("All dataSource faild to get value!");
        } finally {
            configLock.unlock();
        }
    }

    public void setDscount(int dscount) {
        this.dscount = dscount;
    }

    protected String getInsertSql() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("insert into ").append(getTableName()).append("(");
        buffer.append(getNameColumnName()).append(",");
        buffer.append(getValueColumnName()).append(",");
        buffer.append(getGmtModifiedColumnName()).append(") values(?,?,?);");
        return buffer.toString();
    }

    protected String getSelectSql() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("select ").append(getValueColumnName());
        buffer.append(" from ").append(getTableName());
        buffer.append(" where ").append(getNameColumnName()).append(" = ?");
        return buffer.toString();
    }

    protected String getUpdateSql() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("update ").append(getTableName());
        buffer.append(" set ").append(getValueColumnName()).append(" = ?, ");
        buffer.append(getGmtModifiedColumnName()).append(" = ? where ");
        buffer.append(getNameColumnName()).append(" = ? and ");
        buffer.append(getValueColumnName()).append(" = ?");
        return buffer.toString();
    }

    protected static void closeDbResource(ResultSet rs, Statement stmt, Connection conn) {
        closeResultSet(rs);
        closeStatement(stmt);
        closeConnection(conn);
    }

    protected static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.debug("Could not close JDBC ResultSet", e);
            } catch (Throwable e) {
                logger.debug("Unexpected exception on closing JDBC ResultSet", e);
            }
        }
    }

    protected static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                logger.debug("Could not close JDBC Statement", e);
            } catch (Throwable e) {
                logger.debug("Unexpected exception on closing JDBC Statement", e);
            }
        }
    }

    protected static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.debug("Could not close JDBC Connection", e);
            } catch (Throwable e) {
                logger.debug("Unexpected exception on closing JDBC Connection", e);
            }
        }
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getInnerStep() {
        return innerStep;
    }

    public void setInnerStep(int innerStep) {
        this.innerStep = innerStep;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getNameColumnName() {
        return nameColumnName;
    }

    public void setNameColumnName(String nameColumnName) {
        this.nameColumnName = nameColumnName;
    }

    public String getValueColumnName() {
        return valueColumnName;
    }

    public void setValueColumnName(String valueColumnName) {
        this.valueColumnName = valueColumnName;
    }

    public String getGmtModifiedColumnName() {
        return gmtModifiedColumnName;
    }

    public void setGmtModifiedColumnName(String gmtModifiedColumnName) {
        this.gmtModifiedColumnName = gmtModifiedColumnName;
    }

    public List<String> getDbRouteKeys() {
        return dbRouteKeys;
    }

    public void setDbRouteKeys(List<String> dbRouteKeys) {
        this.dbRouteKeys = dbRouteKeys;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isAdjust() {
        return adjust;
    }

    public void setAdjust(boolean adjust) {
        this.adjust = adjust;
    }
}
