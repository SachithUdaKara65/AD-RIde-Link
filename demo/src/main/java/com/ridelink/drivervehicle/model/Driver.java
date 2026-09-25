package com.ridelink.drivervehicle.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "drivers")
public class Driver {

    @Id
    private String id;

    private String accountId;

    private String name;

    private String phone;

    private String licenseNumber;

    private String availability;

    private String status;

    private String serviceArea;
}
