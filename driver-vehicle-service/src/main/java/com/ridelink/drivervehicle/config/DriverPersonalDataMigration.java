package com.ridelink.drivervehicle.config;

import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.result.DeleteResult;
import com.ridelink.drivervehicle.model.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

@Component
public class DriverPersonalDataMigration implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DriverPersonalDataMigration.class);
    private static final String DRIVER_DATABASE = "driver_vehicle_db";
    private static final String ACCOUNT_USER_CLASS = "com.ridelink.account.models.User";

    private final MongoTemplate mongoTemplate;

    public DriverPersonalDataMigration(MongoTemplate mongoTemplate) {
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

        DeleteResult removedAccountUsers = mongoTemplate.remove(
                Query.query(Criteria.where("_class").is(ACCOUNT_USER_CLASS)),
                "users");
        if (removedAccountUsers.getDeletedCount() > 0) {
            logger.warn("Removed {} legacy Account Service user documents from the Driver & Vehicle database.",
                    removedAccountUsers.getDeletedCount());
        }

        Query query = Query.query(new Criteria().orOperator(
                Criteria.where("name").exists(true),
                Criteria.where("fullName").exists(true),
                Criteria.where("phone").exists(true),
                Criteria.where("email").exists(true),
                Criteria.where("password").exists(true),
                Criteria.where("role").exists(true)));
        Update update = new Update()
                .unset("name")
                .unset("fullName")
                .unset("phone")
                .unset("email")
                .unset("password")
                .unset("role");

        UpdateResult result = mongoTemplate.updateMulti(query, update, Driver.class);
        if (result.getModifiedCount() > 0) {
            logger.info("Removed account personal data from {} legacy driver profiles.",
                    result.getModifiedCount());
        }
    }
}
