package com.saas.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.shortlink.admin.common.convention.exception.ClientException;
import com.saas.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.saas.shortlink.admin.dao.entity.UserDO;
import com.saas.shortlink.admin.dao.mapper.UserMapper;
import com.saas.shortlink.admin.dto.UserRegisterDTO;
import com.saas.shortlink.admin.vo.UserVO;
import com.saas.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.redisson.api.RBloomFilter;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Override
    public UserVO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserVO result = new UserVO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    @Override
    public Boolean availableUsername(String username) {
        // 如果布隆过滤器存在username, 说明不可用
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        // 判断用户名是否已经存在，已存在抛出错误
        if (!availableUsername(userRegisterDTO.getUsername())){
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        // 如果不存在，将用户数据加入数据库中
        int inserted = baseMapper.insert(BeanUtil.toBean(userRegisterDTO, UserDO.class));
        if (inserted < 1) {
            throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
        }
        // 将用户名加入redis中
        userRegisterCachePenetrationBloomFilter.add(userRegisterDTO.getUsername());
    }


}
