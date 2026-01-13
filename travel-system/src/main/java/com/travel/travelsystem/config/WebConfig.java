package com.travel.travelsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 【核心修改】：直接读取 application.yml 里的 file.upload-path 配置
    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 确保路径以斜杠结尾，防止拼接错误
        String path = uploadPath;
        if (!path.endsWith("/") && !path.endsWith("\\")) {
            path += File.separator;
        }

        // 2. 检查文件夹是否存在，不存在则创建
        File uploadDir = new File(path);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) System.out.println(">>> [系统提示] 已自动创建图片存储目录: " + path);
        }

        /**
         * 3. 核心映射逻辑
         * 当浏览器访问：http://localhost:8080/files/xxx.jpg
         * 后端会去：uploadPath 对应的硬盘位置寻找文件
         */
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + path);

        // 控制台打印调试信息
        System.out.println("-----------------------------------------");
        System.out.println(">>> [静态资源映射修复成功]");
        System.out.println(">>> 访问 URL: http://localhost:8080/files/文件名.jpg");
        System.out.println(">>> 硬盘实际路径: " + path);
        System.out.println("-----------------------------------------");
    }
}