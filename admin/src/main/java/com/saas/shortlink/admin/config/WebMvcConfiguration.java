package com.saas.shortlink.admin.config;

import com.saas.shortlink.admin.interceptor.JwtTokenAdminInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;


/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    private final JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/api/short-link/admin/**") // 添加拦截路径
                .excludePathPatterns("/api/short-link/admin/v1/user/login") // 排除拦截路径
                .excludePathPatterns("/api/short-link/admin/v1/user/register")
                .excludePathPatterns("/api/short-link/admin/v1/user/has-username");
    }


}
