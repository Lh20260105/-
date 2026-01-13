package com.travel.travelsystem.controller;

import com.travel.travelsystem.entity.User;
import com.travel.travelsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.travel.travelsystem.common.Result;
import java.util.List;

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
        // 1. 根据用户名查询数据库
        User dbUser = userService.query()
                .eq("username", user.getUsername())
                .one();

        // 2. 校验：用户是否存在 且 密码是否正确
        if (dbUser != null && dbUser.getPassword().equals(user.getPassword())) {
            // 登录成功，返回用户信息（注意：实际开发中通常不返回密码，这里为了简单先这样写）
            return Result.success(dbUser, "登录成功");
        } else {
            // 登录失败
            return Result.error("用户名或密码错误");
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Object> register(@RequestBody User user) {
        // 1. 检查用户名是否已存在
        User existUser = userService.query()
                .eq("username", user.getUsername())
                .one();

        if (existUser != null) {
            return Result.error("用户名已存在，请换一个试试");
        }

        // 2. 设置默认属性
        user.setRole("USER");

        // 3. 执行保存
        boolean saved = userService.save(user);

        if (saved) {
            return Result.success(null, "注册成功！请登录");
        } else {
            return Result.error("注册失败，服务器出了点小差");
        }
    }

    /**
     * 获取所有用户列表 (通常用于管理员查看)
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

    @PostMapping("/update")
    public Result<Object> update(@RequestBody User user) {
        // 根据 ID 更新数据库中的用户信息
        boolean ok = userService.updateById(user);
        if (ok) {
            // 更新成功后，建议返回最新的用户信息，或者让前端重新拉取
            return Result.success(userService.getById(user.getId()), "资料修改成功");
        }
        return Result.error("修改失败");
    }
}