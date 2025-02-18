package com.saas.shortlink.admin.remote.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *  短链接分页请求参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkPageDTO extends Page {

    /**
     * 分组标识
     */
    private String gid;
}
