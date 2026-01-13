package com.travel.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.travelsystem.entity.Itinerary;
import com.travel.travelsystem.mapper.ItineraryMapper;
import com.travel.travelsystem.service.ItineraryService;
import org.springframework.stereotype.Service;

@Service
public class ItineraryServiceImpl extends ServiceImpl<ItineraryMapper, Itinerary> implements ItineraryService {
}