package com.saas.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.saas.shortlink.admin.common.context.UserContext;
import com.saas.shortlink.admin.common.convention.exception.ServiceException;
import com.saas.shortlink.admin.common.convention.result.Result;
import com.saas.shortlink.admin.dao.entity.Group;
import com.saas.shortlink.admin.dao.mapper.GroupMapper;
import com.saas.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.saas.shortlink.admin.remote.dto.ShortLinkRecycleBinPageDTO;
import com.saas.shortlink.admin.remote.vo.ShortLinkPageVO;
import com.saas.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * URL 回收站接口实现层
 */
@Service(value = "recycleBinServiceImplByAdmin")
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;
    private final GroupMapper groupMapper;
    @Override
    public Result<Page<ShortLinkPageVO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageDTO requestParam) {
        // 查询用户的所有分组
        LambdaQueryWrapper<Group> queryWrapper = Wrappers.lambdaQuery(Group.class)
                .eq(Group::getUsername, UserContext.getUsername())
                .eq(Group::getDelFlag, 0);
        List<Group> groupList = groupMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(groupList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupList.stream().map(Group::getGid).toList());
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam.getGidList(), requestParam.getCurrent(), requestParam.getSize());
    }
}
