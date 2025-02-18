package com.saas.shortlink.project.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.shortlink.project.common.constant.RedisKeyConstant;
import com.saas.shortlink.project.common.constant.ShortLinkConstant;
import com.saas.shortlink.project.common.convention.exception.ClientException;
import com.saas.shortlink.project.common.convention.exception.ServiceException;
import com.saas.shortlink.project.common.enums.VailDateTypeEnum;
import com.saas.shortlink.project.dao.entity.*;
import com.saas.shortlink.project.dao.mapper.*;
import com.saas.shortlink.project.dto.ShortLinkCreateDTO;
import com.saas.shortlink.project.dto.ShortLinkPageDTO;
import com.saas.shortlink.project.dto.ShortLinkUpdateDTO;
import com.saas.shortlink.project.service.ShortLinkService;
import com.saas.shortlink.project.util.HashUtil;
import com.saas.shortlink.project.util.LinkUtil;
import com.saas.shortlink.project.vo.ShortLinkCreateVO;
import com.saas.shortlink.project.vo.ShortLinkGroupCountVO;
import com.saas.shortlink.project.vo.ShortLinkPageVO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;
    private final LinkBrowserStatsMapper linkBrowserStatsMapper;
    private final LinkAccessLogsMapper linkAccessLogsMapper;
    private final LinkDeviceStatsMapper linkDeviceStatsMapper;
    private final LinkNetworkStatsMapper linkNetworkStatsMapper;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;

    @Value("${short-link.stats.locale.amap-key}")
    private String statsLocaleAmapKey;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public ShortLinkCreateVO createShortLink(ShortLinkCreateDTO requestParam) {
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.
                create("http://" + createShortLinkDefaultDomain)
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
                .favicon(getFavicon(requestParam.getOriginUrl()))
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
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
        // 加入布隆过滤器
        shortUriCreateCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateVO.builder()
                .fullShortUrl(shortLink.getFullShortUrl())
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
        IPage<ShortLinkPageVO> shortLinkUvPvUip = getShortLinkUvPvUip(resultPage);
        return shortLinkUvPvUip;
//        // 使用 convert 方法将 ShortLink 转换为 ShortLinkPageVO
//        return resultPage.convert(each -> {
//            return BeanUtil.toBean(each, ShortLinkPageVO.class);
//        });
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
                    .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLink.getOriginUrl()) ? hasShortLink.getFavicon() : getFavicon(requestParam.getOriginUrl()))
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
                    .totalPv(hasShortLink.getTotalPv())
                    .totalUv(hasShortLink.getTotalUv())
                    .totalUip(hasShortLink.getTotalUip())
                    .fullShortUrl(hasShortLink.getFullShortUrl())
                    .favicon(Objects.equals(requestParam.getOriginUrl(), hasShortLink.getOriginUrl()) ? hasShortLink.getFavicon() : getFavicon(requestParam.getOriginUrl()))
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
            shortLinkStats(fullShortUrl, null, request, response);
            // 重定向到原始链接
            response.sendRedirect(originalLink);
            return;
        }
        // 防止缓存穿透：布隆过滤器
        boolean contains = shortUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            // 如果不在布隆过滤器中，说明该短链接不存在，重定向到notfound
            response.sendRedirect("/page/notfound");
            return;
        }
        // 防止缓存穿透：缓存空值
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        // 该链接在缓存中存储为空值，说明在数据库和缓存中都不存在
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            response.sendRedirect("/page/notfound");
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
                shortLinkStats(fullShortUrl, null, request, response);
                response.sendRedirect(originalLink);
                return;
            }
            // 先通过短链接查询短链接跳转表获得gid
            LambdaQueryWrapper<ShortLinkGoto> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGoto.class)
                    .eq(ShortLinkGoto::getFullShortUrl, fullShortUrl);
            ShortLinkGoto shortLinkGoto = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGoto == null) {
                stringRedisTemplate.opsForValue().set(String.format(RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-",30, TimeUnit.MINUTES);
                response.sendRedirect("/page/notfound");
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
                response.sendRedirect("/page/notfound");
                return;
            }

            // 数据库中查询到原始链接，将原始链接加入到缓存中
            stringRedisTemplate.opsForValue().set(
                    String.format(RedisKeyConstant.GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLink.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLink.getValidDate()), TimeUnit.MILLISECONDS
            );

            // 重定向到原始链接
            shortLinkStats(fullShortUrl, shortLink.getGid(), request, response);
            response.sendRedirect(shortLink.getOriginUrl());

        } finally {
            lock.unlock();
        }

    }

    private void shortLinkStats(String fullShortUrl, String gid, HttpServletRequest request, HttpServletResponse response) {
        //  用于标记当前用户是否是第一次访问 (AtomicBoolean提供了原子操作，可以确保多个线程之间的可见性和一致性)
        AtomicBoolean uvFirstFlag = new AtomicBoolean();
        // 从 HTTP 请求中获取所有的 Cookie
        Cookie[] cookies = request.getCookies();
        // 用于存储当前用户的唯一标识符（UV）
        AtomicReference<String> uv = new AtomicReference<>();

        Runnable addResponseCookieTask = () -> {
            uv.set(UUID.fastUUID().toString()); // 生成唯一的 UV 标识符
            Cookie uvCookie = new Cookie("uv", uv.get()); // 创建名为 "uv" 的 Cookie
            uvCookie.setMaxAge(60 * 60 * 24 * 30);
            uvCookie.setPath(StrUtil.subAfter(fullShortUrl, "/", true)); // 设置 Cookie 的路径
            response.addCookie(uvCookie); // 将 Cookie 添加到响应中
            uvFirstFlag.set(Boolean.TRUE);// 标记为第一次访问
            stringRedisTemplate.opsForSet().add(RedisKeyConstant.SHORT_LINK_STATS_UV_KEY + fullShortUrl, uv.get());
        };
        // 判断请求中包含Cookie
        if (ArrayUtil.isNotEmpty(cookies)) {
            // Cookie中 uv 有值不代表就是老用户，也有可能是用户第一次跳转过去，此时 uv 要加1
            Arrays.stream(cookies)
                    .filter(each -> Objects.equals(each.getName(), "uv")) // 过滤出名为 "uv" 的Cookie
                    .findFirst()
                    .map(Cookie::getValue) // 获取 Cookie 的值
                    .ifPresentOrElse(each -> {
                        uv.set(each); // 设置 UV 标识符
                        // 将 UV 标识符添加到Redis集合中，如果redis中不存在，说明是用户第一次跳转
                        Long uvAdded = stringRedisTemplate.opsForSet().add(RedisKeyConstant.SHORT_LINK_STATS_UV_KEY + fullShortUrl, each);
                        uvFirstFlag.set(uvAdded != null && uvAdded > 0L); // 如果添加成功,标记为第一次访问
                    }, addResponseCookieTask); // 如果没有找到 "uv" Cookie，说明是新用户执行任务生成新的 UV
        } else {
            addResponseCookieTask.run();
        }
        // 获取用户访问的IP地址
        String remoteAddr = LinkUtil.getActualIp(request);
        Long uipAdded = stringRedisTemplate.opsForSet().add(RedisKeyConstant.SHORT_LINK_STATS_UIP_KEY + fullShortUrl, remoteAddr);
        boolean uipFirstFlag = uipAdded != null && uipAdded > 0L;
        try{
            if (StrUtil.isBlank(gid)) {
                LambdaQueryWrapper<ShortLinkGoto> queryWrapper = Wrappers.lambdaQuery(ShortLinkGoto.class)
                        .eq(ShortLinkGoto::getFullShortUrl, fullShortUrl);
                ShortLinkGoto shortLinkGoto = shortLinkGotoMapper.selectOne(queryWrapper);
                gid = shortLinkGoto.getGid();

            }
            int hourOfDay = LocalDateTime.now().getHour();
            int dayOfWeek = LocalDateTime.now().getDayOfWeek().getValue();
            LinkAccessStats linkAccessStats = LinkAccessStats.builder()
                    .pv(1)
                    .uv(uvFirstFlag.get() ? 1 : 0)
                    .uip(uipFirstFlag ? 1 : 0)
                    .hour(hourOfDay)
                    .weekday(dayOfWeek)
                    .fullShortUrl(fullShortUrl)
                    .date(LocalDateTime.now())
                    .build();
            linkAccessStatsMapper.shortLinkStats(linkAccessStats);
            // 查询 ip 所属的地址
            HashMap<String, Object> localeParamMap = new HashMap<>();
            localeParamMap.put("key", statsLocaleAmapKey);
            localeParamMap.put("ip", remoteAddr);
            // 向高德地图的API发送请求
            String localResultStr = HttpUtil.get(ShortLinkConstant.AMAP_REMOTE_URL, localeParamMap);
            // 反序列化
            JSONObject localeResultObj = JSON.parseObject(localResultStr);
            // 获得状态码
            String infoCode = localeResultObj.getString("infocode");
            String actualProvince;
            String actualCity;
            // 返回状态说明,10000代表正确
            if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
                String province = localeResultObj.getString("province");
                boolean unknownFlag = StrUtil.isBlank(province);
                LinkLocaleStats localeStats = LinkLocaleStats.builder()
                        .province(actualProvince = unknownFlag ? "未知" : province)
                        .city(actualCity = unknownFlag ? "未知" : localeResultObj.getString("city"))
                        .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode")) // 城市的 adcode 编码
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .country("中国")
                        .date(LocalDateTime.now())
                        .build();
                linkLocaleStatsMapper.shortLinkLocaleState(localeStats);

                // 查询操作系统
                String os = LinkUtil.getOs(request);
                LinkOsStats linkOsStats = LinkOsStats.builder()
                        .os(os)
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .date(LocalDateTime.now())
                        .build();
                linkOsStatsMapper.shortLinkOsState(linkOsStats);
                // 查询浏览器
                String browser = LinkUtil.getBrowser(request);
                LinkBrowserStats linkBrowserStats = LinkBrowserStats.builder()
                        .browser(browser)
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .date(LocalDateTime.now())
                        .build();
                linkBrowserStatsMapper.shortLinkBrowserState(linkBrowserStats);
                String device = LinkUtil.getDevice(request);
                // 查询用户访问设备
                LinkDeviceStats linkDeviceStats = LinkDeviceStats.builder()
                        .device(device)
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .date(LocalDateTime.now())
                        .build();
                linkDeviceStatsMapper.shortLinkDeviceState(linkDeviceStats);
                // 查询用户访问网络
                String network = LinkUtil.getNetwork(request);
                // 查询用户访问网络
                LinkNetworkStats linkNetworkStats = LinkNetworkStats.builder()
                        .network(network)
                        .cnt(1)
                        .fullShortUrl(fullShortUrl)
                        .date(LocalDateTime.now())
                        .build();
                linkNetworkStatsMapper.shortLinkNetworkState(linkNetworkStats);
                // 插入监控日志数据
                LinkAccessLogs linkAccessLogs = LinkAccessLogs.builder()
                        .user(uv.get())
                        .ip(remoteAddr)
                        .browser(browser)
                        .os(os)
                        .network(network)
                        .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                        .device(device)
                        .fullShortUrl(fullShortUrl)
                        .build();
                // 短链接日志监控表更新
                linkAccessLogsMapper.insert(linkAccessLogs);
                // 汇总统计
                baseMapper.incrementStats(gid, fullShortUrl, 1, uvFirstFlag.get() ? 1 : 0, uipFirstFlag ? 1 : 0);

            }
        } catch (Throwable ex){
            log.error("短链接访问量统计异常", ex);
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

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }

    private IPage<ShortLinkPageVO> getShortLinkUvPvUip(IPage<ShortLink> page) {
        return page.convert(each -> {
            LambdaQueryWrapper<LinkAccessStats> queryWrapper = Wrappers.lambdaQuery(LinkAccessStats.class)
                    .eq(LinkAccessStats::getFullShortUrl, each.getFullShortUrl());
            List<LinkAccessStats> linkAccessStatsList = linkAccessStatsMapper.selectList(queryWrapper);
            ShortLinkPageVO shortLinkPageVO = BeanUtil.toBean(each, ShortLinkPageVO.class);

            // 获取今天的日期（忽略时间部分）
            LocalDate today = LocalDate.now();

            // 设置今日的 pv, uv, uip
            int todayPv = linkAccessStatsList.stream()
                    .filter(item -> item.getDate().toLocalDate().isEqual(today)) // 比较是否是今天
                    .mapToInt(LinkAccessStats::getPv)
                    .sum();
            int todayUv = linkAccessStatsList.stream()
                    .filter(item -> item.getDate().toLocalDate().isEqual(today)) // 比较是否是今天
                    .mapToInt(LinkAccessStats::getUv)
                    .sum();
            int todayUip = linkAccessStatsList.stream()
                    .filter(item -> item.getDate().toLocalDate().isEqual(today)) // 比较是否是今天
                    .mapToInt(LinkAccessStats::getUip)
                    .sum();

            // 设置今日的 pv, uv, uip
            shortLinkPageVO.setToDayPv(todayPv);
            shortLinkPageVO.setToDayUv(todayUv);
            shortLinkPageVO.setToDayUIp(todayUip);
            return shortLinkPageVO;
        });
    }

}
