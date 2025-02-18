package com.saas.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 短链接不存在跳转控制器
 */
@Controller
public class ShortLinkNotFoundController {

    /**
     * 短链接不存在跳转页面
     * 当用户方位 /page/notfound路径时，Spring会调用notfound()方法
     * 方法返回视图名称"notfound", Spring会渲染对应的视图文件并返回给用户
     */
    @RequestMapping("/page/notfound")
    public String notfound() {
        return "notfound";
    }
}