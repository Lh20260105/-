package com.travel.travelsystem.controller;

import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.entity.Attraction;
import com.travel.travelsystem.service.AttractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // 用于读取配置文件路径
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 用于处理文件上传

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 景点管理核心控制器
 * 包含：基础CRUD、模糊搜索、分类筛选、图片上传
 */
@RestController
@RequestMapping("/api/attractions")
public class AttractionController {

    @Autowired
    private AttractionService attractionService;

    // 从 application.yml 中读取图片保存路径
    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 1. 获取所有景点列表 (供管理员后台表格使用)
     */
    @GetMapping("/list")
    public Result<List<Attraction>> list() {
        List<Attraction> list = attractionService.list();
        return Result.success(list, "获取景点列表成功");
    }

    /**
     * 2. 首页搜索与分类筛选 (修复了搜索不到东西的问题)
     * 逻辑：自动去除空格，支持名称模糊查询和分类精确匹配
     */
    @GetMapping("/search")
    public Result<List<Attraction>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {

        // 处理搜索词：去除前后空格，防止误搜
        String searchName = (name != null) ? name.trim() : "";
        String searchCategory = (category != null) ? category.trim() : "";

        List<Attraction> list = attractionService.query()
                // 如果搜索名不为空，则执行 like 模糊查询
                .like(!searchName.isEmpty(), "name", searchName)
                // 如果分类不为空且不为"全部"，则执行 eq 精确查询
                .eq(!searchCategory.isEmpty() && !"全部".equals(searchCategory), "category", searchCategory)
                .list();

        return Result.success(list, "查询成功");
    }

    /**
     * 3. 获取景点详情 (供详情页 AttractionDetail.vue 使用)
     */
    @GetMapping("/{id}")
    public Result<Attraction> getDetail(@PathVariable Long id) {
        Attraction attraction = attractionService.getById(id);
        if (attraction != null) {
            return Result.success(attraction, "获取详情成功");
        } else {
            return Result.error("景点不存在");
        }
    }

    /**
     * 4. 图片上传接口 (管理端新增地点时使用)
     * 逻辑：保存到本地磁盘，并返回浏览器可访问的虚拟路径
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }
        try {
            // 确保本地文件夹存在
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名，防止重名
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // 执行保存
            file.transferTo(new File(uploadPath + fileName));

            // 返回对应的网络访问路径 (需配合 WebConfig 映射)
            String url = "http://localhost:8080/files/" + fileName;
            return Result.success(url, "图片上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败，请检查路径权限");
        }
    }

    /**
     * 5. 新增景点 (管理员功能)
     */
    @PostMapping("/add")
    public Result<Object> add(@RequestBody Attraction attraction) {
        boolean ok = attractionService.save(attraction);
        return ok ? Result.success(null, "添加成功") : Result.error("添加失败");
    }

    /**
     * 6. 修改景点 (管理员功能)
     */
    @PostMapping("/save")
    public Result<Object> save(@RequestBody Attraction attraction) {
        boolean saved = attractionService.saveOrUpdate(attraction);
        return saved ? Result.success(null, "资料更新成功") : Result.error("保存失败");
    }

    /**
     * 7. 删除景点 (管理员功能)
     */
    @DeleteMapping("/delete/{id}")
    public Result<Object> delete(@PathVariable Long id, @RequestHeader("role") String role) {
        // 虽然目前你可能还没做复杂的 Token 校验，但可以简单判断一下
        if (!"ADMIN".equals(role)) {
            return Result.error("权限不足，只有管理员可以删除景点");
        }
        boolean removed = attractionService.removeById(id);
        return removed ? Result.success(null, "删除成功") : Result.error("删除失败");
    }
}