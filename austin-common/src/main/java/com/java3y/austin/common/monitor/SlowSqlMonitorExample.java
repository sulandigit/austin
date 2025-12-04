package com.java3y.austin.common.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 慢SQL监控测试示例
 * 
 * 演示如何触发慢SQL监控
 *
 * @author austin
 */
@Slf4j
@Component
public class SlowSqlMonitorExample {

    @Autowired
    private DataSource dataSource;

    /**
     * 示例1: 使用Statement执行SQL
     * 如果执行时间超过阈值,会触发慢SQL日志
     */
    public void exampleWithStatement() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 执行一个简单的查询
            ResultSet rs = stmt.executeQuery("SELECT * FROM message_template LIMIT 10");
            
            while (rs.next()) {
                log.debug("查询结果: {}", rs.getLong("id"));
            }
            rs.close();
            
        } catch (Exception e) {
            log.error("执行SQL异常", e);
        }
    }

    /**
     * 示例2: 使用PreparedStatement执行SQL
     * 如果执行时间超过阈值,会触发慢SQL日志
     */
    public void exampleWithPreparedStatement() {
        String sql = "SELECT * FROM message_template WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, 1L);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                log.debug("查询结果: {}", rs.getString("name"));
            }
            rs.close();
            
        } catch (Exception e) {
            log.error("执行SQL异常", e);
        }
    }

    /**
     * 示例3: 模拟慢SQL
     * 通过sleep模拟一个执行时间较长的SQL
     */
    public void simulateSlowSql() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 使用MySQL的SLEEP函数模拟慢SQL(睡眠2秒)
            stmt.execute("SELECT SLEEP(2)");
            
            log.info("模拟慢SQL执行完成");
            
        } catch (Exception e) {
            log.error("执行SQL异常", e);
        }
    }

    /**
     * 示例4: 执行批量操作
     * 如果批量操作耗时超过阈值,会触发慢SQL日志
     */
    public void exampleWithBatch() {
        String sql = "INSERT INTO message_template (name, audit_status) VALUES (?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < 100; i++) {
                pstmt.setString(1, "测试消息" + i);
                pstmt.setInt(2, 10);
                pstmt.addBatch();
            }
            
            int[] results = pstmt.executeBatch();
            log.info("批量插入完成, 影响行数: {}", results.length);
            
        } catch (Exception e) {
            log.error("执行批量操作异常", e);
        }
    }
}
