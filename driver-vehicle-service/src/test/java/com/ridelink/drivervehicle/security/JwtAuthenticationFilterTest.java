package com.ridelink.drivervehicle.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtAuthenticationFilterTest {

    @Test
    void normalizeRole_acceptsCaseInsensitiveRoleNamesAndRolePrefix() {
        assertEquals("DRIVER", JwtAuthenticationFilter.normalizeRole("driver"));
        assertEquals("DRIVER", JwtAuthenticationFilter.normalizeRole("ROLE_DRIVER"));
    }

    @Test
    void normalizeRole_rejectsUnknownOrMissingRoles() {
        assertNull(JwtAuthenticationFilter.normalizeRole(null));
        assertNull(JwtAuthenticationFilter.normalizeRole("UNKNOWN"));
    }
}
