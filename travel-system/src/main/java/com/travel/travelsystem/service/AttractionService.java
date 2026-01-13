package com.travel.travelsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.travelsystem.entity.Attraction;

// 继承 IService 是 MyBatis Plus 的规范，能让你直接拥有各种复杂的查询功能
public interface AttractionService extends IService<Attraction> {
}