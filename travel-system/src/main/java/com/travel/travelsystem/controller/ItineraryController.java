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
            String startTime = params.get("startTime").toString();
            // 1. 接收前端传来的日期字符串
            LocalDate selectedDate = LocalDate.parse(params.get("selectedDate").toString());

            // 查找该用户的行程
            Itinerary itinerary = itineraryService.query().eq("user_id", userId).last("limit 1").one();

            if (itinerary == null) {
                // 如果是第一次添加行程，将选中的日期作为整个行程的开始日期
                itinerary = new Itinerary();
                itinerary.setUserId(userId);
                itinerary.setTitle("我的旅行计划");
                itinerary.setStartDate(selectedDate); // 设为选中日期
                itinerary.setCreateTime(LocalDateTime.now());
                itineraryService.save(itinerary);
            }

            // 2. 核心逻辑：计算 dayNumber
            // 规则：(选中日期 - 行程开始日期) + 1。 比如 4.18 出发，选 4.18 就是 Day 1
            long diff = java.time.temporal.ChronoUnit.DAYS.between(itinerary.getStartDate(), selectedDate);
            int dayNumber = (int) diff + 1;

            // 如果用户选了一个比行程开始日期更早的日期（通常被前端拦截了，这里做个兜底）
            if (dayNumber < 1) {
                // 可选方案：自动更新行程开始日期为更早的这个日期
                itinerary.setStartDate(selectedDate);
                itineraryService.updateById(itinerary);
                dayNumber = 1; // 重置为第一天
            }

            // 创建行程项
            ItineraryItem item = new ItineraryItem();
            item.setItineraryId(itinerary.getId());
            item.setAttractionId(attractionId);
            item.setDayNumber(dayNumber); // 设置计算出的天数
            item.setStayDays(1);

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

    /**
     * 5. 更新整个行程的开始日期
     * 对应前端：/api/itinerary/update-start-date
     */
    @PostMapping("/update-start-date")
    public Result<Object> updateStartDate(@RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String newDate = params.get("startDate").toString(); // 格式：2026-04-18

            // 找到该用户最新的行程记录
            Itinerary itinerary = itineraryService.query()
                    .eq("user_id", userId)
                    .last("limit 1")
                    .one();

            if (itinerary != null) {
                itinerary.setStartDate(LocalDate.parse(newDate));
                itineraryService.updateById(itinerary);
                return Result.success(null, "出发日期已更新");
            }
            return Result.error("未找到行程计划");
        } catch (Exception e) {
            return Result.error("日期更新失败: " + e.getMessage());
        }
    }
}