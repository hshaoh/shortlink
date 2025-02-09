package com.saas.shortlink.admin.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "shortlink.group")
@Data
public class GroupProperties {
    /**
     * 管理端分组相关配置
     */
    private Integer maxNum;
}
