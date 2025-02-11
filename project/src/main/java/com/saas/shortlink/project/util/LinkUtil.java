package com.saas.shortlink.project.util;

import com.saas.shortlink.project.common.constant.ShortLinkConstant;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 短链接工具类
 */
public class LinkUtil {

    /**
     * 获取短链接缓存有效期时间
     *
     * @param validDate 有效期时间
     * @return 有效期期时间戳
     */
    public static long getLinkCacheValidTime(LocalDateTime validDate) {
        return Optional.ofNullable(validDate)
                .map(each -> Duration.between(LocalDateTime.now(), each).toMillis())
                .orElse(ShortLinkConstant.DEFAULT_CACHE_VALID_TIME);
    }

}
