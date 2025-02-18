package com.saas.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saas.shortlink.project.dto.ShortLinkGroupStatsDTO;
import com.saas.shortlink.project.dto.ShortLinkStatsAccessRecordDTO;
import com.saas.shortlink.project.dto.ShortLinkStatsDTO;
import com.saas.shortlink.project.dto.ShortLinkGroupStatsAccessRecordDTO;
import com.saas.shortlink.project.vo.ShortLinkStatsAccessRecordVO;
import com.saas.shortlink.project.vo.ShortLinkStatsVO;

/**
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     *
     * @param requestParam 获取短链接监控数据入参
     * @return 短链接监控数据
     */
    ShortLinkStatsVO oneShortLinkStats(ShortLinkStatsDTO requestParam);

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     * @param requestParam 获取短链接监控访问记录数据入参
     * @return 访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordVO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordDTO requestParam);


    /**
     * 获取分组短链接监控数据
     * @param requestParam 获取分组短链接监控数据入参
     * @return 分组短链接监控数据
     */
    ShortLinkStatsVO groupShortLinkStats(ShortLinkGroupStatsDTO requestParam);

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     * @param requestParam 获取分组短链接监控访问记录数据入参
     * @return 分组访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordVO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordDTO requestParam);
}
