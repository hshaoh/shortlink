package com.saas.shortlink.admin.remote;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saas.shortlink.admin.common.convention.result.Result;
import com.saas.shortlink.admin.remote.dto.ShortLinkCreateDTO;
import com.saas.shortlink.admin.remote.vo.ShortLinkCreateVO;
import com.saas.shortlink.admin.remote.vo.ShortLinkGroupCountVO;
import com.saas.shortlink.admin.remote.vo.ShortLinkPageVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 短链接中台远程调用服务
 */
@FeignClient("short-link-project")
public interface ShortLinkActualRemoteService {

    /**
     * 创建短链接
     *
     * @param requestParam 创建短链接请求参数
     * @return 短链接创建响应
     */
    @PostMapping("/api/short-link/v1/create")
    Result<ShortLinkCreateVO> createShortLink(@RequestBody ShortLinkCreateDTO requestParam);


    /**
     * 分页查询短链接
     *
     * @param gid      分组标识
     * @param current  当前页
     * @param size     当前数据多少
     * @return 查询短链接响应
     */
    @GetMapping("/api/short-link/v1/page")
    Result<Page<ShortLinkPageVO>> pageShortLink(@RequestParam("gid") String gid,
                                                     @RequestParam("current") Long current,
                                                     @RequestParam("size") Long size);


    /**
     * 查询分组短链接总量
     *
     * @param requestParam 分组短链接总量请求参数
     * @return 查询分组短链接总量响应
     */
    @GetMapping("/api/short-link/v1/count")
    Result<List<ShortLinkGroupCountVO>> listGroupShortLinkCount(@RequestParam("requestParam") List<String> requestParam);
}
