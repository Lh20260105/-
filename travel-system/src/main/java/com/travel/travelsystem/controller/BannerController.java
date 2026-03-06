package com.travel.travelsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.travel.travelsystem.common.Result;
import com.travel.travelsystem.entity.Banner;
import com.travel.travelsystem.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("/banners")
    public Result<List<Banner>> getActiveBanners() {
        List<Banner> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }

    @GetMapping("/admin/banners")
    public Result<List<Banner>> getAllBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Banner::getSortOrder);
        List<Banner> banners = bannerService.list(wrapper);
        return Result.success(banners);
    }

    @PostMapping("/admin/banners")
    public Result<Banner> addBanner(@RequestBody Banner banner) {
        banner.setCreateTime(LocalDateTime.now());
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        bannerService.save(banner);
        return Result.success(banner);
    }

    @PutMapping("/admin/banners/{id}")
    public Result<Banner> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        Banner existing = bannerService.getById(id);
        if (existing == null) {
            return Result.error("轮播图不存在");
        }
        banner.setId(id);
        bannerService.updateById(banner);
        return Result.success(banner);
    }

    @DeleteMapping("/admin/banners/{id}")
    public Result<Void> deleteBanner(@PathVariable Long id) {
        Banner existing = bannerService.getById(id);
        if (existing == null) {
            return Result.error("轮播图不存在");
        }
        bannerService.removeById(id);
        return Result.success(null);
    }

    @PutMapping("/admin/banners/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id) {
        Banner banner = bannerService.getById(id);
        if (banner == null) {
            return Result.error("轮播图不存在");
        }
        banner.setStatus(banner.getStatus() == 1 ? 0 : 1);
        bannerService.updateById(banner);
        return Result.success(null);
    }
}
