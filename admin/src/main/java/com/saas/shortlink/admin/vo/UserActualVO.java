package com.saas.shortlink.admin.vo;

import lombok.Data;

/**
 * 用户返回参数实体(无脱敏)
 */
@Data
public class UserActualVO {

    /**
     * id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}
