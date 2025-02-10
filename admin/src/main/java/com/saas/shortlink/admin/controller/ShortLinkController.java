package com.saas.shortlink.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saas.shortlink.admin.common.convention.result.Result;
import com.saas.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.saas.shortlink.admin.remote.dto.ShortLinkCreateDTO;
import com.saas.shortlink.admin.remote.dto.ShortLinkPageDTO;
import com.saas.shortlink.admin.remote.vo.ShortLinkCreateVO;
import com.saas.shortlink.admin.remote.vo.ShortLinkPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController(value = "shortLinkControllerByAdmin")
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateVO> createShortLink(@RequestBody ShortLinkCreateDTO requestParam) {
        return shortLinkActualRemoteService.createShortLink(requestParam);
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<Page<ShortLinkPageVO>> pageShortLink(ShortLinkPageDTO requestParam) {
        return shortLinkActualRemoteService.pageShortLink(requestParam.getGid(), requestParam.getCurrent(), requestParam.getSize());
    }
}
