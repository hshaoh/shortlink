package com.saas.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.shortlink.admin.dao.entity.Group;
import com.saas.shortlink.admin.dto.GroupSortDTO;
import com.saas.shortlink.admin.dto.GroupUpdateDTO;
import com.saas.shortlink.admin.vo.GroupVO;

import java.util.List;

/**
 * 短链接分组接口层
 */
public interface GroupService extends IService<Group> {

    /**
     * 新增短链接分组
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);


    /**
     * 新增短链接分组
     *
     * @param username  用户名
     * @param groupName 短链接分组名
     */
    void saveGroup(String username, String groupName);

    /**
     * 查询用户短链接分组集合
     *
     * @return 用户短链接分组集合
     */
    List<GroupVO> listGroup();

    /**
     * 修改短链接分组
     * @param groupUpdateDTO 修改链接分组参数
     */
    void updateGroup(GroupUpdateDTO groupUpdateDTO);

    /**
     * 删除短链接分组
     * @param gid 短链接分组标识
     */
    void deleteGroup(String gid);

    /**
     * 短链接分组排序
     * @param groupSortDTOList 短链接分组排序参数
     */
    void sortGroup(List<GroupSortDTO> groupSortDTOList);
}
