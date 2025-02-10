package com.saas.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.shortlink.admin.common.constant.RedisCacheConstant;
import com.saas.shortlink.admin.common.context.UserContext;
import com.saas.shortlink.admin.common.convention.exception.ClientException;
import com.saas.shortlink.admin.common.convention.result.Result;
import com.saas.shortlink.admin.common.properties.GroupProperties;
import com.saas.shortlink.admin.dao.entity.Group;
import com.saas.shortlink.admin.dao.entity.User;
import com.saas.shortlink.admin.dao.mapper.GroupMapper;
import com.saas.shortlink.admin.dto.GroupSortDTO;
import com.saas.shortlink.admin.dto.GroupUpdateDTO;
import com.saas.shortlink.admin.remote.ShortLinkActualRemoteService;
import com.saas.shortlink.admin.remote.vo.ShortLinkGroupCountVO;
import com.saas.shortlink.admin.service.GroupService;
import com.saas.shortlink.admin.util.RandomGenerator;
import com.saas.shortlink.admin.vo.GroupVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements GroupService {

    private final RedissonClient redissonClient;

    private final GroupProperties groupProperties;

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @Override
    public void saveGroup(String groupName) {
        saveGroup(UserContext.getUsername(), groupName);
    }

    @Override
    public void saveGroup(String username, String groupName) {
        // 分布式锁：这是为了防止同一用户在不同线程（可能同一用户在多个设备）同时新增分组，而超出最大分组数
        RLock lock = redissonClient.getLock(String.format(RedisCacheConstant.LOCK_GROUP_CREATE_KEY, username));
        lock.lock();
        try {
            LambdaQueryWrapper<Group> queryWrapper = Wrappers.lambdaQuery(Group.class)
                    .eq(Group::getUsername, username)
                    .eq(Group::getDelFlag, 0);
            List<Group> groupList = baseMapper.selectList(queryWrapper);
            if (CollUtil.isNotEmpty(groupList) && groupList.size() == groupProperties.getMaxNum()) {
                throw new ClientException(String.format("已超出最大分组数：%d", groupProperties.getMaxNum()));
            }
            String gid;
            do {
                // 随机生成 6 位的 gid
                gid = RandomGenerator.generateRandom();
            }while (!hasGid(username, gid));
            Group group = Group.builder()
                    .gid(gid)
                    .sortOrder(0)
                    .username(username)
                    .name(groupName)
                    .build();
            baseMapper.insert(group);
        }finally {
            lock.unlock();
        }

    }
    private boolean hasGid(String username, String gid) {
        // gid 和 username 是联合索引，存在多个相同的 gid 对应不同的username的情况
        LambdaQueryWrapper<Group> queryWrapper = Wrappers.lambdaQuery(Group.class)
                .eq(Group::getGid, gid)
                .eq(Group::getUsername, username);
        Group groupFlag = baseMapper.selectOne(queryWrapper);
        return groupFlag == null;
    }

    @Override
    public List<GroupVO> listGroup() {
        LambdaQueryWrapper<Group> queryWrapper = Wrappers.lambdaQuery(Group.class)
                .eq(Group::getDelFlag, 0)
                .eq(Group::getUsername, UserContext.getUsername())
                .orderByDesc(Group::getSortOrder, Group::getUpdateTime);
        List<Group> groupList = baseMapper.selectList(queryWrapper);

        // 解析出gid集合
        List<String> gids = groupList.stream().map(Group::getGid).toList();
        // 远程调用project返回分组结果
        Result<List<ShortLinkGroupCountVO>> listResult = shortLinkActualRemoteService.listGroupShortLinkCount(gids);
        // 取出 Result 中的 Data 数据
        List<ShortLinkGroupCountVO> shortLinkGroupCountVOList = listResult.getData();
        // 构造Map<gid:countGroup>对应关系
        Map<String, Integer> CountGroupMap = shortLinkGroupCountVOList.stream().collect(Collectors.toMap(ShortLinkGroupCountVO::getGid, // key
                ShortLinkGroupCountVO::getShortLinkCount));//value

        return groupList.stream().map(group -> GroupVO.builder()
                        .name(group.getName())
                        .gid(group.getGid())
                        .sortOrder(group.getSortOrder())
                        .shortLinkCount(CountGroupMap.get(group.getGid()))
                        .build()).toList();
//        return BeanUtil.copyToList(groupList, GroupVO.class);
    }

    @Override
    public void updateGroup(GroupUpdateDTO groupUpdateDTO) {
        LambdaUpdateWrapper<Group> updateWrapper = Wrappers.lambdaUpdate(Group.class)
                .eq(Group::getUsername, UserContext.getUsername())
                .eq(Group::getGid, groupUpdateDTO.getGid())
                .eq(Group::getDelFlag, 0);
        Group group = new Group();
        group.setName(groupUpdateDTO.getName());
        baseMapper.update(group, updateWrapper);
    }

    @Override
    public void deleteGroup(String gid) {
        LambdaUpdateWrapper<Group> updateWrapper = Wrappers.lambdaUpdate(Group.class)
                .eq(Group::getUsername, UserContext.getUsername())
                .eq(Group::getGid, gid)
                .eq(Group::getDelFlag, 0);
        Group group = new Group();
        group.setDelFlag(1);
        baseMapper.update(group, updateWrapper);
    }

    @Override
    public void sortGroup(List<GroupSortDTO> groupSortDTOList) {
        // 用户在前排拖动了分组的位置，也就是重新排序，那么前端就应该传一个顺序过来，也就是这个接口进行修改sortOrder字段
        groupSortDTOList.forEach(each->{
            LambdaUpdateWrapper<Group> updateWrapper = Wrappers.lambdaUpdate(Group.class)
                    .set(Group::getSortOrder, each.getSortOrder())
                    .eq(Group::getGid, each.getGid())
                    .eq(Group::getUsername, UserContext.getUsername())
                    .eq(Group::getDelFlag, 0);
            baseMapper.update(null, updateWrapper);
        });
    }
}
