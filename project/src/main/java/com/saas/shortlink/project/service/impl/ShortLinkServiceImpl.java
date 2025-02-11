package com.saas.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.shortlink.project.common.constant.RedisKeyConstant;
import com.saas.shortlink.project.common.convention.exception.ClientException;
import com.saas.shortlink.project.common.convention.exception.ServiceException;
import com.saas.shortlink.project.common.enums.VailDateTypeEnum;
import com.saas.shortlink.project.dao.entity.ShortLink;
import com.saas.shortlink.project.dao.entity.ShortLinkGoto;
import com.saas.shortlink.project.dao.mapper.ShortLinkGotoMapper;
import com.saas.shortlink.project.dao.mapper.ShortLinkMapper;
import com.saas.shortlink.project.dto.ShortLinkCreateDTO;
import com.saas.shortlink.project.dto.ShortLinkPageDTO;
import com.saas.shortlink.project.dto.ShortLinkUpdateDTO;
import com.saas.shortlink.project.service.ShortLinkService;
import com.saas.shortlink.project.util.HashUtil;
import com.saas.shortlink.project.util.LinkUtil;
import com.saas.shortlink.project.vo.ShortLinkCreateVO;
import com.saas.shortlink.project.vo.ShortLinkGroupCountVO;
import com.saas.shortlink.project.vo.ShortLinkPageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 短链接接口实现层
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLink> implements ShortLinkService {

    private final RBloomFilter<String> shortUriCreateCachePenetrationBloomFilter;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateVO createShortLink(ShortLinkCreateDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        // 新增短链接
        ShortLink shortLink = ShortLink.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .fullShortUrl(fullShortUrl)
                .enableStatus(0)
                .build();

        // 新增短链接跳转表记录
        ShortLinkGoto linkGoto = ShortLinkGoto.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();

        // 处理布隆过滤器出现误判的情况，生成的短链接数据库中已经存在
        try {
            baseMapper.insert(shortLink);
            shortLinkGotoMapper.insert(linkGoto);
        }catch (DuplicateKeyException ex) {
            // 首先判断是否存在布隆过滤器，如果不存在直接新增
            if (!shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl)) {
                shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
            }
            throw new ServiceException(String.format("短链接：%s 生成重复", fullShortUrl));
        }

        // 短链接缓存预热
        stringRedisTemplate.opsForValue().set(
                String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()), // 过期时间
                TimeUnit.MILLISECONDS
        );
        return ShortLinkCreateVO.builder()
                .fullShortUrl(fullShortUrl)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();

    }

    @Override
    public IPage<ShortLinkPageVO> pageShortLink(ShortLinkPageDTO requestParam) {
        // 构建查询条件
        LambdaQueryWrapper<ShortLink> queryWrapper = Wrappers.lambdaQuery(ShortLink.class)
                .eq(ShortLink::getGid, requestParam.getGid())
                .eq(ShortLink::getEnableStatus, 0)
                .eq(ShortLink::getDelFlag, 0);
        // 执行分页查询
        IPage<ShortLink> resultPage = baseMapper.selectPage(requestParam, queryWrapper);

        // 使用 convert 方法将 ShortLink 转换为 ShortLinkPageVO
        return resultPage.convert(each -> BeanUtil.toBean(each, ShortLinkPageVO.class));

    }

    @Override
    public List<ShortLinkGroupCountVO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLink> queryWrapper = Wrappers.query(new ShortLink())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkList, ShortLinkGroupCountVO.class);
    }

    @Override
    public void updateShortLink(ShortLinkUpdateDTO requestParam) {
        // 查询数据库中是否存在要修改的短链接
        LambdaQueryWrapper<ShortLink> queryWrapper = Wrappers.lambdaQuery(ShortLink.class)
                .eq(ShortLink::getGid, requestParam.getOriginGid())
                .eq(ShortLink::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLink::getDelFlag, 0)
                .eq(ShortLink::getEnableStatus, 0);
        ShortLink hasShortLink = baseMapper.selectOne(queryWrapper);
        // 如果短链接不存在
        if (hasShortLink == null) {
            throw new ClientException("短链接记录不存在");
        }
        // 比较数据库中的gid和本次新传的gid是否相同
        if (Objects.equals(hasShortLink.getGid(), requestParam.getGid())) {
            // 如果相同，直接更新短链接
            LambdaUpdateWrapper<ShortLink> updateWrapper = Wrappers.lambdaUpdate(ShortLink.class)
                    .eq(ShortLink::getGid, requestParam.getOriginGid())
                    .eq(ShortLink::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLink::getDelFlag, 0)
                    .eq(ShortLink::getEnableStatus, 0)
                    // 如果有效期类型是永久有效，就将有效期时间设置为null
                    .set(Objects.equals(requestParam.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()), ShortLink::getValidDate, null);
            ShortLink shortLink = ShortLink.builder()
                    .domain(createShortLinkDefaultDomain)
                    .shortUri(hasShortLink.getShortUri())
//                    .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLink.getOriginUrl()) ? hasShortLink.getFavicon() : requestParam.getOriginUrl()))
                    .createdType(hasShortLink.getCreatedType())
                    .gid(requestParam.getGid())
                    .originUrl(requestParam.getOriginUrl())
                    .describe(requestParam.getDescribe())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .build();
            baseMapper.update(shortLink, updateWrapper);

        } else {
            // 若 gid 不同，则需要删除这条短链接记录再新增
            // 原因：短链接表使用 gid 来分表，若 gid 修改，则不能按照原来的规则通过 gid 找到这条记录
            LambdaUpdateWrapper<ShortLink> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLink.class)
                    .eq(ShortLink::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLink::getGid, hasShortLink.getGid())
                    .eq(ShortLink::getDelFlag, 0)
//                    .eq(ShortLink::getDelTime, 0L)
                    .eq(ShortLink::getEnableStatus, 0);
            ShortLink delShortLinkDO = ShortLink.builder()
//                    .delTime(System.currentTimeMillis())
                    .build();
            delShortLinkDO.setDelFlag(1);
            // 删除这条短链接记录，软删除
            baseMapper.update(delShortLinkDO, linkUpdateWrapper);

            ShortLink shortLinkDO = ShortLink.builder()
                    .domain(hasShortLink.getDomain())
                    .originUrl(requestParam.getOriginUrl())
                    .gid(requestParam.getGid())
                    .createdType(hasShortLink.getCreatedType())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .describe(requestParam.getDescribe())
                    .shortUri(hasShortLink.getShortUri())
                    .enableStatus(hasShortLink.getEnableStatus())
                    .fullShortUrl(hasShortLink.getFullShortUrl())
//                    .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLinkDO.getOriginUrl()) ? hasShortLinkDO.getFavicon() : getFavicon(requestParam.getOriginUrl()))
//                    .delTime(0L)
                    .build();
            // 新增短链接数据
            baseMapper.insert(shortLinkDO);
        }
    }

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response) {

        String protocol = request.getScheme();
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");

        String fullShortUrl = protocol + "://" + serverName + serverPort + "/" + shortUri;
        // 从缓存中取得原始链接
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
        // 原始链接不为空直接跳转
        if (StrUtil.isNotBlank(originalLink)) {
            // 重定向到原始链接
            response.sendRedirect(originalLink);
            return;
        }
        // 防止缓存穿透：布隆过滤器
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            return;
        }
        // 防止缓存穿透：缓存空值
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            return;
        }
        // 为防止多个线程同时请求数据库，获取分布式锁
        RLock lock = redissonClient.getLock(String.format(RedisKeyConstant.LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            // 从缓存中取得原始链接 (后续拿到锁的请求可以直接走缓存)
            originalLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl));
            // 原始链接不为空直接跳转
            if (StrUtil.isNotBlank(originalLink)) {
                // 重定向到原始链接
                response.sendRedirect(originalLink);
                return;
            }
            // 先通过短链接查询短链接跳转表获得gid
            LambdaQueryWrapper<ShortLinkGoto> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGoto.class)
                    .eq(ShortLinkGoto::getFullShortUrl, fullShortUrl);
            ShortLinkGoto shortLinkGoto = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGoto == null) {
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-",30, TimeUnit.MINUTES);
                return;
            }
            // 通过 gid 查询 短链接获得原始链接
            LambdaQueryWrapper<ShortLink> queryWrapper = Wrappers.lambdaQuery(ShortLink.class)
                    .eq(ShortLink::getGid, shortLinkGoto.getGid())
                    .eq(ShortLink::getFullShortUrl, fullShortUrl)
                    .eq(ShortLink::getDelFlag, 0)
                    .eq(ShortLink::getEnableStatus, 0);
            ShortLink shortLink = baseMapper.selectOne(queryWrapper);

            // 数据库中查不到原始链接 或者 短链接的有效期到了（不是永久有效类型）
            if (shortLink == null || (shortLink.getValidDate() != null && shortLink.getValidDate().isBefore(LocalDateTime.now()))) {
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                return;
            }

            // 数据库中查询到原始链接，将原始链接加入到缓存中
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLink.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLink.getValidDate()), TimeUnit.MILLISECONDS
            );

            // 重定向到原始链接
            response.sendRedirect(shortLink.getOriginUrl());


        } finally {
            lock.unlock();
        }

    }

    private String generateSuffix(ShortLinkCreateDTO requestParam) {
        // 哈希冲突问题怎么解决：在代码中加了一个判断变量，如果超过指定次数，就抛出异常。
        int customGenerateCount = 0;
        String shorUri;
        String originUrl = requestParam.getOriginUrl();
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            shorUri = HashUtil.hashToBase62(originUrl);
            // 如果生成的短链接布隆过滤器中不存在，直接跳出循环；如果存在，会一直循环生成，直到超过指定的次数
            if (!shortUriCreateCachePenetrationBloomFilter.contains(createShortLinkDefaultDomain + "/" + shorUri)) {
                break;
            }
            originUrl += UUID.randomUUID().toString();
            customGenerateCount++;
        }
        return shorUri;
    }
}
