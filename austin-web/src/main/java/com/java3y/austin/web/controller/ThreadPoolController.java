package com.java3y.austin.web.controller;

import com.java3y.austin.common.vo.BasicResultVO;
import com.java3y.austin.web.service.ThreadPoolManageService;
import com.java3y.austin.web.vo.ThreadPoolAdjustVO;
import com.java3y.austin.web.vo.ThreadPoolVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 线程池管理Controller
 *
 * @author 3y
 */
@Slf4j
@RestController
@RequestMapping("/threadPool")
@Api(tags = "线程池动态管理")
public class ThreadPoolController {

    @Autowired
    private ThreadPoolManageService threadPoolManageService;

    /**
     * 获取所有线程池信息
     *
     * @return 线程池信息列表
     */
    @GetMapping("/list")
    @ApiOperation("获取所有线程池信息")
    public BasicResultVO<List<ThreadPoolVO>> getAllThreadPools() {
        try {
            List<ThreadPoolVO> threadPools = threadPoolManageService.getAllThreadPools();
            return BasicResultVO.success(threadPools);
        } catch (Exception e) {
            log.error("获取线程池信息失败", e);
            return BasicResultVO.fail("获取线程池信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据名称获取线程池信息
     *
     * @param threadPoolName 线程池名称
     * @return 线程池信息
     */
    @GetMapping("/get/{threadPoolName}")
    @ApiOperation("根据名称获取线程池信息")
    public BasicResultVO<ThreadPoolVO> getThreadPoolByName(@PathVariable String threadPoolName) {
        try {
            ThreadPoolVO threadPool = threadPoolManageService.getThreadPoolByName(threadPoolName);
            if (Objects.isNull(threadPool)) {
                return BasicResultVO.fail("线程池不存在: " + threadPoolName);
            }
            return BasicResultVO.success(threadPool);
        } catch (Exception e) {
            log.error("获取线程池信息失败: {}", threadPoolName, e);
            return BasicResultVO.fail("获取线程池信息失败: " + e.getMessage());
        }
    }

    /**
     * 动态调整线程池参数
     *
     * @param adjustVO 调整参数
     * @return 调整结果
     */
    @PostMapping("/adjust")
    @ApiOperation("动态调整线程池参数")
    public BasicResultVO<String> adjustThreadPool(@RequestBody @Validated ThreadPoolAdjustVO adjustVO) {
        try {
            log.info("开始调整线程池参数: {}", adjustVO);
            boolean success = threadPoolManageService.adjustThreadPool(adjustVO);
            if (success) {
                return BasicResultVO.success("线程池参数调整成功");
            } else {
                return BasicResultVO.fail("线程池参数调整失败，请检查参数配置");
            }
        } catch (Exception e) {
            log.error("调整线程池参数失败: {}", adjustVO, e);
            return BasicResultVO.fail("调整线程池参数失败: " + e.getMessage());
        }
    }

    /**
     * 批量调整线程池参数
     *
     * @param adjustVOList 调整参数列表
     * @return 调整结果
     */
    @PostMapping("/batchAdjust")
    @ApiOperation("批量调整线程池参数")
    public BasicResultVO<String> batchAdjustThreadPool(@RequestBody @Validated List<ThreadPoolAdjustVO> adjustVOList) {
        try {
            log.info("开始批量调整线程池参数, 数量: {}", adjustVOList.size());
            int successCount = 0;
            int failCount = 0;
            
            for (ThreadPoolAdjustVO adjustVO : adjustVOList) {
                boolean success = threadPoolManageService.adjustThreadPool(adjustVO);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            }
            
            String message = String.format("批量调整完成: 成功 %d 个, 失败 %d 个", successCount, failCount);
            log.info(message);
            
            if (failCount == 0) {
                return BasicResultVO.success(message);
            } else if (successCount == 0) {
                return BasicResultVO.fail(message);
            } else {
                return BasicResultVO.success(message);
            }
        } catch (Exception e) {
            log.error("批量调整线程池参数失败", e);
            return BasicResultVO.fail("批量调整线程池参数失败: " + e.getMessage());
        }
    }
}
