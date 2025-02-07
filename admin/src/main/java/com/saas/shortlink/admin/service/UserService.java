package com.saas.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.shortlink.admin.dao.entity.UserDO;
import com.saas.shortlink.admin.dto.UserRegisterDTO;
import com.saas.shortlink.admin.vo.UserVO;

/**
 * 用户接口层
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    UserVO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     * @param username 用户名
     * @return 用户名存在返回 True, 不存在返回 False
     */
    Boolean availableUsername(String username);

    /**
     * 注册用户
     * @param userRegisterDTO 注册用户请求参数
     */
    void register(UserRegisterDTO userRegisterDTO);

}
