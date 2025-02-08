package com.saas.shortlink.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户登录接口返回响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginVO {

    /**
     * jwt令牌
     */
    private String token;

}
