package com.saas.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.shortlink.project.dao.entity.ShortLink;
import com.saas.shortlink.project.dto.ShortLinkCreateDTO;
import com.saas.shortlink.project.dto.ShortLinkPageDTO;
import com.saas.shortlink.project.vo.ShortLinkCreateVO;
import com.saas.shortlink.project.vo.ShortLinkPageVO;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLink> {

    /**
     * 创建短链接
     * @param shortLinkCreateDTO 短链接创建请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateVO createShortLink(ShortLinkCreateDTO shortLinkCreateDTO);

    /**
     * 分页查询短链接
     * @param shortLinkPageDTO 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageVO> pageShortLink(ShortLinkPageDTO shortLinkPageDTO);
}
