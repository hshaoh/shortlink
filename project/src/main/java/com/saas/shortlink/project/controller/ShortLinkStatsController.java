package com.saas.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saas.shortlink.project.common.convention.result.Result;
import com.saas.shortlink.project.common.convention.result.Results;
import com.saas.shortlink.project.dto.ShortLinkGroupStatsDTO;
import com.saas.shortlink.project.dto.ShortLinkStatsAccessRecordDTO;
import com.saas.shortlink.project.dto.ShortLinkStatsDTO;
import com.saas.shortlink.project.service.ShortLinkStatsService;
import com.saas.shortlink.project.dto.ShortLinkGroupStatsAccessRecordDTO;
import com.saas.shortlink.project.vo.ShortLinkStatsAccessRecordVO;
import com.saas.shortlink.project.vo.ShortLinkStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    private final ShortLinkStatsService shortLinkStatsService;

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats")
    public Result<ShortLinkStatsVO> shortLinkStats(ShortLinkStatsDTO requestParam) {
        return Results.success(shortLinkStatsService.oneShortLinkStats(requestParam));
    }

    /**
     * 访问分组短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/v1/stats/group")
    public Result<ShortLinkStatsVO> groupShortLinkStats(ShortLinkGroupStatsDTO requestParam) {
        return Results.success(shortLinkStatsService.groupShortLinkStats(requestParam));
    }


    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordVO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordDTO requestParam) {
        return Results.success(shortLinkStatsService.shortLinkStatsAccessRecord(requestParam));
    }

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/v1/stats/access-record/group")
    public Result<IPage<ShortLinkStatsAccessRecordVO>> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordDTO requestParam) {
        return Results.success(shortLinkStatsService.groupShortLinkStatsAccessRecord(requestParam));
    }
}
