package com.travel.travelsystem.controller;

import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.entity.Attraction;
import com.travel.travelsystem.entity.Favorite;
import com.travel.travelsystem.service.AttractionService;
import com.travel.travelsystem.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommend")
public class RecommendController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private AttractionService attractionService;

    /**
     * 获取个性化推荐景点
     * 逻辑：根据用户收藏最多的分类进行推荐
     */
    @GetMapping("/personal/{userId}")
    public Result<List<Attraction>> getPersonalRecommend(@PathVariable Long userId) {
        // 1. 获取该用户所有的收藏记录
        List<Favorite> favorites = favoriteService.query().eq("user_id", userId).list();

        // 2. 兜底逻辑：如果用户还没收藏过任何东西，随机推荐 4 个景点
        if (favorites.isEmpty()) {
            List<Attraction> randomList = attractionService.query().last("limit 4").list();
            return Result.success(randomList, "为您推荐热门景点");
        }

        // 3. 统计用户最喜欢的分类 (Category)
        Map<String, Long> categoryCount = new HashMap<>();
        for (Favorite fav : favorites) {
            Attraction att = attractionService.getById(fav.getAttractionId());
            if (att != null && att.getCategory() != null) {
                categoryCount.put(att.getCategory(), categoryCount.getOrDefault(att.getCategory(), 0L) + 1);
            }
        }

        // 4. 找出收藏次数最多的分类
        String topCategory = categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("自然风光"); // 如果没统计出来，默认推荐自然风光

        // 5. 获取用户已收藏的 ID 集合，用于排除
        Set<Long> favoritedIds = favorites.stream()
                .map(Favorite::getAttractionId)
                .collect(Collectors.toSet());

        // 6. 从该分类中选出用户尚未收藏的景点进行推荐
        List<Attraction> recommends = attractionService.query()
                .eq("category", topCategory)
                .notIn(!favoritedIds.isEmpty(), "id", favoritedIds) // 排除已收藏的
                .last("limit 4") // 只推荐 4 个，保持页面美观
                .list();

        // 7. 如果该分类下没有更多景点了，再拿全库前 4 个补位
        if (recommends.isEmpty()) {
            recommends = attractionService.query().last("limit 4").list();
        }

        return Result.success(recommends, "根据您的偏好，为您推荐了 " + topCategory + " 相关的景点");
    }
}
