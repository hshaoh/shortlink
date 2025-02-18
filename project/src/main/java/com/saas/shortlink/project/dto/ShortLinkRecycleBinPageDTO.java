package com.saas.shortlink.project.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saas.shortlink.project.dao.entity.ShortLink;
import lombok.Data;

import java.util.List;


/**
 * 回收站短链接分页请求参数
 */
@Data
public class ShortLinkRecycleBinPageDTO extends Page<ShortLink> {


    /**
     * 分组标识
     */
    private List<String> gidList;
}
