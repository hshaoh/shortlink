package com.saas.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saas.shortlink.project.common.convention.result.Result;
import com.saas.shortlink.project.common.convention.result.Results;
import com.saas.shortlink.project.dto.RecycleBinRecoverDTO;
import com.saas.shortlink.project.dto.RecycleBinRemoveDTO;
import com.saas.shortlink.project.dto.RecycleBinSaveDTO;
import com.saas.shortlink.project.dto.ShortLinkRecycleBinPageDTO;
import com.saas.shortlink.project.service.RecycleBinService;
import com.saas.shortlink.project.vo.ShortLinkPageVO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecycleBinController {

    private final RecycleBinService recycleBinService;

    /**
     * 保存回收站
     */
    @PostMapping("/api/short-link/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveDTO requestParam) {
        recycleBinService.saveRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 分页查询回收站短链接
     */
    @GetMapping("/api/short-link/v1/recycle-bin/page")
    public Result<IPage<ShortLinkPageVO>> pageShortLink(ShortLinkRecycleBinPageDTO requestParam) {
        return Results.success(recycleBinService.pageShortLink(requestParam));
    }


    /**
     * 恢复短链接
     */
    @PostMapping("/api/short-link/v1/recycle-bin/recover")
    public Result<Void> recoverRecycleBin(@RequestBody RecycleBinRecoverDTO requestParam) {
        recycleBinService.recoverRecycleBin(requestParam);
        return Results.success();
    }

    /**
     * 移除短链接
     */
    @PostMapping("/api/short-link/v1/recycle-bin/remove")
    public Result<Void> removeRecycleBin(@RequestBody RecycleBinRemoveDTO requestParam) {
        recycleBinService.removeRecycleBin(requestParam);
        return Results.success();
    }

}
