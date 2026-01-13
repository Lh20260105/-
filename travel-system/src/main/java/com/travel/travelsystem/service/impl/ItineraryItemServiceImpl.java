package com.travel.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.travelsystem.entity.ItineraryItem;
import com.travel.travelsystem.mapper.ItineraryItemMapper;
import com.travel.travelsystem.service.ItineraryItemService;
import org.springframework.stereotype.Service;

@Service
public class ItineraryItemServiceImpl extends ServiceImpl<ItineraryItemMapper, ItineraryItem> implements ItineraryItemService {
}