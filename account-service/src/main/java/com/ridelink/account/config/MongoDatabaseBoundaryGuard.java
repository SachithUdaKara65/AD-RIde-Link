package com.ridelink.account.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoDatabaseBoundaryGuard implements ApplicationRunner {

    private static final String ACCOUNT_DATABASE = "ridelink_account_db";

    private final MongoTemplate mongoTemplate;

    public MongoDatabaseBoundaryGuard(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String databaseName = mongoTemplate.getDb().getName();
        if (!ACCOUNT_DATABASE.equals(databaseName)) {
            throw new IllegalStateException(
                    "Account Service must use MongoDB database '" + ACCOUNT_DATABASE
                            + "', but is configured to use '" + databaseName + "'.");
        }
    }
}
