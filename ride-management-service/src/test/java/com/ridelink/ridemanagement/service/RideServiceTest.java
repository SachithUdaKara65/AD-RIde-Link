package com.ridelink.ridemanagement.service;

import com.ridelink.ridemanagement.dto.CreateRideRequest;
import com.ridelink.ridemanagement.dto.RideResponse;
import com.ridelink.ridemanagement.exception.InvalidStateTransitionException;
import com.ridelink.ridemanagement.exception.ResourceNotFoundException;
import com.ridelink.ridemanagement.model.Ride;
import com.ridelink.ridemanagement.model.RideStatus;
import com.ridelink.ridemanagement.repository.RideRepository;
import com.ridelink.ridemanagement.service.impl.RideServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    @Mock
    private RideRepository rideRepository;

    @InjectMocks
    private RideServiceImpl rideService;

    private Ride sampleRide;
    private CreateRideRequest sampleRequest;

    @BeforeEach
    void setUp() {
        Date now = new Date();
        sampleRide = Ride.builder()
                .id("ride_100")
                .passengerId("pass_1")
                .driverId(null)
                .pickupLocation("Colombo 03")
                .destinationLocation("Dehiwala")
                .fare(1250.0)
                .status(RideStatus.REQUESTED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        sampleRequest = CreateRideRequest.builder()
                .passengerId("pass_1")
                .pickupLocation("Colombo 03")
                .destinationLocation("Dehiwala")
                .fare(1250.0)
                .build();
    }

    @Nested
    @DisplayName("Request Ride Tests")
    class RequestRideTests {

        @Test
        @DisplayName("Should successfully create a ride with status REQUESTED")
        void testRequestRide_Success() {
            when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
                Ride saved = invocation.getArgument(0);
                saved.setId("ride_generated_id");
                return saved;
            });

            RideResponse response = rideService.requestRide(sampleRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("ride_generated_id");
            assertThat(response.getPassengerId()).isEqualTo("pass_1");
            assertThat(response.getStatus()).isEqualTo(RideStatus.REQUESTED);
            assertThat(response.getDriverId()).isNull();
            assertThat(response.getFare()).isEqualTo(1250.0);
            assertThat(response.getCreatedAt()).isNotNull();
            assertThat(response.getUpdatedAt()).isNotNull();

            verify(rideRepository).save(any(Ride.class));
        }

        @Test
        @DisplayName("Should fail when request payload is null")
        void testRequestRide_NullRequest() {
            assertThatThrownBy(() -> rideService.requestRide(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Ride request must not be null");

            verify(rideRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when required fields are blank")
        void testRequestRide_BlankFields() {
            CreateRideRequest invalidRequest = CreateRideRequest.builder()
                    .passengerId("   ")
                    .pickupLocation("   ")
                    .destinationLocation("Downtown")
                    .fare(250.0)
                    .build();

            assertThatThrownBy(() -> rideService.requestRide(invalidRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Passenger ID must not be blank");

            verify(rideRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Assign Driver Tests (REQUESTED -> ASSIGNED)")
    class AssignDriverTests {

        @Test
        @DisplayName("Should successfully assign a driver to a REQUESTED ride")
        void testAssignDriver_Success() {
            sampleRide.setStatus(RideStatus.REQUESTED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));
            when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RideResponse response = rideService.assignDriver("ride_100", "drv_88");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(RideStatus.ASSIGNED);
            assertThat(response.getDriverId()).isEqualTo("drv_88");

            verify(rideRepository).save(sampleRide);
        }

        @Test
        @DisplayName("Should fail when ride is not found")
        void testAssignDriver_NotFound() {
            when(rideRepository.findById("non_existent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rideService.assignDriver("non_existent", "drv_88"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Ride not found with id: non_existent");

            verify(rideRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when driver ID is null or blank")
        void testAssignDriver_BlankDriverId() {
            assertThatThrownBy(() -> rideService.assignDriver("ride_100", "   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Driver ID must not be blank");

            verify(rideRepository, never()).findById(any());
            verify(rideRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when ride status is not REQUESTED (e.g. ACCEPTED)")
        void testAssignDriver_InvalidStatus_Accepted() {
            sampleRide.setStatus(RideStatus.ACCEPTED);
            sampleRide.setDriverId("drv_existing");
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.assignDriver("ride_100", "drv_new"))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("Expected status: REQUESTED");

            verify(rideRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when ride status is COMPLETED")
        void testAssignDriver_InvalidStatus_Completed() {
            sampleRide.setStatus(RideStatus.COMPLETED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.assignDriver("ride_100", "drv_88"))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("Expected status: REQUESTED");

            verify(rideRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Accept Ride Tests (ASSIGNED -> ACCEPTED)")
    class AcceptRideTests {

        @Test
        @DisplayName("Should successfully accept ride when status is ASSIGNED")
        void testAcceptRide_Success() {
            sampleRide.setStatus(RideStatus.ASSIGNED);
            sampleRide.setDriverId("drv_88");
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));
            when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RideResponse response = rideService.acceptRide("ride_100");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(RideStatus.ACCEPTED);
            verify(rideRepository).save(sampleRide);
        }

        @Test
        @DisplayName("Should fail when ride is still in REQUESTED status")
        void testAcceptRide_InvalidStatus_Requested() {
            sampleRide.setStatus(RideStatus.REQUESTED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.acceptRide("ride_100"))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("Expected status: ASSIGNED");

            verify(rideRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when ride not found")
        void testAcceptRide_NotFound() {
            when(rideRepository.findById("ride_100")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rideService.acceptRide("ride_100"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Start Ride Tests (ACCEPTED -> IN_PROGRESS)")
    class StartRideTests {

        @Test
        @DisplayName("Should successfully start ride when status is ACCEPTED")
        void testStartRide_Success() {
            sampleRide.setStatus(RideStatus.ACCEPTED);
            sampleRide.setDriverId("drv_88");
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));
            when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RideResponse response = rideService.startRide("ride_100");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(RideStatus.IN_PROGRESS);
            verify(rideRepository).save(sampleRide);
        }

        @Test
        @DisplayName("Should fail when ride status is ASSIGNED (driver has not accepted yet)")
        void testStartRide_InvalidStatus_Assigned() {
            sampleRide.setStatus(RideStatus.ASSIGNED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.startRide("ride_100"))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("Expected status: ACCEPTED");

            verify(rideRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when ride status is COMPLETED")
        void testStartRide_InvalidStatus_Completed() {
            sampleRide.setStatus(RideStatus.COMPLETED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.startRide("ride_100"))
                    .isInstanceOf(InvalidStateTransitionException.class);

            verify(rideRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Complete Ride Tests (IN_PROGRESS -> COMPLETED)")
    class CompleteRideTests {

        @Test
        @DisplayName("Should successfully complete ride when status is IN_PROGRESS")
        void testCompleteRide_Success() {
            sampleRide.setStatus(RideStatus.IN_PROGRESS);
            sampleRide.setDriverId("drv_88");
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));
            when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RideResponse response = rideService.completeRide("ride_100");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(RideStatus.COMPLETED);
            verify(rideRepository).save(sampleRide);
        }

        @Test
        @DisplayName("Should fail when ride is in ACCEPTED status (not yet started)")
        void testCompleteRide_InvalidStatus_Accepted() {
            sampleRide.setStatus(RideStatus.ACCEPTED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.completeRide("ride_100"))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("Expected status: IN_PROGRESS");

            verify(rideRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail when ride is in CANCELLED status")
        void testCompleteRide_InvalidStatus_Cancelled() {
            sampleRide.setStatus(RideStatus.CANCELLED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.completeRide("ride_100"))
                    .isInstanceOf(InvalidStateTransitionException.class);

            verify(rideRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Cancel Ride Tests")
    class CancelRideTests {

        @Test
        @DisplayName("Should successfully cancel ride when status is REQUESTED")
        void testCancelRide_WhenRequested_Success() {
            sampleRide.setStatus(RideStatus.REQUESTED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));
            when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RideResponse response = rideService.cancelRide("ride_100");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(RideStatus.CANCELLED);
            verify(rideRepository).save(sampleRide);
        }

        @Test
        @DisplayName("Should successfully cancel ride when status is ASSIGNED")
        void testCancelRide_WhenAssigned_Success() {
            sampleRide.setStatus(RideStatus.ASSIGNED);
            sampleRide.setDriverId("drv_88");
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));
            when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RideResponse response = rideService.cancelRide("ride_100");

            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(RideStatus.CANCELLED);
            verify(rideRepository).save(sampleRide);
        }

        @Test
        @DisplayName("Should fail to cancel when status is ACCEPTED")
        void testCancelRide_WhenAccepted_Fails() {
            sampleRide.setStatus(RideStatus.ACCEPTED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.cancelRide("ride_100"))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("Cancellation is only allowed when ride is in REQUESTED or ASSIGNED status");

            verify(rideRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail to cancel when status is IN_PROGRESS")
        void testCancelRide_WhenInProgress_Fails() {
            sampleRide.setStatus(RideStatus.IN_PROGRESS);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.cancelRide("ride_100"))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("Current status: IN_PROGRESS");

            verify(rideRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should fail to cancel when status is COMPLETED")
        void testCancelRide_WhenCompleted_Fails() {
            sampleRide.setStatus(RideStatus.COMPLETED);
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            assertThatThrownBy(() -> rideService.cancelRide("ride_100"))
                    .isInstanceOf(InvalidStateTransitionException.class)
                    .hasMessageContaining("Current status: COMPLETED");

            verify(rideRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Query Ride Tests")
    class QueryRideTests {

        @Test
        @DisplayName("Should get ride details by ID")
        void testGetRideById_Success() {
            when(rideRepository.findById("ride_100")).thenReturn(Optional.of(sampleRide));

            RideResponse response = rideService.getRideById("ride_100");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo("ride_100");
            assertThat(response.getPassengerId()).isEqualTo("pass_1");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when ride ID not found")
        void testGetRideById_NotFound() {
            when(rideRepository.findById("unknown_id")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> rideService.getRideById("unknown_id"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Ride not found with id: unknown_id");
        }

        @Test
        @DisplayName("Should get passenger ride history")
        void testGetRidesByPassengerId_Success() {
            when(rideRepository.findByPassengerId("pass_1")).thenReturn(List.of(sampleRide));

            List<RideResponse> history = rideService.getRidesByPassengerId("pass_1");

            assertThat(history).hasSize(1);
            assertThat(history.get(0).getPassengerId()).isEqualTo("pass_1");
            verify(rideRepository).findByPassengerId("pass_1");
        }

        @Test
        @DisplayName("Should get driver assigned rides")
        void testGetRidesByDriverId_Success() {
            sampleRide.setDriverId("drv_88");
            when(rideRepository.findByDriverId("drv_88")).thenReturn(List.of(sampleRide));

            List<RideResponse> driverRides = rideService.getRidesByDriverId("drv_88");

            assertThat(driverRides).hasSize(1);
            assertThat(driverRides.get(0).getDriverId()).isEqualTo("drv_88");
            verify(rideRepository).findByDriverId("drv_88");
        }
    }
}
