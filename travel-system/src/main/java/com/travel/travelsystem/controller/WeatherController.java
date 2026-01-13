package com.travel.travelsystem.controller;

import com.travel.travelsystem.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @GetMapping("/info")
    public Result<Map<String, Object>> getWeather(@RequestParam String city) {
        // 纯模拟数据，保证不报错
        Map<String, Object> result = new HashMap<>();
        result.put("city", city);
        result.put("status", "晴转多云");
        result.put("temp", "22");
        result.put("tips", "天气不错，适合出游");
        return Result.success(result, "获取模拟天气成功");
    }
}