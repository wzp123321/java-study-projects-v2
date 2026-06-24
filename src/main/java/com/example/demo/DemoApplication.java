package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * 启动类。
 * 注意:@EnableCaching 拆出来用 @Configuration + @ConditionalOnProperty,
 * 这样 Redis 未启用时不会触发 CacheManager 的创建。
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * 只有 redis 启用时才打开 Spring Cache 注解(@Cacheable 等)。
     * 否则会因为找不到 CacheManager 而启动失败。
     */
    @Configuration
    @EnableCaching
    @ConditionalOnProperty(name = "app.middleware.redis.enabled", havingValue = "true")
    public static class CacheConfiguration {
    }
}
