package com.travel.travelsystem.controller;

import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.entity.User;
import com.travel.travelsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 必须导入

import java.io.File;
import java.io.IOException;
import java.util.List;
@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody User user) {
        User dbUser = userService.query()
                .eq("username", user.getUsername())
                .one();

        if (dbUser != null && dbUser.getPassword().equals(user.getPassword())) {
            return Result.success(dbUser, "登录成功");
        } else {
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Object> register(@RequestBody User user) {
        User existUser = userService.query()
                .eq("username", user.getUsername())
                .one();

        if (existUser != null) {
            return Result.error("用户名已存在，请换一个试试");
        }

        user.setRole("USER");
        boolean saved = userService.save(user);

        if (saved) {
            return Result.success(null, "注册成功！请登录");
        } else {
            return Result.error("注册失败，服务器出了点小差");
        }
    }

    /**
     * --- 【核心新增：自定义头像上传接口】 ---
     * 处理逻辑：接收图片 -> 保存到硬盘 -> 返回访问地址并更新数据库
     */
    @PostMapping("/upload-avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestParam("userId") Long userId) {
        if (file.isEmpty()) {
            return Result.error("请选择一张图片上传");
        }

        try {
            // 1. 定义文件的名字（时间戳 + 原名，防止文件名重复覆盖）
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // 2. 获取项目的根目录，并创建一个名为 "files" 的文件夹用来存图片
            String projectPath = System.getProperty("user.dir");
            String uploadPath = projectPath + "/files/";
            File folder = new File(uploadPath);
            if (!folder.exists()) {
                folder.mkdirs(); // 如果文件夹不存在就创建一个
            }

            // 3. 将文件真正保存到硬盘里
            File dest = new File(uploadPath + fileName);
            file.transferTo(dest);

            // 4. 拼凑出前端可以访问的路径（假设后端端口是 8080）
            String avatarUrl = "http://localhost:8080/files/" + fileName;

            // 5. 更新数据库中该用户的 avatar_url 字段
            boolean updated = userService.update()
                    .set("avatar_url", avatarUrl)
                    .eq("id", userId)
                    .update();

            if (updated) {
                System.out.println(">>> [头像更新] 用户 ID " + userId + " 的新头像已存入数据库: " + avatarUrl);
                return Result.success(avatarUrl, "头像上传成功");
            } else {
                return Result.error("数据库更新失败");
            }

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件读写错误：" + e.getMessage());
        }
    }

    /**
     * 获取所有用户列表
     */
    @GetMapping("/list")
    public Result<List<User>> list() {
        List<User> users = userService.list();
        return Result.success(users, "获取成功");
    }

    /**
     * 根据 ID 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public Result<Object> delete(@PathVariable Long id) {
        boolean removed = userService.removeById(id);
        if (removed) {
            return Result.success(null, "删除成功");
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 修改个人资料
     */
    @PostMapping("/update")
    public Result<Object> update(@RequestBody User user) {
        boolean ok = userService.updateById(user);
        if (ok) {
            // 返回更新后的最新用户信息，包含最新的 avatar_url
            return Result.success(userService.getById(user.getId()), "资料修改成功");
        }
        return Result.error("修改失败");
    }
}