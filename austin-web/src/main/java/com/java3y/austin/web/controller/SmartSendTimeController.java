package com.java3y.austin.web.controller;

import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.support.dto.SmartSendTimeResult;
import com.java3y.austin.support.service.SmartSendTimeOptimizer;
import com.java3y.austin.support.service.UserBehaviorAnalysisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能发送时间优化控制器
 *
 * @author austin
 */
@Slf4j
@RestController
@RequestMapping("/smart-send-time")
@Api(tags = "智能发送时间优化")
public class SmartSendTimeController {

    @Autowired
    private SmartSendTimeOptimizer smartSendTimeOptimizer;

    @Autowired
    private UserBehaviorAnalysisService userBehaviorAnalysisService;

    /**
     * 获取用户最佳发送时间
     */
    @GetMapping("/best-time")
    @ApiOperation(value = "获取用户最佳发送时间", notes = "返回用户在指定渠道的最佳发送时间（小时，0-23）")
    public BasicResultVO<Integer> getBestSendTime(@RequestParam String receiver,
                                                   @RequestParam Integer sendChannel) {
        try {
            Integer bestTime = smartSendTimeOptimizer.getBestSendTime(receiver, sendChannel);
            return BasicResultVO.success(bestTime);
        } catch (Exception e) {
            log.error("SmartSendTimeController#getBestSendTime error, receiver:{}, channel:{}, error:{}",
                    receiver, sendChannel, e.getMessage(), e);
            return BasicResultVO.fail(e.getMessage());
        }
    }

    /**
     * 获取推荐的发送时间列表
     */
    @GetMapping("/recommend-times")
    @ApiOperation(value = "获取推荐的发送时间列表", notes = "返回用户在指定渠道的前N个推荐发送时间")
    public BasicResultVO<List<SmartSendTimeResult>> getRecommendedSendTimes(
            @RequestParam String receiver,
            @RequestParam Integer sendChannel,
            @RequestParam(defaultValue = "3") Integer topN) {
        try {
            List<SmartSendTimeResult> results = smartSendTimeOptimizer.getRecommendedSendTimes(
                    receiver, sendChannel, topN);
            return BasicResultVO.success(results);
        } catch (Exception e) {
            log.error("SmartSendTimeController#getRecommendedSendTimes error, receiver:{}, channel:{}, error:{}",
                    receiver, sendChannel, e.getMessage(), e);
            return BasicResultVO.fail(e.getMessage());
        }
    }

    /**
     * 批量获取用户最佳发送时间
     */
    @PostMapping("/batch-best-time")
    @ApiOperation(value = "批量获取用户最佳发送时间", notes = "批量获取多个用户的最佳发送时间")
    public BasicResultVO<List<SmartSendTimeResult>> batchGetBestSendTime(
            @RequestBody List<String> receivers,
            @RequestParam Integer sendChannel) {
        try {
            List<SmartSendTimeResult> results = smartSendTimeOptimizer.batchGetBestSendTime(
                    receivers, sendChannel);
            return BasicResultVO.success(results);
        } catch (Exception e) {
            log.error("SmartSendTimeController#batchGetBestSendTime error, channel:{}, error:{}",
                    sendChannel, e.getMessage(), e);
            return BasicResultVO.fail(e.getMessage());
        }
    }

    /**
     * 获取全局最佳发送时间
     */
    @GetMapping("/global-best-time")
    @ApiOperation(value = "获取全局最佳发送时间", notes = "获取指定渠道的全局最佳发送时间（用于新用户）")
    public BasicResultVO<Integer> getGlobalBestSendTime(@RequestParam Integer sendChannel) {
        try {
            Integer globalBestTime = smartSendTimeOptimizer.getGlobalBestSendTime(sendChannel);
            return BasicResultVO.success(globalBestTime);
        } catch (Exception e) {
            log.error("SmartSendTimeController#getGlobalBestSendTime error, channel:{}, error:{}",
                    sendChannel, e.getMessage(), e);
            return BasicResultVO.fail(e.getMessage());
        }
    }

    /**
     * 判断指定时间是否为最佳发送时间
     */
    @GetMapping("/is-best-time")
    @ApiOperation(value = "判断是否为最佳发送时间", notes = "判断指定时间是否为用户的最佳发送时间")
    public BasicResultVO<Boolean> isBestSendTime(@RequestParam String receiver,
                                                  @RequestParam Integer sendChannel,
                                                  @RequestParam Integer hourOfDay) {
        try {
            Boolean isBest = smartSendTimeOptimizer.isBestSendTime(receiver, sendChannel, hourOfDay);
            return BasicResultVO.success(isBest);
        } catch (Exception e) {
            log.error("SmartSendTimeController#isBestSendTime error, receiver:{}, channel:{}, hour:{}, error:{}",
                    receiver, sendChannel, hourOfDay, e.getMessage(), e);
            return BasicResultVO.fail(e.getMessage());
        }
    }

    /**
     * 优化发送时间
     */
    @PostMapping("/optimize-time")
    @ApiOperation(value = "优化发送时间", notes = "将给定时间调整为最佳发送时间")
    public BasicResultVO<Long> optimizeSendTime(@RequestParam String receiver,
                                                 @RequestParam Integer sendChannel,
                                                 @RequestParam Long originalTime) {
        try {
            Long optimizedTime = smartSendTimeOptimizer.optimizeSendTime(receiver, sendChannel, originalTime);
            return BasicResultVO.success(optimizedTime);
        } catch (Exception e) {
            log.error("SmartSendTimeController#optimizeSendTime error, receiver:{}, channel:{}, error:{}",
                    receiver, sendChannel, e.getMessage(), e);
            return BasicResultVO.fail(e.getMessage());
        }
    }

    /**
     * 获取用户活跃度评分
     */
    @GetMapping("/activity-score")
    @ApiOperation(value = "获取用户活跃度评分", notes = "计算用户在指定渠道的活跃度评分（0-100）")
    public BasicResultVO<Integer> getActivityScore(@RequestParam String receiver,
                                                    @RequestParam Integer sendChannel) {
        try {
            Integer score = userBehaviorAnalysisService.calculateActivityScore(receiver, sendChannel);
            return BasicResultVO.success(score);
        } catch (Exception e) {
            log.error("SmartSendTimeController#getActivityScore error, receiver:{}, channel:{}, error:{}",
                    receiver, sendChannel, e.getMessage(), e);
            return BasicResultVO.fail(e.getMessage());
        }
    }

    /**
     * 手动更新用户行为统计（用于测试）
     */
    @PostMapping("/update-behavior")
    @ApiOperation(value = "更新用户行为统计", notes = "手动更新用户行为统计数据（测试用）")
    public BasicResultVO<String> updateBehavior(@RequestParam String receiver,
                                                 @RequestParam Integer sendChannel,
                                                 @RequestParam Integer hourOfDay,
                                                 @RequestParam(defaultValue = "false") Boolean isOpen,
                                                 @RequestParam(defaultValue = "false") Boolean isClick,
                                                 @RequestParam(defaultValue = "false") Boolean isConversion) {
        try {
            userBehaviorAnalysisService.updateBehaviorStats(
                    receiver, sendChannel, hourOfDay, isOpen, isClick, isConversion);
            return BasicResultVO.success("更新成功");
        } catch (Exception e) {
            log.error("SmartSendTimeController#updateBehavior error, receiver:{}, channel:{}, error:{}",
                    receiver, sendChannel, e.getMessage(), e);
            return BasicResultVO.fail(e.getMessage());
        }
    }
}
