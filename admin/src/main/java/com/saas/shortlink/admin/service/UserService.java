package com.saas.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.saas.shortlink.admin.dao.entity.User;
import com.saas.shortlink.admin.dto.UserLoginDTO;
import com.saas.shortlink.admin.dto.UserRegisterDTO;
import com.saas.shortlink.admin.dto.UserUpdateDTO;
import com.saas.shortlink.admin.vo.UserLoginVO;
import com.saas.shortlink.admin.vo.UserVO;

/**
 * 用户接口层
 */
public interface UserService extends IService<User> {

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

    /**
     * 根据用户名修改用户信息
     * @param userUpdateDTO 修改用户请求参数
     */
    void update(UserUpdateDTO userUpdateDTO);

    /**
     * 用户登录
     * @param userLoginDTO 用户登录请求参数
     * @return 用户登录返回参数 Token
     */
    UserLoginVO login(UserLoginDTO userLoginDTO);

    /**
     * 检查用户是否登录
     *
     * @param username 用户名
     * @param token    用户登录 Token
     * @return 用户是否登录标识
     */
    Boolean checkLogin(String username, String token);

    /**
     * 退出登录
     *
     * @param username 用户名
     * @param token    用户登录 Token
     */
    void logout(String username, String token);
}
