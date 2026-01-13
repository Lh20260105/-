package com.travel.travelsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.entity.ForumPost;
import com.travel.travelsystem.entity.User;
import com.travel.travelsystem.service.ForumPostService;
import com.travel.travelsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private UserService userService;

    /**
     * 【管理端/前台通用】分页获取帖子列表（支持按标题搜索）
     * 访问地址：GET /api/forum/page?current=1&size=10&title=关键词
     */
    @GetMapping("/page")
    public Result<Page<Map<String, Object>>> getPostPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title) {

        // 1. 构建查询条件：模糊搜索标题并按时间倒序
        QueryWrapper<ForumPost> queryWrapper = new QueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            queryWrapper.like("title", title); // WHERE title LIKE '%title%'
        }
        queryWrapper.orderByDesc("create_time");

        // 2. 执行分页查询
        Page<ForumPost> page = forumPostService.page(new Page<>(current, size), queryWrapper);

        // 3. 将分页结果中的记录转换为包含作者信息的 Map 格式
        Page<Map<String, Object>> resultMap = new Page<>(current, size, page.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();

        for (ForumPost post : page.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("imageUrl", post.getImageUrl()); // 封面预览
            map.put("createTime", post.getCreateTime());

            // 补全作者信息
            User author = userService.getById(post.getUserId());
            if (author != null) {
                String name = (author.getNickname() != null && !author.getNickname().isEmpty())
                        ? author.getNickname() : author.getUsername();
                map.put("author", name);
                map.put("avatar", author.getAvatarUrl());
            } else {
                map.put("author", "已注销用户");
            }
            records.add(map);
        }

        resultMap.setRecords(records);
        return Result.success(resultMap, "获取分页成功");
    }

    /**
     * 前台获取所有帖子列表（不分页，你原本的方法）
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getPosts() {
        List<ForumPost> posts = forumPostService.query().orderByDesc("create_time").list();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ForumPost post : posts) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent());
            map.put("imageUrl", post.getImageUrl()); // 【修复】：一定要加上这个，否则广场不显图
            map.put("createTime", post.getCreateTime());

            User author = userService.getById(post.getUserId());
            if (author != null) {
                String nameToDisplay = (author.getNickname() != null && !author.getNickname().isEmpty())
                        ? author.getNickname() : author.getUsername();
                map.put("author", nameToDisplay);
                map.put("avatar", author.getAvatarUrl());
            } else {
                map.put("author", "已注销用户");
                map.put("avatar", "");
            }
            result.add(map);
        }
        return Result.success(result, "获取成功");
    }

    /**
     * 发布新帖子
     */
    @PostMapping("/add")
    public Result<Object> addPost(@RequestBody ForumPost post) {
        post.setCreateTime(LocalDateTime.now());
        post.setViewCount(0);
        return forumPostService.save(post) ? Result.success(null, "发布成功") : Result.error("发布失败");
    }

    /**
     * 根据ID获取帖子详情
     */
    @GetMapping("/{id}")
    public Result getPostById(@PathVariable Long id) {
        ForumPost post = forumPostService.getById(id);
        if (post != null) {
            User user = userService.getById(post.getUserId());
            if (user != null) {
                String name = (user.getNickname() != null && !user.getNickname().isEmpty())
                        ? user.getNickname() : user.getUsername();
                post.setAuthorName(name);
                post.setAuthorAvatar(user.getAvatarUrl());
            }
            return Result.success(post);
        } else {
            return Result.error("未找到该帖子");
        }
    }

    /**
     * 删除帖子（管理端和用户自己共用）
     */
    @DeleteMapping("/{id}")
    public Result deletePost(@PathVariable Long id) {
        boolean removed = forumPostService.removeById(id);
        if (removed) {
            return Result.success(null, "帖子已成功删除");
        } else {
            return Result.error("删除失败，帖子可能已被删除");
        }
    }
}