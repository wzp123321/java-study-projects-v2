package com.example.demo.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
@ConditionalOnProperty(name = "app.middleware.mongo.enabled", havingValue = "true", matchIfMissing = true)
public class MongoConfig {

}
