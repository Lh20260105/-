package com.travel.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.travelsystem.entity.Attraction;
import com.travel.travelsystem.mapper.AttractionMapper;
import com.travel.travelsystem.service.AttractionService;
import org.springframework.stereotype.Service;

@Service // 必须加这个注解，告诉 Spring 这是业务层
public class AttractionServiceImpl extends ServiceImpl<AttractionMapper, Attraction> implements AttractionService {
}