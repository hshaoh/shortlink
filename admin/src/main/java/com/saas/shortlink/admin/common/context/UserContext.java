package com.saas.shortlink.admin.common.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.saas.shortlink.admin.dto.UserInfoDTO;

import java.util.Optional;

/**
 * 用户上下文
 */
public final class UserContext {

    /**
     * <a href="https://github.com/alibaba/transmittable-thread-local" />
     */
    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置用户至上下文
     *
     * @param user 用户详情信息
     */
    public static void setUser(UserInfoDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取上下文中用户 ID
     *
     * @return 用户 ID
     */
    public static String getUserId() {
        /**
         *  Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUserId).orElse(null)的解析
         *  1. Optional.ofNullable()方法用于创建一个Optional对象（创建对象）
         *     如果userInfoDTO为null，则返回一个空的Optional对象；否则，返回一个包含userInfoDTO的Optional对象
         *  2. map()方法用于对Optional中的值进行转换（值转换）
         *     如果Optional对象不为空，则对其中的值应用UserInfoDTO::getUserId方法（即调用userInfoDTO.getUserId()），并将结果包装在一个新的Optional对象中
         *     如果Optional对象为空（即userInfoDTO为null），则map()方法不会执行任何操作，直接返回一个空的Optional对象
         *  3. orElse()方法用于获取Optional对象中的值。
         *     如果Optional对象不为空，则返回其中的值（返回值）
         *     如果Optional对象为空，则返回orElse()方法中指定的默认值（这里是null）。
         */


        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUserId).orElse(null);
    }

    /**
     * 获取上下文中用户名称
     *
     * @return 用户名称
     */
    public static String getUsername() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUsername).orElse(null);
    }

    /**
     * 获取上下文中用户真实姓名
     *
     * @return 用户真实姓名
     */
    public static String getRealName() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getRealName).orElse(null);
    }

    /**
     * 清理用户上下文
     */
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}