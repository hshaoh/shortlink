package com.saas.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 访问网络统计访问实体
 */
@Data
@TableName("t_link_network_stats")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkNetworkStats extends BaseDO {

    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 日期
     */
    private LocalDateTime date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 访问网络
     */
    private String network;
}
