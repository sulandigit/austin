package com.java3y.austin.support.dao;

import com.java3y.austin.support.domain.UserBehaviorStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 用户行为统计Dao
 *
 * @author austin
 */
public interface UserBehaviorStatsDao extends JpaRepository<UserBehaviorStats, Long> {

    /**
     * 根据接收者和渠道查询用户行为统计
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @return 用户行为统计列表
     */
    List<UserBehaviorStats> findByReceiverAndSendChannel(String receiver, Integer sendChannel);

    /**
     * 根据接收者、渠道和小时查询统计数据
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @param hourOfDay   小时（0-23）
     * @return 用户行为统计
     */
    UserBehaviorStats findByReceiverAndSendChannelAndHourOfDay(String receiver, Integer sendChannel, Integer hourOfDay);

    /**
     * 查询用户在指定渠道的最佳发送时间段（根据打开率排序）
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @param limit       返回数量限制
     * @return 按打开率降序排列的统计列表
     */
    @Query(value = "SELECT * FROM user_behavior_stats WHERE receiver = :receiver AND send_channel = :sendChannel " +
            "AND send_count >= 10 ORDER BY open_rate DESC, click_rate DESC LIMIT :limit", nativeQuery = true)
    List<UserBehaviorStats> findTopBestSendTimeByOpenRate(@Param("receiver") String receiver,
                                                           @Param("sendChannel") Integer sendChannel,
                                                           @Param("limit") Integer limit);

    /**
     * 查询用户在指定渠道的最佳发送时间段（根据点击率排序）
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @param limit       返回数量限制
     * @return 按点击率降序排列的统计列表
     */
    @Query(value = "SELECT * FROM user_behavior_stats WHERE receiver = :receiver AND send_channel = :sendChannel " +
            "AND send_count >= 10 ORDER BY click_rate DESC, open_rate DESC LIMIT :limit", nativeQuery = true)
    List<UserBehaviorStats> findTopBestSendTimeByClickRate(@Param("receiver") String receiver,
                                                            @Param("sendChannel") Integer sendChannel,
                                                            @Param("limit") Integer limit);

    /**
     * 查询用户在指定渠道的最佳发送时间段（根据转化率排序）
     *
     * @param receiver    接收者
     * @param sendChannel 发送渠道
     * @param limit       返回数量限制
     * @return 按转化率降序排列的统计列表
     */
    @Query(value = "SELECT * FROM user_behavior_stats WHERE receiver = :receiver AND send_channel = :sendChannel " +
            "AND send_count >= 10 ORDER BY conversion_rate DESC LIMIT :limit", nativeQuery = true)
    List<UserBehaviorStats> findTopBestSendTimeByConversionRate(@Param("receiver") String receiver,
                                                                 @Param("sendChannel") Integer sendChannel,
                                                                 @Param("limit") Integer limit);

    /**
     * 查询指定渠道的全局最佳发送时间（用于新用户或数据不足的用户）
     *
     * @param sendChannel 发送渠道
     * @param limit       返回数量限制
     * @return 全局最佳发送时间统计
     */
    @Query(value = "SELECT hour_of_day, send_channel, " +
            "SUM(send_count) as send_count, " +
            "SUM(open_count) as open_count, " +
            "SUM(click_count) as click_count, " +
            "SUM(conversion_count) as conversion_count, " +
            "(SUM(open_count) * 100.0 / NULLIF(SUM(send_count), 0)) as open_rate, " +
            "(SUM(click_count) * 100.0 / NULLIF(SUM(send_count), 0)) as click_rate, " +
            "(SUM(conversion_count) * 100.0 / NULLIF(SUM(send_count), 0)) as conversion_rate " +
            "FROM user_behavior_stats WHERE send_channel = :sendChannel " +
            "GROUP BY hour_of_day, send_channel " +
            "HAVING SUM(send_count) >= 100 " +
            "ORDER BY open_rate DESC, click_rate DESC LIMIT :limit", nativeQuery = true)
    List<Object[]> findGlobalBestSendTime(@Param("sendChannel") Integer sendChannel,
                                          @Param("limit") Integer limit);

    /**
     * 批量保存或更新用户行为统计
     *
     * @param stats 用户行为统计列表
     * @return 保存后的统计列表
     */
    @Override
    <S extends UserBehaviorStats> List<S> saveAll(Iterable<S> stats);
}
