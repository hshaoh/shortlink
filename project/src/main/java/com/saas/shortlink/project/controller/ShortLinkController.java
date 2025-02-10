package com.saas.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.saas.shortlink.project.common.convention.result.Result;
import com.saas.shortlink.project.common.convention.result.Results;
import com.saas.shortlink.project.dto.ShortLinkCreateDTO;
import com.saas.shortlink.project.dto.ShortLinkPageDTO;
import com.saas.shortlink.project.service.ShortLinkService;
import com.saas.shortlink.project.vo.ShortLinkCreateVO;
import com.saas.shortlink.project.vo.ShortLinkPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateVO> createShortLink(@RequestBody ShortLinkCreateDTO shortLinkCreateDTO) {
        return Results.success(shortLinkService.createShortLink(shortLinkCreateDTO));
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageVO>> pageShortLink(ShortLinkPageDTO shortLinkPageDTO) {
        return Results.success(shortLinkService.pageShortLink(shortLinkPageDTO));
    }

}