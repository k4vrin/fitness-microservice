package com.fitness.aiservice.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableReactiveMongoAuditing;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
@EnableReactiveMongoAuditing
public class MongoConfig {

    private static final String MONGO_URI =
            "mongodb://aiservice:aiservice@localhost:27019/fitness_ai_db?authSource=admin";
    private static final String DATABASE_NAME = "fitness_ai_db";

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(MONGO_URI);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(MongoClient mongoClient) {
        return new ReactiveMongoTemplate(mongoClient, DATABASE_NAME);
    }
}
