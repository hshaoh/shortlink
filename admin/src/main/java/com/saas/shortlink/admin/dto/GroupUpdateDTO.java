package com.saas.shortlink.admin.dto;

import lombok.Data;

@Data
public class GroupUpdateDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名
     */
    private String name;
}
