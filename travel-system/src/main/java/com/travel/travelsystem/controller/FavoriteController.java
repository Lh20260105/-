package com.travel.travelsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.entity.Attraction;
import com.travel.travelsystem.entity.Favorite;
import com.travel.travelsystem.service.AttractionService;
import com.travel.travelsystem.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private AttractionService attractionService;

    /**
     * 1. 添加收藏
     */
    @PostMapping("/add")
    public Result<Object> add(@RequestBody Favorite favorite) {
        Favorite exist = favoriteService.query()
                .eq("user_id", favorite.getUserId())
                .eq("attraction_id", favorite.getAttractionId())
                .one();
        if (exist != null) {
            return Result.error("已经在收藏夹中啦");
        }
        boolean ok = favoriteService.save(favorite);
        return ok ? Result.success(null, "收藏成功") : Result.error("收藏失败");
    }

    /**
     * 2. 取消收藏 (根据用户ID和景点ID精准删除)
     */
    @DeleteMapping("/remove")
    public Result<Object> remove(@RequestParam Long userId, @RequestParam Long attractionId) {
        QueryWrapper<Favorite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("attraction_id", attractionId);
        boolean ok = favoriteService.remove(queryWrapper);
        System.out.println(">>> [取消收藏] userId=" + userId + ", attractionId=" + attractionId + ", 结果=" + ok);
        return ok ? Result.success(null, "已取消收藏") : Result.error("取消失败");
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam Long userId, @RequestParam Long attractionId) {
        Favorite exist = favoriteService.query()
                .eq("user_id", userId)
                .eq("attraction_id", attractionId)
                .one();
        return Result.success(exist != null);
    }

    /**
     * 3. 获取当前用户收藏的所有景点 ID 列表
     */
    @GetMapping("/ids/{userId}")
    public Result<List<Long>> getFavoriteIds(@PathVariable Long userId) {
        List<Favorite> list = favoriteService.query().eq("user_id", userId).list();
        List<Long> ids = new ArrayList<>();
        for (Favorite f : list) {
            ids.add(f.getAttractionId());
        }
        return Result.success(ids, "获取成功");
    }

    /**
     * 4. 获取收藏夹完整列表 (供收藏页面使用)
     */
    @GetMapping("/list/{userId}")
    public Result<List<Attraction>> getList(@PathVariable Long userId) {
        List<Favorite> favorites = favoriteService.query().eq("user_id", userId).list();
        List<Attraction> result = new ArrayList<>();
        for (Favorite f : favorites) {
            Attraction att = attractionService.getById(f.getAttractionId());
            if (att != null) result.add(att);
        }
        return Result.success(result, "获取成功");
    }
}
