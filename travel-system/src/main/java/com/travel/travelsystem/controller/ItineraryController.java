package com.travel.travelsystem.controller;

import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.entity.Attraction;
import com.travel.travelsystem.entity.Itinerary;
import com.travel.travelsystem.entity.ItineraryItem;
import com.travel.travelsystem.service.AttractionService;
import com.travel.travelsystem.service.ItineraryService;
import com.travel.travelsystem.service.ItineraryItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/itinerary")
public class ItineraryController {

    @Autowired
    private ItineraryService itineraryService;

    @Autowired
    private AttractionService attractionService;

    @Autowired
    private ItineraryItemService itineraryItemService;

    /**
     * 1. 获取行程列表 (支持显示离开日期所需的数据)
     * 对应前端：/api/itinerary/user-items/{userId}
     */
    @GetMapping("/user-items/{userId}")
    public Result<Map<String, Object>> getMyItinerary(@PathVariable Long userId) {
        System.out.println(">>> [查询] 正在获取用户 " + userId + " 的详细行程单...");

        // 查找用户的主行程表
        Itinerary itinerary = itineraryService.query()
                .eq("user_id", userId)
                .orderByDesc("create_time")
                .last("limit 1")
                .one();

        if (itinerary == null) {
            return Result.success(null, "暂无行程数据");
        }

        // 查询明细项，并按天数和时间排序
        List<ItineraryItem> items = itineraryItemService.query()
                .eq("itinerary_id", itinerary.getId())
                .orderByAsc("day_number", "start_time")
                .list();

        List<Map<String, Object>> itemList = new ArrayList<>();
        for (ItineraryItem item : items) {
            Map<String, Object> map = new HashMap<>();
            Attraction att = attractionService.getById(item.getAttractionId());

            map.put("id", item.getId());
            map.put("dayNumber", item.getDayNumber());
            // 【关键】：返回停留天数，供前端计算离开日期
            map.put("stayDays", item.getStayDays() != null ? item.getStayDays() : 1);
            map.put("startTime", item.getStartTime() != null ? item.getStartTime().toString() : "");
            map.put("attractionName", att != null ? att.getName() : "未知景点");
            map.put("location", att != null ? att.getLocation() : "未知地点");
            map.put("attractionImageUrl", att != null ? att.getImageUrl() : "");
            itemList.add(map);
        }

        Map<String, Object> finalData = new HashMap<>();
        finalData.put("startDate", itinerary.getStartDate()); // 行程开始日期 (如 2026-01-12)
        finalData.put("items", itemList);

        return Result.success(finalData, "查询成功");
    }

    /**
     * 2. 添加景点到行程 (增加 stayDays 接收)
     * 对应前端：/api/itinerary/add-item
     */
    @PostMapping("/add-item")
    public Result<Object> addItem(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            Long attractionId = Long.valueOf(params.get("attractionId").toString());
            Integer dayNumber = Integer.valueOf(params.get("dayNumber").toString());
            String startTime = params.get("startTime").toString();
            // 【关键】：接收前端传来的停留天数，默认1天
            Integer stayDays = params.containsKey("stayDays") ? Integer.valueOf(params.get("stayDays").toString()) : 1;

            // 锁定或创建主表
            Itinerary itinerary = itineraryService.query().eq("user_id", userId).last("limit 1").one();
            if (itinerary == null) {
                itinerary = new Itinerary();
                itinerary.setUserId(userId);
                itinerary.setTitle("我的旅行计划");
                itinerary.setStartDate(LocalDate.now());
                itinerary.setCreateTime(LocalDateTime.now());
                itineraryService.save(itinerary);
            }

            // 创建明细项
            ItineraryItem item = new ItineraryItem();
            item.setItineraryId(itinerary.getId());
            item.setAttractionId(attractionId);
            item.setDayNumber(dayNumber);
            item.setStayDays(stayDays); // 设置停留天数

            // 时间转换
            if (startTime.length() == 5) startTime += ":00";
            item.setStartTime(LocalTime.parse(startTime));

            itineraryItemService.save(item);
            return Result.success(null, "添加成功");
        } catch (Exception e) {
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 3. 删除行程项
     */
    @DeleteMapping("/item/{id}")
    public Result<Object> deleteItem(@PathVariable Long id) {
        return itineraryItemService.removeById(id) ? Result.success(null, "已从行程中移除") : Result.error("移除失败");
    }

    /**
     * 4. 修改行程项 (包括停留天数、日期和时间)
     * 对应前端：/api/itinerary/update-item
     */
    @PostMapping("/update-item")
    public Result<Object> updateItem(@RequestBody ItineraryItem item) {
        System.out.println(">>> [修改] 正在更新行程项 ID: " + item.getId());

        // 这里的 item 对象会自动包含前端传来的 dayNumber, startTime 和 stayDays
        boolean ok = itineraryItemService.updateById(item);
        if (ok) {
            return Result.success(null, "修改成功");
        }
        return Result.error("修改失败");
    }
}