package com.saas.shortlink.project.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接访问网络监控响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortLinkStatsNetworkVO {

    /**
     * 统计
     */
    private Integer cnt;

    /**
     * 访问网络
     */
    private String network;

    /**
     * 占比
     */
    private Double ratio;
}