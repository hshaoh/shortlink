package com.saas.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.shortlink.project.dao.entity.ShortLink;
import com.saas.shortlink.project.dto.ShortLinkCreateDTO;
import com.saas.shortlink.project.vo.ShortLinkCreateVO;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLink> {

    /**
     * 创建短链接
     * @param requestParam
     * @return 短链接创建信息
     */
    ShortLinkCreateVO createShortLink(ShortLinkCreateDTO requestParam);
}
