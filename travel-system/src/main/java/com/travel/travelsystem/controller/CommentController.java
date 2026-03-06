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
@RequestMapping("/api/comment")
public class CommentController {

    @Autowired
    private ForumCommentService commentService;

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public Result add(@RequestBody ForumComment comment) {
        comment.setCreateTime(LocalDateTime.now());
        boolean saved = commentService.save(comment);
        return saved ? Result.success(null, "评论成功") : Result.error("评论失败");
    }

    @GetMapping("/list/{postId}")
    public Result<List<ForumComment>> list(@PathVariable Long postId) {
        List<ForumComment> comments = commentService.list(
                new QueryWrapper<ForumComment>().eq("post_id", postId).orderByDesc("create_time")
        );

        for (ForumComment c : comments) {
            User user = userService.getById(c.getUserId());
            if (user != null) {
                String nickname = user.getNickname() != null && !user.getNickname().isEmpty() 
                    ? user.getNickname() : user.getUsername();
                String avatar = user.getAvatarUrl();
                
                c.setNickname(nickname);
                c.setAvatar(avatar);
                
                System.out.println(">>> [评论] 用户ID: " + c.getUserId() + ", 昵称: " + nickname + ", 头像: " + avatar);
            }
        }
        return Result.success(comments);
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        boolean removed = commentService.removeById(id);
        return removed ? Result.success(null, "删除成功") : Result.error("删除失败");
    }
}
