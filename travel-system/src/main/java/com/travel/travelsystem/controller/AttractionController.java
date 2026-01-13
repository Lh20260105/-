package com.travel.travelsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.entity.Attraction;
import com.travel.travelsystem.service.AttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 景点管理核心控制器
 * 已修复：删除 400 错误、图片后缀丢失问题、搜索逻辑
 */
@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    @Autowired
    private AttractionService attractionService;

    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 1. 获取所有景点列表
     */
    @GetMapping("/list")
    public Result<List<Attraction>> list() {
        // 按照 ID 倒序排列，新加的景点在最前面
        List<Attraction> list = attractionService.list(new QueryWrapper<Attraction>().orderByDesc("id"));
        return Result.success(list, "获取景点列表成功");
    }

    /**
     * 2. 搜索与筛选
     */
    @GetMapping("/search")
    public Result<List<Attraction>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {

        String searchName = (name != null) ? name.trim() : "";
        String searchCategory = (category != null) ? category.trim() : "";

        List<Attraction> list = attractionService.query()
                .like(!searchName.isEmpty(), "name", searchName)
                .eq(!searchCategory.isEmpty() && !"全部".equals(searchCategory), "category", searchCategory)
                .orderByDesc("id")
                .list();

        return Result.success(list, "查询成功");
    }

    /**
     * 3. 获取详情
     */
    @GetMapping("/{id}")
    public Result<Attraction> getDetail(@PathVariable Long id) {
        Attraction attraction = attractionService.getById(id);
        if (attraction != null) {
            return Result.success(attraction);
        } else {
            return Result.error("景点不存在");
        }
    }

    /**
     * 4. 景点图片上传 (修复后缀名丢失问题)
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error("请选择图片");

        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        // 提取后缀名，确保图片可正常访问
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 5) + suffix;

        try {
            file.transferTo(new File(uploadPath + fileName));
            String url = "http://localhost:8080/files/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            return Result.error("上传失败");
        }
    }

    /**
     * 5. 新增景点
     */
    @PostMapping("/add")
    public Result<Object> add(@RequestBody Attraction attraction) {
        boolean ok = attractionService.save(attraction);
        return ok ? Result.success(null, "添加成功") : Result.error("添加失败");
    }

    /**
     * 6. 修改景点
     */
    @PostMapping("/update")
    public Result<Object> update(@RequestBody Attraction attraction) {
        boolean ok = attractionService.updateById(attraction);
        return ok ? Result.success(null, "资料更新成功") : Result.error("更新失败");
    }

    /**
     * 7. 删除景点 (修复 400 错误)
     * 删除了 @RequestHeader("role")，因为前端暂时没有传这个 Header
     */
    @DeleteMapping("/{id}")
    public Result<Object> delete(@PathVariable Long id) {
        // 直接根据路径里的 ID 进行删除
        boolean removed = attractionService.removeById(id);
        if (removed) {
            return Result.success(null, "删除成功");
        } else {
            return Result.error("删除失败，该景点可能不存在");
        }
    }
}