package com.travel.travelsystem.controller;

import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.service.AttractionService;
import com.travel.travelsystem.service.FavoriteService;
import com.travel.travelsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/statistics")
public class StatisticsController {

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private UserService userService;

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 1. 获取基础统计数据 (总景点、总用户、今日新增)
     */
    @GetMapping("/base")
    public Result<Map<String, Object>> getBaseStats() {
        Map<String, Object> map = new HashMap<>();

        // 总景点数
        long totalAttractions = attractionService.count();
        // 总用户数
        long totalUsers = userService.count();

        // 今日新增用户数 (查询创建时间在今天 00:00 之后的用户)
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long todayNewUsers = userService.query().gt("create_time", todayStart).count();

        map.put("totalAttractions", totalAttractions);
        map.put("totalUsers", totalUsers);
        map.put("todayNewUsers", todayNewUsers);

        return Result.success(map, "基础统计获取成功");
    }

    /**
     * 2. 获取热门景点排行 (根据收藏表统计)
     */
    @GetMapping("/hot-attractions")
    public Result<List<Map<String, Object>>> getHotAttractions() {
        // 这一步逻辑：从收藏表中按 attraction_id 分组统计数量，取前 10 名
        // 为了小白理解，我们用简单逻辑实现：

        // A. 获取所有景点
        List<com.travel.travelsystem.entity.Attraction> allAttractions = attractionService.list();
        List<Map<String, Object>> hotList = new ArrayList<>();

        for (com.travel.travelsystem.entity.Attraction att : allAttractions) {
            // B. 统计每个景点的收藏数
            long count = favoriteService.query().eq("attraction_id", att.getId()).count();

            Map<String, Object> item = new HashMap<>();
            item.put("name", att.getName());
            item.put("collectCount", count);
            hotList.add(item);
        }

        // C. 按收藏数从大到小排序，只取前 10 个
        hotList.sort((o1, o2) -> ((Long) o2.get("collectCount")).compareTo((Long) o1.get("collectCount")));

        if (hotList.size() > 10) {
            hotList = hotList.subList(0, 10);
        }

        return Result.success(hotList, "排行数据获取成功");
    }
}