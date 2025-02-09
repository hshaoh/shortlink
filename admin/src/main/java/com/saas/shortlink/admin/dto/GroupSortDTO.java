package com.saas.shortlink.admin.dto;

import lombok.Data;

@Data
public class GroupSortDTO {
    /**
     * 分组ID
     */
    private String gid;

    /**
     * 排序
     */
    private Integer sortOrder;
}
