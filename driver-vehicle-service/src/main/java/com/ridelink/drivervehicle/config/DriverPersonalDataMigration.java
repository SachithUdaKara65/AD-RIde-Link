package com.ridelink.drivervehicle.config;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
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
    private static final String ACCOUNT_USER_CLASS = "com.ridelink.account.models.User";

    private final MongoTemplate mongoTemplate;

    public DriverPersonalDataMigration(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String databaseName = mongoTemplate.getDb().getName();
        if (!"driver_vehicle_db".equals(databaseName)) {
            throw new IllegalStateException(
                    "Driver & Vehicle Service must use MongoDB database 'driver_vehicle_db', but is configured to use '"
                            + databaseName + "'.");
        }

        Query accountUsers = Query.query(new Criteria().orOperator(
                Criteria.where("_class").is(ACCOUNT_USER_CLASS),
                Criteria.where("email").exists(true),
                Criteria.where("password").exists(true),
                Criteria.where("role").exists(true)));
        DeleteResult deletedUsers = mongoTemplate.remove(accountUsers, "users");
        if (deletedUsers.getDeletedCount() > 0) {
            logger.warn("Removed {} legacy Account Service user documents from the Driver & Vehicle database.",
                    deletedUsers.getDeletedCount());
        }

        Query personalData = Query.query(new Criteria().orOperator(
                Criteria.where("fullName").exists(true),
                Criteria.where("name").exists(true),
                Criteria.where("phone").exists(true),
                Criteria.where("email").exists(true),
                Criteria.where("password").exists(true),
                Criteria.where("role").exists(true)));
        Update unsetPersonalData = new Update()
                .unset("fullName")
                .unset("name")
                .unset("phone")
                .unset("email")
                .unset("password")
                .unset("role");
        UpdateResult cleanedDrivers = mongoTemplate.updateMulti(personalData, unsetPersonalData, Driver.class);
        if (cleanedDrivers.getModifiedCount() > 0) {
            logger.info("Removed personal account data from {} legacy driver profiles.",
                    cleanedDrivers.getModifiedCount());
        }
    }
}
