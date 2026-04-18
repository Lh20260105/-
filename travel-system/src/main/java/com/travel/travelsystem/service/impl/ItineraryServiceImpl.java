package com.travel.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.travelsystem.entity.Itinerary;
import com.travel.travelsystem.mapper.ItineraryMapper;
import com.travel.travelsystem.service.ItineraryService;
import org.springframework.stereotype.Service;
import java.time.temporal.ChronoUnit;

@Service
public class ItineraryServiceImpl extends ServiceImpl<ItineraryMapper, Itinerary> implements ItineraryService {

    // 建议在获取行程信息时调用此逻辑
    public long calculateTotalDays(Itinerary itinerary) {
        if (itinerary.getStartDate() == null || itinerary.getEndDate() == null) {
            return 0;
        }
        // 使用 ChronoUnit 计算日期差，并 +1 以包含当天
        return ChronoUnit.DAYS.between(itinerary.getStartDate(), itinerary.getEndDate()) + 1;
    }
}