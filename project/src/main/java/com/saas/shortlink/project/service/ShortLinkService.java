package com.saas.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.shortlink.project.dao.entity.ShortLink;
import com.saas.shortlink.project.dto.ShortLinkCreateDTO;
import com.saas.shortlink.project.dto.ShortLinkPageDTO;
import com.saas.shortlink.project.dto.ShortLinkUpdateDTO;
import com.saas.shortlink.project.vo.ShortLinkCreateVO;
import com.saas.shortlink.project.vo.ShortLinkGroupCountVO;
import com.saas.shortlink.project.vo.ShortLinkPageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLink> {

    /**
     * 创建短链接
     * @param requestParam 短链接创建请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateVO createShortLink(ShortLinkCreateDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageVO> pageShortLink(ShortLinkPageDTO requestParam);

    /**
     * 查询短链接分组内数量
     * @param requestParam 查询短链接分组内数量请求参数
     * @return 查询短链接分组内数量响应
     */
    List<ShortLinkGroupCountVO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 修改短链接
     * @param requestParam 修改短链接请求参数
     */
    void updateShortLink(ShortLinkUpdateDTO requestParam);


    /**
     * 短链接跳转
     *
     * @param shortUri 短链接后缀
     * @param request  HTTP 请求
     * @param response HTTP 响应
     */
    void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response);
}
