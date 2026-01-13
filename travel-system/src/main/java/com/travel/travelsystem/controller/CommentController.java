package com.travel.travelsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.entity.ForumComment;
import com.travel.travelsystem.entity.User;
import com.travel.travelsystem.service.ForumCommentService;
import com.travel.travelsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/comment") // 对应前端的 /api/comment
public class CommentController {

    @Autowired
    private ForumCommentService commentService;

    @Autowired
    private UserService userService;

    // 1. 发布评论：对应前端 request.post('/comment/add', ...)
    @PostMapping("/add")
    public Result add(@RequestBody ForumComment comment) {
        comment.setCreateTime(LocalDateTime.now());
        boolean saved = commentService.save(comment);
        return saved ? Result.success(null, "评论成功") : Result.error("评论失败");
    }

    // 2. 获取评论列表：对应前端 request.get(`/comment/list/${id}`)
    @GetMapping("/list/{postId}")
    public Result<List<ForumComment>> list(@PathVariable Long postId) {
        List<ForumComment> comments = commentService.list(
                new QueryWrapper<ForumComment>().eq("post_id", postId).orderByDesc("create_time")
        );

        // 为每一条评论补全评论人的名字和头像
        for (ForumComment c : comments) {
            User user = userService.getById(c.getUserId());
            if (user != null) {
                // 如果没有昵称就用用户名
                c.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                c.setAvatar(user.getAvatarUrl());
            }
        }
        return Result.success(comments);
    }
}