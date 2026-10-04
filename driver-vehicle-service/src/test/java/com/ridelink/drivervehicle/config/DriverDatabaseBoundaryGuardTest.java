package com.ridelink.drivervehicle.config;

import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DriverDatabaseBoundaryGuardTest {

    @Test
    void acceptsDriverServiceDatabase() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoDatabase database = mock(MongoDatabase.class);
        when(mongoTemplate.getDb()).thenReturn(database);
        when(database.getName()).thenReturn("driver_vehicle_db");

        assertDoesNotThrow(() -> new DriverDatabaseBoundaryGuard(mongoTemplate).run(null));
    }

    @Test
    void rejectsAccountServiceDatabase() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoDatabase database = mock(MongoDatabase.class);
        when(mongoTemplate.getDb()).thenReturn(database);
        when(database.getName()).thenReturn("ridelink_account_db");

        assertThrows(IllegalStateException.class,
                () -> new DriverDatabaseBoundaryGuard(mongoTemplate).run(null));
    }
}
