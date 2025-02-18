package com.saas.shortlink.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 回收站保存功能
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleBinSaveDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 全部短链接
     */
    private String fullShortUrl;

}
