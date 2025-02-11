package com.saas.shortlink.admin;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@EnableDiscoveryClient
@EnableFeignClients("com.saas.shortlink.admin.remote")
@MapperScan("com.saas.shortlink.admin.dao.mapper")
public class ShortLinkAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortLinkAdminApplication.class, args);
    }
}
