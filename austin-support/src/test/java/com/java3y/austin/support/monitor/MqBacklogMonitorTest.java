package com.java3y.austin.support.monitor;

import com.java3y.austin.common.domain.MqBacklogAlertInfo;
import com.java3y.austin.common.enums.AlertLevel;
import com.java3y.austin.support.config.BacklogMonitorConfig;
import com.java3y.austin.support.dto.BacklogMonitorTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MQ积压监控器单元测试
 *
 * @author austin
 */
class MqBacklogMonitorTest {

    @Mock
    private BacklogMonitorConfig monitorConfig;

    @Mock
    private MqBacklogAlertService alertService;

    @Mock
    private MqBacklogAdapter mockAdapter;

    @InjectMocks
    private MqBacklogMonitor mqBacklogMonitor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCollectAndMonitor_WhenDisabled_ShouldNotExecute() {
        // Given
        when(monitorConfig.getEnabled()).thenReturn(false);

        // When
        mqBacklogMonitor.collectAndMonitor();

        // Then
        verify(monitorConfig, never()).getTargetList();
        verify(alertService, never()).sendAlert(any());
    }

    @Test
    void testCollectAndMonitor_WhenEnabled_ShouldMonitorTargets() {
        // Given
        when(monitorConfig.getEnabled()).thenReturn(true);
        
        BacklogMonitorTarget target = BacklogMonitorTarget.builder()
                .mqType("redis")
                .topic("austinBusiness")
                .consumerGroup("default")
                .enabled(true)
                .warnThreshold(10000L)
                .criticalThreshold(50000L)
                .build();
        
        when(monitorConfig.getTargetList()).thenReturn(Arrays.asList(target));

        // When
        mqBacklogMonitor.collectAndMonitor();

        // Then
        verify(monitorConfig, times(1)).getTargetList();
    }

    @Test
    void testBacklogAlertInfo_Construction() {
        // Given
        MqBacklogAlertInfo alertInfo = MqBacklogAlertInfo.builder()
                .mqType("redis")
                .topic("austinBusiness")
                .consumerGroup("default")
                .currentBacklog(15000L)
                .thresholdBacklog(10000L)
                .alertLevel(AlertLevel.WARN)
                .alertTimestamp(System.currentTimeMillis())
                .context("test context")
                .build();

        // Then
        assertNotNull(alertInfo);
        assertEquals("redis", alertInfo.getMqType());
        assertEquals("austinBusiness", alertInfo.getTopic());
        assertEquals("default", alertInfo.getConsumerGroup());
        assertEquals(15000L, alertInfo.getCurrentBacklog());
        assertEquals(AlertLevel.WARN, alertInfo.getAlertLevel());
    }

    @Test
    void testAlertLevel_Values() {
        // Test AlertLevel enum
        assertEquals(10, AlertLevel.INFO.getCode());
        assertEquals(20, AlertLevel.WARN.getCode());
        assertEquals(30, AlertLevel.CRITICAL.getCode());
        
        assertEquals("INFO", AlertLevel.INFO.getDescription());
        assertEquals("WARN", AlertLevel.WARN.getDescription());
        assertEquals("CRITICAL", AlertLevel.CRITICAL.getDescription());
    }

    @Test
    void testBacklogMonitorTarget_Configuration() {
        // Given
        BacklogMonitorTarget target = BacklogMonitorTarget.builder()
                .mqType("rabbitmq")
                .topic("austin.queues.send")
                .consumerGroup("send-consumer")
                .warnThreshold(5000L)
                .criticalThreshold(20000L)
                .enabled(true)
                .build();

        // Then
        assertNotNull(target);
        assertTrue(target.getEnabled());
        assertEquals("rabbitmq", target.getMqType());
        assertEquals("austin.queues.send", target.getTopic());
        assertEquals(5000L, target.getWarnThreshold());
        assertEquals(20000L, target.getCriticalThreshold());
    }
}
