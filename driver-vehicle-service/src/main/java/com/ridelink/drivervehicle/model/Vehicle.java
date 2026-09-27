package com.ridelink.drivervehicle.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "vehicles")
public class Vehicle {

    @Id
    private String id;

    private String driverId;           // Reference to Driver
    private String make;
    private String model;
    private Integer year;
    private String color;
    private String licensePlate;
    private String vehicleType;        // CAR, VAN, MOTORCYCLE
    private boolean isActive;
}