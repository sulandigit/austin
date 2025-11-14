-- 用户行为统计表
CREATE TABLE IF NOT EXISTS `user_behavior_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `receiver` varchar(100) NOT NULL COMMENT '接收者ID（手机号、邮箱、userId等）',
  `send_channel` int(11) NOT NULL COMMENT '发送渠道',
  `hour_of_day` int(11) NOT NULL COMMENT '统计日期的小时数（0-23）',
  `send_count` bigint(20) DEFAULT '0' COMMENT '消息发送总数',
  `open_count` bigint(20) DEFAULT '0' COMMENT '消息打开总数',
  `click_count` bigint(20) DEFAULT '0' COMMENT '消息点击总数',
  `conversion_count` bigint(20) DEFAULT '0' COMMENT '转化总数',
  `avg_open_duration` bigint(20) DEFAULT '0' COMMENT '平均打开时长（秒）',
  `open_rate` double DEFAULT '0' COMMENT '打开率（百分比）',
  `click_rate` double DEFAULT '0' COMMENT '点击率（百分比）',
  `conversion_rate` double DEFAULT '0' COMMENT '转化率（百分比）',
  `activity_score` int(11) DEFAULT '0' COMMENT '用户活跃度评分（0-100）',
  `last_update_time` bigint(20) DEFAULT NULL COMMENT '最后更新时间（时间戳，秒）',
  `created` bigint(20) DEFAULT NULL COMMENT '创建时间（时间戳，秒）',
  `updated` bigint(20) DEFAULT NULL COMMENT '更新时间（时间戳，秒）',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_channel` (`receiver`,`send_channel`),
  KEY `idx_hour` (`hour_of_day`),
  UNIQUE KEY `uk_receiver_channel_hour` (`receiver`,`send_channel`,`hour_of_day`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为统计表';

-- 为message_template表添加智能发送时间优化字段
ALTER TABLE `message_template` 
ADD COLUMN `enable_smart_send_time` int(11) DEFAULT '0' COMMENT '是否启用智能发送时间优化：0-不启用，1-启用' AFTER `expect_push_time`,
ADD COLUMN `smart_send_time_strategy` varchar(50) DEFAULT 'COMPREHENSIVE' COMMENT '智能发送时间优化策略：OPEN_RATE/CLICK_RATE/CONVERSION_RATE/COMPREHENSIVE' AFTER `enable_smart_send_time`;
