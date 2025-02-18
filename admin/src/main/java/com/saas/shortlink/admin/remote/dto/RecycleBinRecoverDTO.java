package com.saas.shortlink.admin.remote.dto;

import lombok.Data;

/**
 * 回收站恢复功能
 */
@Data
public class RecycleBinRecoverDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 全部短链接
     */
    private String fullShortUrl;
}
