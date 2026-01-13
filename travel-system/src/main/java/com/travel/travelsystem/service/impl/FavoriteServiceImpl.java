package com.travel.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.travelsystem.entity.Favorite;
import com.travel.travelsystem.mapper.FavoriteMapper;
import com.travel.travelsystem.service.FavoriteService;
import org.springframework.stereotype.Service;

@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {
}