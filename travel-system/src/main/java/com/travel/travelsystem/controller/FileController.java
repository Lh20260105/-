package com.travel.travelsystem.controller;

import com.travel.travelsystem.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    // 自动读取 application.yml 里的 file.upload-path 配置
    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        // 1. 判断文件是否为空
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 2. 确保文件夹存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 3. 获取原始文件名并提取后缀名（核心修复点：防止后缀丢失）
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 4. 生成唯一的文件名：时间戳 + 随机字符 + 原后缀
        // 这样做可以防止文件重名，且保证后缀永远存在
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + suffix;

        try {
            // 5. 保存文件到硬盘：D:/travel2/travel-system/files/xxx.jpg
            file.transferTo(new File(uploadPath + fileName));

            // 6. 返回给前端访问的 URL 地址
            // 注意：/files/** 是在 WebConfig 中配置的映射路径
            String url = "http://localhost:8080/files/" + fileName;

            System.out.println(">>> [文件上传成功] 访问路径: " + url);
            return Result.success(url);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件保存失败：" + e.getMessage());
        }
    }
}