package com.saas.shortlink.admin.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GroupVO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组排序
     */
    private Integer sortOrder;

    /**
     * 分组下短链接数量
     */
    private Integer shortLinkCount;
}
