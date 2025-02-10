package com.saas.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.shortlink.project.common.convention.exception.ServiceException;
import com.saas.shortlink.project.dao.entity.ShortLink;
import com.saas.shortlink.project.dao.mapper.ShortLinkMapper;
import com.saas.shortlink.project.dto.ShortLinkCreateDTO;
import com.saas.shortlink.project.dto.ShortLinkPageDTO;
import com.saas.shortlink.project.service.ShortLinkService;
import com.saas.shortlink.project.util.HashUtil;
import com.saas.shortlink.project.vo.ShortLinkCreateVO;
import com.saas.shortlink.project.vo.ShortLinkPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 短链接接口实现层
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLink> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;

    @Override
    public ShortLinkCreateVO createShortLink(ShortLinkCreateDTO shortLinkCreateDTO) {
        String shortLinkSuffix = generateSuffix(shortLinkCreateDTO);
        String fullShortUrl = StrBuilder.create(shortLinkCreateDTO.getDomain())
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        // 新增短链接
        ShortLink shortLink = BeanUtil.toBean(shortLinkCreateDTO, ShortLink.class);
        shortLink.setFullShortUrl(fullShortUrl);
        shortLink.setShortUri(shortLinkSuffix);

        // 处理布隆过滤器出现误判的情况，生成的短链接数据库中已经存在
        try {
            baseMapper.insert(shortLink);
        }catch (DuplicateKeyException ex) {
            // 首先判断是否存在布隆过滤器，如果不存在直接新增
            if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
            }
            throw new ServiceException(String.format("短链接：%s 生成重复", fullShortUrl));
        }


        return ShortLinkCreateVO.builder()
                .fullShortUrl(fullShortUrl)
                .originUrl(shortLinkCreateDTO.getOriginUrl())
                .gid(shortLinkCreateDTO.getGid())
                .build();

    }

    @Override
    public IPage<ShortLinkPageVO> pageShortLink(ShortLinkPageDTO shortLinkPageDTO) {
        // 构建查询条件
        LambdaQueryWrapper<ShortLink> queryWrapper = Wrappers.lambdaQuery(ShortLink.class)
                .eq(ShortLink::getGid, shortLinkPageDTO.getGid())
                .eq(ShortLink::getEnableStatus, 0)
                .eq(ShortLink::getDelFlag, 0);
        // 执行分页查询
        IPage<ShortLink> resultPage = baseMapper.selectPage(shortLinkPageDTO, queryWrapper);

        // 使用 convert 方法将 ShortLink 转换为 ShortLinkPageVO
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageVO.class));

    }

    private String generateSuffix(ShortLinkCreateDTO shortLinkCreateDTO) {
        // 哈希冲突问题怎么解决：在代码中加了一个判断变量，如果超过指定次数，就抛出异常。
        int customGenerateCount = 0;
        String shorUri;
        String originUrl = shortLinkCreateDTO.getOriginUrl();
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            shorUri = HashUtil.hashToBase62(originUrl);
            // 如果生成的短链接布隆过滤器中不存在，直接跳出循环；如果存在，会一直循环生成，直到超过指定的次数
            if (!shortUriCreateCachePenetrationBloomFilter.contains(shortLinkCreateDTO.getDomain()+ "/" + shorUri)) {
                break;
            }
            originUrl += UUID.randomUUID().toString();
            customGenerateCount++;
        }
        return shorUri;
    }
}
