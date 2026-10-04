package com.ridelink.account.config;

import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MongoDatabaseBoundaryGuardTest {

    @Test
    void acceptsAccountServiceDatabase() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoDatabase database = mock(MongoDatabase.class);
        when(mongoTemplate.getDb()).thenReturn(database);
        when(database.getName()).thenReturn("ridelink_account_db");

        assertDoesNotThrow(() -> new MongoDatabaseBoundaryGuard(mongoTemplate).run(null));
    }

    @Test
    void rejectsDriverServiceDatabase() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        MongoDatabase database = mock(MongoDatabase.class);
        when(mongoTemplate.getDb()).thenReturn(database);
        when(database.getName()).thenReturn("driver_vehicle_db");

        assertThrows(IllegalStateException.class,
                () -> new MongoDatabaseBoundaryGuard(mongoTemplate).run(null));
    }
}
