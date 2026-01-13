package com.travel.travelsystem.mapper; // 注意这里加了 travelsystem

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.travelsystem.entity.Attraction; // 确保这里也对应上
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttractionMapper extends BaseMapper<Attraction> {
    // 这里继承了 BaseMapper，已经具备了基本的增删改查功能
}