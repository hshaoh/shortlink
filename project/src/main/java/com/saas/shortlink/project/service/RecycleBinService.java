package com.saas.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.shortlink.project.dao.entity.ShortLink;
import com.saas.shortlink.project.dto.RecycleBinRecoverDTO;
import com.saas.shortlink.project.dto.RecycleBinRemoveDTO;
import com.saas.shortlink.project.dto.RecycleBinSaveDTO;
import com.saas.shortlink.project.dto.ShortLinkRecycleBinPageDTO;
import com.saas.shortlink.project.vo.ShortLinkPageVO;

/**
 * 回收站管理接口层
 */
public interface RecycleBinService extends IService<ShortLink> {

    /**
     * 保存回收站
     * @param requestParam 请求参数
     */
    void saveRecycleBin(RecycleBinSaveDTO requestParam);

    /**
     * 分页查询短链接
     * @param requestParam 分页查询短链接请求参数xu
     * @return 短链接分页返回结果
     */
    IPage<ShortLinkPageVO> pageShortLink(ShortLinkRecycleBinPageDTO requestParam);

    /**
     * 从回收站恢复短链接
     * @param requestParam 恢复短链接请求参数
     */
    void recoverRecycleBin(RecycleBinRecoverDTO requestParam);

    /**
     * 从回收站移除短链接
     * @param requestParam 移除短链接请求参数
     */
    void removeRecycleBin(RecycleBinRemoveDTO requestParam);
}
