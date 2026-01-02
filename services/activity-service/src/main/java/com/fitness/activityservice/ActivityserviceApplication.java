package com.fitness.activityservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class ActivityserviceApplication {

    private static final Logger log = LoggerFactory.getLogger(ActivityserviceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ActivityserviceApplication.class, args);
    }

    @Bean
    public ApplicationRunner mongoDatabaseStartupLogger(MongoTemplate mongoTemplate) {
        return args -> {
            String dbName = mongoTemplate.getDb().getName();
            log.info("MongoDB database in use: {}", dbName);
        };
    }

}
