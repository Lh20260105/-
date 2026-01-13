package com.travel.travelsystem.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.travel.travelsystem.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}