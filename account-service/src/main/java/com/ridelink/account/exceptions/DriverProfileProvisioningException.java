package com.ridelink.account.exceptions;

public class DriverProfileProvisioningException extends RuntimeException {

    public DriverProfileProvisioningException(String message) {
        super(message);
    }

    public DriverProfileProvisioningException(String message, Throwable cause) {
        super(message, cause);
    }
}
