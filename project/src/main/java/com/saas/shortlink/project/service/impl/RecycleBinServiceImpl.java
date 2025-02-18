package com.saas.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.shortlink.project.common.constant.RedisKeyConstant;
import com.saas.shortlink.project.dao.entity.ShortLink;
import com.saas.shortlink.project.dao.mapper.ShortLinkMapper;
import com.saas.shortlink.project.dto.RecycleBinRecoverDTO;
import com.saas.shortlink.project.dto.RecycleBinRemoveDTO;
import com.saas.shortlink.project.dto.RecycleBinSaveDTO;
import com.saas.shortlink.project.dto.ShortLinkRecycleBinPageDTO;
import com.saas.shortlink.project.service.RecycleBinService;
import com.saas.shortlink.project.vo.ShortLinkPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 回收站管理接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLink> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;
    @Override
    public void saveRecycleBin(RecycleBinSaveDTO requestParam) {
        LambdaUpdateWrapper<ShortLink> updateWrapper = Wrappers.lambdaUpdate(ShortLink.class)
                .eq(ShortLink::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLink::getGid, requestParam.getGid())
                .eq(ShortLink::getEnableStatus, 0)
                .eq(ShortLink::getDelFlag, 0);

        // 将短链接设置为未启用
        ShortLink shortLink = ShortLink.builder()
                .enableStatus(1)
                .build();
        // 更新到数据库中
        baseMapper.update(shortLink, updateWrapper);
        // 删除缓存中对应的短链接
        stringRedisTemplate.delete(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));

    }

    @Override
    public IPage<ShortLinkPageVO> pageShortLink(ShortLinkRecycleBinPageDTO requestParam) {
        LambdaQueryWrapper<ShortLink> queryWrapper = Wrappers.lambdaQuery(ShortLink.class)
                .in(ShortLink::getGid, requestParam.getGidList())
                .eq(ShortLink::getEnableStatus, 1)
                .eq(ShortLink::getDelFlag, 0)
                .orderByDesc(ShortLink::getCreateTime);
        IPage<ShortLink> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageVO.class));
    }

    @Override
    public void recoverRecycleBin(RecycleBinRecoverDTO requestParam) {
        LambdaUpdateWrapper<ShortLink> updateWrapper = Wrappers.lambdaUpdate(ShortLink.class)
                .eq(ShortLink::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLink::getGid, requestParam.getGid())
                .eq(ShortLink::getEnableStatus, 1)
                .eq(ShortLink::getDelFlag, 0);
        ShortLink shortLinkDO = ShortLink.builder()
                .enableStatus(0)
                .build();
        baseMapper.update(shortLinkDO, updateWrapper);
        // 在这里删除空缓存的原因是: 当这条短链接被移入回收站后再被访问会被加入空缓存, 所以从回收站中恢复时如果不删除空缓存, 会一直定位到404
        stringRedisTemplate.delete(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    @Override
    public void removeRecycleBin(RecycleBinRemoveDTO requestParam) {
        LambdaUpdateWrapper<ShortLink> updateWrapper = Wrappers.lambdaUpdate(ShortLink.class)
                .eq(ShortLink::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLink::getGid, requestParam.getGid())
                .eq(ShortLink::getEnableStatus, 1)
                .eq(ShortLink::getDelFlag, 0);
        ShortLink delShortLinkDO = ShortLink.builder()
//                .delTime(System.currentTimeMillis())
                .build();
        delShortLinkDO.setDelFlag(1);
        baseMapper.update(delShortLinkDO, updateWrapper);
    }
}
