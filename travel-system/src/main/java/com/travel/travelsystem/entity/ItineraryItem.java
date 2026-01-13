package com.travel.travelsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalTime;

@Data
@TableName("itinerary_item")
public class ItineraryItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long itineraryId;
    private Long attractionId;
    private Integer dayNumber;
    private LocalTime startTime; // 用户计划去的时间
    private Integer stayDays;
}