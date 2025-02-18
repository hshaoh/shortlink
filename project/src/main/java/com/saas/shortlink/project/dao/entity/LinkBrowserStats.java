package com.saas.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.saas.shortlink.project.common.database.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("t_link_browser_stats")
@NoArgsConstructor
@AllArgsConstructor
public class LinkBrowserStats extends BaseDO {

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
     * 浏览器
     */
    private String browser;
}
