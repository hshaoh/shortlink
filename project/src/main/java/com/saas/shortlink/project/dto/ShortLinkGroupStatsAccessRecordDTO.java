package com.saas.shortlink.project.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saas.shortlink.project.dao.entity.LinkAccessLogs;
import lombok.Data;



/**
 * 分组短链接监控访问记录请求参数
 */
@Data
public class ShortLinkGroupStatsAccessRecordDTO extends Page<LinkAccessLogs> {

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

}
