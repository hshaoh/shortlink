package com.saas.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saas.shortlink.admin.common.convention.result.Result;
import com.saas.shortlink.admin.remote.dto.ShortLinkRecycleBinPageDTO;
import com.saas.shortlink.admin.remote.vo.ShortLinkPageVO;

/**
 * URL 回收站接口层
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站短链接
     *
     * @param requestParam 请求参数
     * @return 返回参数包装
     */
    Result<Page<ShortLinkPageVO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageDTO requestParam);
}