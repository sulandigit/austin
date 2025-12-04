package com.java3y.austin.common.monitor;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.*;

/**
 * Connection代理类
 * 用于拦截Statement和PreparedStatement的创建
 *
 * @author austin
 */
@Slf4j
public class SlowSqlConnectionProxy implements InvocationHandler {

    private final Connection target;
    private final long slowSqlThreshold;

    public SlowSqlConnectionProxy(Connection target, long slowSqlThreshold) {
        this.target = target;
        this.slowSqlThreshold = slowSqlThreshold;
    }

    public static Object createProxy(Connection target, long slowSqlThreshold) {
        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class[]{Connection.class},
                new SlowSqlConnectionProxy(target, slowSqlThreshold)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Object result = method.invoke(target, args);
        
        // 拦截创建Statement的方法
        if (result instanceof Statement) {
            if ("prepareStatement".equals(methodName) || "prepareCall".equals(methodName)) {
                // PreparedStatement需要记录SQL
                String sql = args != null && args.length > 0 ? (String) args[0] : "";
                return SlowSqlStatementProxy.createProxy((Statement) result, slowSqlThreshold, sql);
            } else if ("createStatement".equals(methodName)) {
                // Statement
                return SlowSqlStatementProxy.createProxy((Statement) result, slowSqlThreshold, "");
            }
        }
        
        return result;
    }
}
