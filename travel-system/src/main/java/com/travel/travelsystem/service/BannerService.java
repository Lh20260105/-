package com.travel.travelsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.travel.travelsystem.entity.Banner;
import java.util.List;

public interface BannerService extends IService<Banner> {
    List<Banner> getActiveBanners();
}
