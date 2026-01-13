package com.travel.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.travelsystem.entity.ForumPost;
import com.travel.travelsystem.mapper.ForumPostMapper;
import com.travel.travelsystem.service.ForumPostService;
import org.springframework.stereotype.Service;

@Service
public class ForumPostServiceImpl extends ServiceImpl<ForumPostMapper, ForumPost> implements ForumPostService {
}