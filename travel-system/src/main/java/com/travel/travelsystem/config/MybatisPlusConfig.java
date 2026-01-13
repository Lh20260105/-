package com.travel.travelsystem.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 核心配置类
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 【核心配置】：添加分页插件拦截器
     * 如果没有这个 Bean，分页查询就会失效（返回全部数据）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 向拦截器链中添加分页拦截器，并指定数据库类型为 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));

        System.out.println("-----------------------------------------");
        System.out.println(">>> [系统提示] MyBatis-Plus 分页插件已启用");
        System.out.println("-----------------------------------------");

        return interceptor;
    }
}