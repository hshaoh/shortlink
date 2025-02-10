package com.saas.shortlink.project.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saas.shortlink.project.dao.entity.ShortLink;
import lombok.Data;

/**
 *  短链接分页请求参数
 */
@Data
public class ShortLinkPageDTO extends Page<ShortLink> {

    /**
     * 分组标识
     */
    private String gid;
}
