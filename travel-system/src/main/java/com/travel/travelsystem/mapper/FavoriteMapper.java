package com.travel.travelsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.travelsystem.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}