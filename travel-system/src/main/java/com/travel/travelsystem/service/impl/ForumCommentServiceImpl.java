package com.travel.travelsystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.travel.travelsystem.entity.ForumComment;
import com.travel.travelsystem.mapper.ForumCommentMapper;
import com.travel.travelsystem.service.ForumCommentService;
import org.springframework.stereotype.Service;

@Service
public class ForumCommentServiceImpl extends ServiceImpl<ForumCommentMapper, ForumComment> implements ForumCommentService {
}