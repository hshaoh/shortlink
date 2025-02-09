package com.saas.shortlink.admin.interceptor;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.system.UserInfo;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saas.shortlink.admin.common.constant.JwtClaimsConstant;
import com.saas.shortlink.admin.common.context.UserContext;
import com.saas.shortlink.admin.common.convention.exception.ClientException;
import com.saas.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.saas.shortlink.admin.common.properties.JwtProperties;
import com.saas.shortlink.admin.dao.entity.User;
import com.saas.shortlink.admin.dto.UserInfoDTO;
import com.saas.shortlink.admin.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.saas.shortlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        //1、从请求头中获取令牌
        String token = request.getHeader(jwtProperties.getAdminTokenName());

        //2、校验令牌
        try {
            // 2.1 使用JwtUtil的parseJWT方法解析token，并获取用户名
            log.info("jwt校验: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getAdminSecretKey(), token);
            String username = String.valueOf(claims.get(JwtClaimsConstant.USERNAME).toString());
            log.info("当前用户姓名: {}", username);

            // 2.2 从Redis中获取用户信息，如果用户信息为null，返回401
            String userJson = (String) stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token);
            if (userJson == null){
                setErrorResponse(response, 401, "未授权：用户信息无效或已过期");
                return false;
            }
            // 反序列化得到用户类
            UserInfoDTO userInfoDTO = JSON.parseObject(userJson, UserInfoDTO.class);
            // 保存用户信息到ThreadLocal中
            UserContext.setUser(userInfoDTO);

            // 更新该用户的登录状态的有效期为 30 分钟
            stringRedisTemplate.expire(USER_LOGIN_KEY + userInfoDTO.getUsername(), 30L, TimeUnit.MINUTES);

            //3、校验通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            setErrorResponse(response, 401, "未授权：无效的令牌");
            return false;
        }
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清除用户信息
        UserContext.removeUser();
    }


    /**
     * 设置错误响应
     *
     * @param response HttpServletResponse
     * @param status   状态码
     * @param message  错误信息
     */
    private void setErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");

        // 构造错误响应体
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", status);
        errorResponse.put("message", message);

        // 将错误响应体写入 response
        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}
