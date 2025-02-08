package com.saas.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.saas.shortlink.admin.common.constant.RedisCacheConstant;
import com.saas.shortlink.admin.common.convention.exception.ClientException;
import com.saas.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.saas.shortlink.admin.dao.entity.User;
import com.saas.shortlink.admin.dao.mapper.UserMapper;
import com.saas.shortlink.admin.dto.UserLoginDTO;
import com.saas.shortlink.admin.dto.UserRegisterDTO;
import com.saas.shortlink.admin.dto.UserUpdateDTO;
import com.saas.shortlink.admin.vo.UserLoginVO;
import com.saas.shortlink.admin.vo.UserVO;
import com.saas.shortlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.redisson.api.RBloomFilter;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.saas.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public UserVO getUserByUsername(String username) {
        // Wrappers 是 MyBatis-Plus 提供的一个工具类，用于创建各种类型的 Wrapper
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, username);
        User userDO = baseMapper.selectOne(queryWrapper);
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
        // 判断用户名是否已经存在，已存在抛出异常
        if (!availableUsername(userRegisterDTO.getUsername())){
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }

        // 获得用户名的分布式锁
        RLock lock = redissonClient.getLock(RedisCacheConstant.LOCK_USER_REGISTER_KEY + userRegisterDTO.getUsername());

        if (!lock.tryLock()) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }

        try {
            // 如果不存在，将用户数据加入数据库中
            int inserted = baseMapper.insert(BeanUtil.toBean(userRegisterDTO, User.class));
            if (inserted < 1) {
                throw new ClientException(UserErrorCodeEnum.USER_SAVE_ERROR);
            }
            // 将用户名加入redis中
            userRegisterCachePenetrationBloomFilter.add(userRegisterDTO.getUsername());
        } catch (DuplicateKeyException ex){
            throw new ClientException(UserErrorCodeEnum.USER_EXIST);
        } finally {
            lock.unlock();
        }

    }

    @Override
    public void update(UserUpdateDTO userUpdateDTO) {
        // TODO 验证当前用户是否为登录用户
        LambdaUpdateWrapper<User> updateWrapper = Wrappers.lambdaUpdate(User.class)
                .eq(User::getUsername, userUpdateDTO.getUsername());
        User userDO = BeanUtil.toBean(userUpdateDTO, User.class);
        baseMapper.update(userDO, updateWrapper);
    }

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        LambdaQueryWrapper<User> queryWrapper = Wrappers.lambdaQuery(User.class)
                .eq(User::getUsername, userLoginDTO.getUsername())
                .eq(User::getPassword, userLoginDTO.getPassword())
                .eq(User::getDelFlag, 0);
        User userDO = baseMapper.selectOne(queryWrapper);
        // 用户名不存在
        if (userDO == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        // 检查用户是否已经登录过
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash().entries(USER_LOGIN_KEY + userLoginDTO.getUsername());
        // 如果用户已经登录
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            // 更新该用户的登录状态的有效期为 30 分钟
            stringRedisTemplate.expire(USER_LOGIN_KEY + userLoginDTO.getUsername(), 30L, TimeUnit.MINUTES);
            // 从 hasLoginMap 中提取第一个 Key（即 token）
            // 如果 hasLoginMap 为空或没有 Key，抛出 ClientException 异常
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException(UserErrorCodeEnum.USER_LOGIN_ERROR));
            return new UserLoginVO(token);
        }
        /**
         * Hash
         * Key：login_用户名
         * Value：
         *  Key：token标识
         *  Val：JSON 字符串（用户信息）
         */
        // 用户首次登录
        String uuid = UUID.randomUUID().toString();
        // 将用户信息（UserDO）序列化为JSON字符串，存入redis中
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + userLoginDTO.getUsername(), uuid, JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + userLoginDTO.getUsername(), 30L, TimeUnit.MINUTES);
        return new UserLoginVO(uuid);
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        // 删除用户在redis中的缓存记录
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        // 用户token为NULL或者未登录
        throw new ClientException(UserErrorCodeEnum.USER_TOKEN_NULL);
    }


}
