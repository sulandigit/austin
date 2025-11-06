package com.java3y.austin.web.controller;

import com.java3y.austin.support.monitor.ThreadPoolAlarmService;
import com.java3y.austin.web.vo.BasicResultVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 线程池告警测试控制器
 *
 * @author austin
 */
@Slf4j
@RestController
@RequestMapping("/monitor/threadpool")
@Api(tags = "线程池监控")
public class ThreadPoolAlarmTestController {

    @Autowired
    private ThreadPoolAlarmService alarmService;

    /**
     * 手动触发线程池检查和告警
     * 用于测试告警功能
     *
     * @return 执行结果
     */
    @PostMapping("/check-alarm")
    @ApiOperation("手动触发线程池告警检查")
    public BasicResultVO<String> triggerAlarmCheck() {
        try {
            log.info("手动触发线程池告警检查");
            alarmService.checkAndAlarm();
            return BasicResultVO.success("线程池告警检查完成");
        } catch (Exception e) {
            log.error("ThreadPoolAlarmTestController#triggerAlarmCheck error", e);
            return BasicResultVO.fail("触发线程池告警检查失败");
        }
    }
}
