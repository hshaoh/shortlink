package com.saas.shortlink.project.controller;

import com.saas.shortlink.project.common.convention.result.Result;
import com.saas.shortlink.project.common.convention.result.Results;
import com.saas.shortlink.project.dto.ShortLinkCreateDTO;
import com.saas.shortlink.project.service.ShortLinkService;
import com.saas.shortlink.project.vo.ShortLinkCreateVO;
import lombok.RequiredArgsConstructor;
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

}