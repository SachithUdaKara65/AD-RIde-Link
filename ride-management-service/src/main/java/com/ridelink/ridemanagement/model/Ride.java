package com.ridelink.ridemanagement.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rides")
public class Ride {

    @Id
    private String id;

    private String passengerId;

    private String driverId;

    private String pickupLocation;

    private String destinationLocation;

    private Double fare;

    private RideStatus status;

    private Date createdAt;

    private Date updatedAt;
}
