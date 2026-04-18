package com.travel.travelsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;
import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. 配置跨域支持（新增部分）
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径
                .allowedOriginPatterns("*") // 允许所有来源（开发环境最省心）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许所有请求方式
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许携带 Cookie/凭证
                .maxAge(3600); // 预检请求有效期
    }

    // 2. 原有的静态资源映射逻辑
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String userDir = System.getProperty("user.dir");
        Path uploadDir = Paths.get(userDir, "files");
        String path = uploadDir.toFile().getAbsolutePath() + File.separator;

        File dir = uploadDir.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + path);

        System.out.println("-----------------------------------------");
        System.out.println(">>> [系统提示] 跨域配置与静态资源映射已就绪");
        System.out.println(">>> 允许前端跨域访问所有接口");
        System.out.println(">>> 资源路径: " + path);
        System.out.println("-----------------------------------------");
    }
}