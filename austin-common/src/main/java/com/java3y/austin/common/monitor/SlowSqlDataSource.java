package com.java3y.austin.common.monitor;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * 慢SQL监控数据源包装类
 * 包装原始数据源,返回带有监控功能的Connection
 *
 * @author austin
 */
@Slf4j
public class SlowSqlDataSource implements DataSource {

    private final DataSource targetDataSource;
    private final long slowSqlThreshold;

    public SlowSqlDataSource(DataSource targetDataSource, long slowSqlThreshold) {
        this.targetDataSource = targetDataSource;
        this.slowSqlThreshold = slowSqlThreshold;
        log.info("【慢SQL监控】初始化完成, 慢SQL阈值: {}ms", slowSqlThreshold);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = targetDataSource.getConnection();
        return (Connection) SlowSqlConnectionProxy.createProxy(connection, slowSqlThreshold);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = targetDataSource.getConnection(username, password);
        return (Connection) SlowSqlConnectionProxy.createProxy(connection, slowSqlThreshold);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return targetDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        targetDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        targetDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return targetDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return targetDataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return targetDataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return targetDataSource.isWrapperFor(iface);
    }
}
