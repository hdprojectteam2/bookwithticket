//redis
package com.example.bookwithticket.global.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

@Configuration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            redisServer = new RedisServer(6379);
            redisServer.start();
        } catch (Exception ignored) {
            // 이미 포트가 켜져있는 경우에는 무시
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            try {
                redisServer.stop();
            } catch (Exception ignored) {}
        }
    }
}
