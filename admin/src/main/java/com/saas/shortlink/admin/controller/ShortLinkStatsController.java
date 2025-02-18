package com.saas.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saas.shortlink.admin.common.convention.result.Result;
import com.saas.shortlink.admin.common.convention.result.Results;
import com.saas.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.saas.shortlink.admin.remote.dto.ShortLinkGroupStatsAccessRecordDTO;
import com.saas.shortlink.admin.remote.dto.ShortLinkGroupStatsDTO;
import com.saas.shortlink.admin.remote.dto.ShortLinkStatsAccessRecordDTO;
import com.saas.shortlink.admin.remote.dto.ShortLinkStatsDTO;
import com.saas.shortlink.admin.remote.vo.ShortLinkStatsAccessRecordVO;
import com.saas.shortlink.admin.remote.vo.ShortLinkStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsVO> shortLinkStats(ShortLinkStatsDTO requestParam) {
        return shortLinkActualRemoteService.oneShortLinkStats(
                requestParam.getFullShortUrl(),
                requestParam.getGid(),
                requestParam.getEnableStatus(),
                requestParam.getStartDate(),
                requestParam.getEndDate()
        );
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/group")
    public Result<ShortLinkStatsVO> groupShortLinkStats(ShortLinkGroupStatsDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStats(
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate()
        );
    }


    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<Page<ShortLinkStatsAccessRecordVO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordDTO requestParam) {
        return shortLinkActualRemoteService.shortLinkStatsAccessRecord(
                requestParam.getFullShortUrl(),
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                requestParam.getEnableStatus(),
                requestParam.getCurrent(),
                requestParam.getSize()
        );
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record/group")
    public Result<Page<ShortLinkStatsAccessRecordVO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordDTO requestParam) {
        return shortLinkActualRemoteService.groupShortLinkStatsAccessRecord(
                requestParam.getGid(),
                requestParam.getStartDate(),
                requestParam.getEndDate(),
                requestParam.getCurrent(),
                requestParam.getSize()
        );
    }
}
