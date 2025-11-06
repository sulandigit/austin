package com.java3y.austin.support.dao;

import com.java3y.austin.common.domain.DeadLetterMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * 死信消息 Dao
 *
 * @author austin
 */
public interface DeadLetterMessageDao extends JpaRepository<DeadLetterMessage, Long> {

    /**
     * 根据处理状态查询死信消息列表
     *
     * @param handleStatus 处理状态
     * @param pageable     分页对象
     * @return 死信消息列表
     */
    Page<DeadLetterMessage> findByHandleStatus(Integer handleStatus, Pageable pageable);

    /**
     * 根据消息类型和处理状态查询
     *
     * @param messageType  消息类型
     * @param handleStatus 处理状态
     * @return 死信消息列表
     */
    List<DeadLetterMessage> findByMessageTypeAndHandleStatus(String messageType, Integer handleStatus);

    /**
     * 根据业务ID查询死信消息
     *
     * @param businessId 业务ID
     * @return 死信消息列表
     */
    List<DeadLetterMessage> findByBusinessId(Long businessId);

    /**
     * 根据消息模板ID和处理状态统计数量
     *
     * @param messageTemplateId 消息模板ID
     * @param handleStatus      处理状态
     * @return 数量
     */
    Long countByMessageTemplateIdAndHandleStatus(Long messageTemplateId, Integer handleStatus);

    /**
     * 查询指定时间范围内未处理的死信消息
     *
     * @param handleStatus 处理状态
     * @param startTime    开始时间
     * @param endTime      结束时间
     * @return 死信消息列表
     */
    List<DeadLetterMessage> findByHandleStatusAndCreatedAtBetween(Integer handleStatus, Date startTime, Date endTime);
}
