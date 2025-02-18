package com.saas.shortlink.project.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saas.shortlink.project.dao.entity.LinkAccessLogs;
import lombok.Data;


/**
 * 短链接监控访问记录请求参数
 */
@Data
public class ShortLinkStatsAccessRecordDTO extends Page<LinkAccessLogs> {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 启用标识 0：启用 1：未启用
     */
    private Integer enableStatus;
}
