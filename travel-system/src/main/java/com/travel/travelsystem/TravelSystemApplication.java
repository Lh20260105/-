package com.travel.travelsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.travel.travelsystem.mapper") // 确保这里的路径是 travelsystem.mapper
public class TravelSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(TravelSystemApplication.class, args);
    }
}