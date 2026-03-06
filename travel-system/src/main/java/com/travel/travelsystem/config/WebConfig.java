package com.travel.travelsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 使用项目根目录下的 files 文件夹
        String userDir = System.getProperty("user.dir");
        Path uploadDir = Paths.get(userDir, "files");
        String path = uploadDir.toFile().getAbsolutePath() + File.separator;

        // 确保文件夹存在
        File dir = uploadDir.toFile();
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println(">>> [系统提示] 已自动创建图片存储目录: " + path);
            }
        }

        /**
         * 核心映射逻辑
         * 当浏览器访问：http://localhost:8080/files/xxx.jpg
         * 后端会去：项目根目录/files 文件夹寻找文件
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
