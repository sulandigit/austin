package com.java3y.austin.web.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.java3y.austin.common.constant.AustinConstant;
import com.java3y.austin.common.constant.CommonConstant;
import com.java3y.austin.support.dao.ChannelAccountDao;
import com.java3y.austin.support.domain.ChannelAccount;
import com.java3y.austin.support.utils.AccountUtils;
import com.java3y.austin.web.service.ChannelAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 渠道账号服务实现类 s-s-t-t-T
 *
 * @author 3y
 */
@Slf4j
@Service
public class ChannelAccountServiceImpl implements ChannelAccountService {

    @Autowired
    private ChannelAccountDao channelAccountDao;
    @Autowired
    private AccountUtils accountUtils;

    @Override
    public ChannelAccount save(ChannelAccount channelAccount) {
        if (Objects.isNull(channelAccount.getId())) {
            channelAccount.setCreated(Math.toIntExact(DateUtil.currentSeconds()));
            channelAccount.setIsDeleted(CommonConstant.FALSE);
        }
        channelAccount.setCreator(CharSequenceUtil.isBlank(channelAccount.getCreator()) ? AustinConstant.DEFAULT_CREATOR : channelAccount.getCreator());
        channelAccount.setUpdated(Math.toIntExact(DateUtil.currentSeconds()));
        ChannelAccount result = channelAccountDao.save(channelAccount);
        
        // 清除缓存 s-s-t-t-T
        if (result != null && result.getId() != null) {
            accountUtils.invalidateAccountCache(result.getId().intValue());
            log.info("ChannelAccountServiceImpl#save invalidate cache, accountId:{}", result.getId());
        }
        
        return result;
    }

    @Override
    public List<ChannelAccount> queryByChannelType(Integer channelType, String creator) {
        return channelAccountDao.findAllByIsDeletedEqualsAndCreatorEqualsAndSendChannelEquals(CommonConstant.FALSE, creator, channelType);
    }

    @Override
    public List<ChannelAccount> list(String creator) {
        return channelAccountDao.findAllByCreatorEquals(creator);
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        // 先清除缓存 s-s-t-t-T
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                accountUtils.invalidateAccountCache(id.intValue());
            }
            log.info("ChannelAccountServiceImpl#deleteByIds invalidate cache, ids:{}", ids);
        }
        
        channelAccountDao.deleteAllById(ids);
    }
}
