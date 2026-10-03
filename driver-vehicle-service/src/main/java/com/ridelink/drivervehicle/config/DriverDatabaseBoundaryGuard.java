package com.ridelink.drivervehicle.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DriverDatabaseBoundaryGuard implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DriverDatabaseBoundaryGuard.class);
    private static final String DRIVER_DATABASE = "driver_vehicle_db";

    private final MongoTemplate mongoTemplate;

    public DriverDatabaseBoundaryGuard(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String databaseName = mongoTemplate.getDb().getName();
        if (!DRIVER_DATABASE.equals(databaseName)) {
            throw new IllegalStateException(
                    "Driver & Vehicle Service must use MongoDB database '" + DRIVER_DATABASE
                            + "', but is configured to use '" + databaseName + "'.");
        }
        logger.info("Driver & Vehicle Service is connected to its owned MongoDB database '{}'.", databaseName);
    }
}
