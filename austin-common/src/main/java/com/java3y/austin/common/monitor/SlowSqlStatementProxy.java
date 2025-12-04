package com.java3y.austin.common.monitor;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Statement代理类
 * 用于监控SQL执行时间
 *
 * @author austin
 */
@Slf4j
public class SlowSqlStatementProxy implements InvocationHandler {

    private final Statement target;
    private final long slowSqlThreshold;
    private final String sql;

    public SlowSqlStatementProxy(Statement target, long slowSqlThreshold, String sql) {
        this.target = target;
        this.slowSqlThreshold = slowSqlThreshold;
        this.sql = sql;
    }

    public static Object createProxy(Statement target, long slowSqlThreshold, String sql) {
        Class<?>[] interfaces;
        if (target instanceof PreparedStatement) {
            interfaces = new Class[]{PreparedStatement.class};
        } else {
            interfaces = new Class[]{Statement.class};
        }
        
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                interfaces,
                new SlowSqlStatementProxy(target, slowSqlThreshold, sql)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        
        // 监控execute相关方法
        if (isExecuteMethod(methodName)) {
            long startTime = System.currentTimeMillis();
            String executedSql = sql;
            
            // 对于Statement的execute方法,SQL在参数中
            if (executedSql.isEmpty() && args != null && args.length > 0 && args[0] instanceof String) {
                executedSql = (String) args[0];
            }
            
            try {
                return method.invoke(target, args);
            } finally {
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                
                // 如果执行时间超过阈值,记录慢SQL日志
                if (executionTime > slowSqlThreshold) {
                    logSlowSql(executedSql, executionTime);
                }
            }
        }
        
        return method.invoke(target, args);
    }

    /**
     * 判断是否是execute相关方法
     */
    private boolean isExecuteMethod(String methodName) {
        return "execute".equals(methodName) || 
               "executeQuery".equals(methodName) || 
               "executeUpdate".equals(methodName) ||
               "executeBatch".equals(methodName) ||
               "executeLargeUpdate".equals(methodName);
    }

    /**
     * 记录慢SQL日志
     */
    private void logSlowSql(String sql, long executionTime) {
        log.warn("【慢SQL监控】检测到慢SQL, 执行时间: {}ms, SQL: {}", executionTime, sql);
        
        // 可以在这里扩展功能: 
        // 1. 发送告警通知(邮件/短信/企业微信等)
        // 2. 记录到数据库,便于后续分析
        // 3. 推送到APM监控系统(如Prometheus/Grafana)
        // 4. 统计慢SQL的次数、平均耗时等指标
    }
}
