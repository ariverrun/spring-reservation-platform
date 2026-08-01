package com.example.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.reservation.dto.CreateReservationRequestDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.ReservationEventDto;
import com.example.reservation.dto.ReservationWithEventDto;
import com.example.reservation.dto.UpdateReservationRequestDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.entity.Reservation;
import com.example.reservation.exceptions.AccessViolationException;
import com.example.reservation.exceptions.EntityNotFoundException;
import com.example.reservation.exceptions.NotEnoughFreeSeatsException;
import com.example.reservation.exceptions.RepeatedActiveReservationException;
import com.example.reservation.repository.ReservationRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@Import(ReservationServiceImpl.class)
class ReservationServiceImplTest {

    @Autowired
    private ReservationServiceImpl reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    private static final UUID EXISTING_USER_ID_1 = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID EXISTING_USER_ID_2 = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID EXISTING_USER_ID_3 = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID EXISTING_USER_ID_4 = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final UUID EXISTING_USER_ID_5 = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    private static final UUID EXISTING_EVENT_ID_1 = UUID.fromString("12345678-90ab-cdef-1234-567890abcdef");
    private static final UUID EXISTING_EVENT_ID_2 = UUID.fromString("22345678-90ab-cdef-1234-567890abcdef");
    private static final UUID EXISTING_EVENT_ID_3 = UUID.fromString("32345678-90ab-cdef-1234-567890abcdef");

    private static final UUID EXISTING_RESERVATION_ID_1 = UUID.fromString("41000000-0000-0000-0000-000000000001");
    private static final UUID EXISTING_RESERVATION_ID_2 = UUID.fromString("41000000-0000-0000-0000-000000000002");
    private static final UUID EXISTING_RESERVATION_ID_3 = UUID.fromString("41000000-0000-0000-0000-000000000003");
    private static final UUID EXISTING_RESERVATION_ID_4 = UUID.fromString("41000000-0000-0000-0000-000000000004");
    private static final UUID EXISTING_RESERVATION_ID_5 = UUID.fromString("41000000-0000-0000-0000-000000000005");

    @ParameterizedTest
    @MethodSource("getUserReservations")
    void shouldGetUserReservations(UserInfo user, List<ReservationWithEventDto> expectedReservations) {
        List<ReservationWithEventDto> actualReservations = reservationService.getUserReservations(user);
        assertThat(actualReservations)
            .usingRecursiveComparison()
            .isEqualTo(expectedReservations);
    }

    @ParameterizedTest
    @MethodSource("getEventReservations")
    void shouldGetEventReservations(UUID eventId, List<ReservationDto> expectedReservations) {
        List<ReservationDto> actualReservations = reservationService.getEventReservations(eventId);
        assertThat(actualReservations)
            .usingRecursiveComparison()
            .isEqualTo(expectedReservations);
    }

    @ParameterizedTest
    @MethodSource("getUserReservationById")
    void shouldGetUserReservationById(UUID reservationId, UserInfo user, ReservationWithEventDto expectedReservation) {
        ReservationWithEventDto actualReservation = reservationService.getUserReservationById(reservationId, user);
        assertThat(actualReservation)
            .usingRecursiveComparison()
            .isEqualTo(expectedReservation);
    }

    @ParameterizedTest
    @MethodSource("getInvalidUserReservationById")
    void shouldThrowExceptionWhenGetUserReservationByIdWithInvalidUser(UUID reservationId, UserInfo user) {
        assertThatThrownBy(() -> reservationService.getUserReservationById(reservationId, user))
            .isInstanceOf(AccessViolationException.class)
            .hasMessage("Access denied");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "41000000-0000-0000-0000-0000000000f0",
        "41000000-0000-0000-0000-0000000000f1"
    })
    void shouldThrowExceptionWhenGetUserReservationByIdNotFound(UUID nonExistentId) {
        UserInfo user = UserInfo.builder()
            .id(EXISTING_USER_ID_1)
            .roles(List.of("USER"))
            .build();

        assertThatThrownBy(() -> reservationService.getUserReservationById(nonExistentId, user))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Reservation not found with id: " + nonExistentId);
    }

    @ParameterizedTest
    @MethodSource("getReservationsForInsert")
    @Transactional
    @Rollback
    void shouldCreateReservation(CreateReservationRequestDto request, UserInfo user, ReservationDto expectedReservation) {
        ReservationDto createdReservation = reservationService.createReservation(request, user);
        assertThat(createdReservation)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(expectedReservation);
        assertThat(createdReservation.id()).isNotNull();

        Reservation savedReservation = reservationRepository.findById(createdReservation.id()).orElse(null);
        assertThat(savedReservation).isNotNull();
        assertThat(savedReservation.getUserId()).isEqualTo(user.getId());
        assertThat(savedReservation.getEvent().getId()).isEqualTo(request.eventId());
        assertThat(savedReservation.getSeats()).isEqualTo(request.seats());
        assertThat(savedReservation.getIsCanceled()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("getReservationsForInsertWithErrors")
    @Transactional
    @Rollback
    void shouldThrowExceptionWhenCreateReservationFails(CreateReservationRequestDto request, UserInfo user, Class<? extends Exception> expectedException, String expectedMessage) {
        assertThatThrownBy(() -> reservationService.createReservation(request, user))
            .isInstanceOf(expectedException)
            .hasMessage(expectedMessage);
    }

    @ParameterizedTest
    @MethodSource("getReservationsForUpdate")
    @Transactional
    @Rollback
    void shouldUpdateReservation(UUID reservationId, UpdateReservationRequestDto request, UserInfo user, ReservationDto expectedReservation) {
        ReservationDto updatedReservation = reservationService.updateReservation(reservationId, request, user);
        assertThat(updatedReservation)
            .usingRecursiveComparison()
            .isEqualTo(expectedReservation);

        Reservation dbReservation = reservationRepository.findById(reservationId).orElse(null);
        assertThat(dbReservation).isNotNull();
        assertThat(dbReservation.getSeats()).isEqualTo(request.seats());
        assertThat(dbReservation.getIsCanceled()).isEqualTo(request.isCanceled());
    }

    @ParameterizedTest
    @MethodSource("getReservationsForUpdateWithErrors")
    @Transactional
    @Rollback
    void shouldThrowExceptionWhenUpdateReservationFails(UUID reservationId, UpdateReservationRequestDto request, UserInfo user, Class<? extends Exception> expectedException, String expectedMessage) {
        assertThatThrownBy(() -> reservationService.updateReservation(reservationId, request, user))
            .isInstanceOf(expectedException)
            .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> getUserReservations() {
        return Stream.of(
            Arguments.of(
                UserInfo.builder()
                    .id(EXISTING_USER_ID_1)
                    .roles(List.of("USER"))
                    .build(),
                List.of(
                    new ReservationWithEventDto(
                        EXISTING_RESERVATION_ID_1,
                        EXISTING_USER_ID_1,
                        new ReservationEventDto(
                            EXISTING_EVENT_ID_1,
                            "Spring Boot Conference 2024",
                            "Annual Spring Boot developer conference",
                            Instant.parse("2024-12-15T10:00:00Z"),
                            28800L,
                            149.99,
                            false
                        ),
                        2,
                        false
                    )
                )
            ),
            Arguments.of(
                UserInfo.builder()
                    .id(EXISTING_USER_ID_2)
                    .roles(List.of("USER"))
                    .build(),
                List.of(
                    new ReservationWithEventDto(
                        EXISTING_RESERVATION_ID_2,
                        EXISTING_USER_ID_2,
                        new ReservationEventDto(
                            EXISTING_EVENT_ID_1,
                            "Spring Boot Conference 2024",
                            "Annual Spring Boot developer conference",
                            Instant.parse("2024-12-15T10:00:00Z"),
                            28800L,
                            149.99,
                            false
                        ),
                        1,
                        false
                    )
                )
            ),
            Arguments.of(
                UserInfo.builder()
                    .id(EXISTING_USER_ID_3)
                    .roles(List.of("USER"))
                    .build(),
                List.of(
                    new ReservationWithEventDto(
                        EXISTING_RESERVATION_ID_3,
                        EXISTING_USER_ID_3,
                        new ReservationEventDto(
                            EXISTING_EVENT_ID_2,
                            "Java Microservices Workshop",
                            "Hands-on workshop with Spring Cloud",
                            Instant.parse("2024-12-20T09:00:00Z"),
                            21600L,
                            89.50,
                            false
                        ),
                        3,
                        false
                    )
                )
            ),
            Arguments.of(
                UserInfo.builder()
                    .id(EXISTING_USER_ID_4)
                    .roles(List.of("USER"))
                    .build(),
                List.of(
                    new ReservationWithEventDto(
                        EXISTING_RESERVATION_ID_4,
                        EXISTING_USER_ID_4,
                        new ReservationEventDto(
                            EXISTING_EVENT_ID_2,
                            "Java Microservices Workshop",
                            "Hands-on workshop with Spring Cloud",
                            Instant.parse("2024-12-20T09:00:00Z"),
                            21600L,
                            89.50,
                            false
                        ),
                        1,
                        false
                    )
                )
            ),
            Arguments.of(
                UserInfo.builder()
                    .id(EXISTING_USER_ID_5)
                    .roles(List.of("USER"))
                    .build(),
                List.of(
                    new ReservationWithEventDto(
                        EXISTING_RESERVATION_ID_5,
                        EXISTING_USER_ID_5,
                        new ReservationEventDto(
                            EXISTING_EVENT_ID_3,
                            "Tech Meetup: Modern Java",
                            "Monthly meetup about Java features",
                            Instant.parse("2024-12-25T18:30:00Z"),
                            7200L,
                            0.00,
                            false
                        ),
                        5,
                        false
                    )
                )
            )
        );
    }

    private static Stream<Arguments> getEventReservations() {
        return Stream.of(
            Arguments.of(
                EXISTING_EVENT_ID_1,
                List.of(
                    new ReservationDto(
                        EXISTING_RESERVATION_ID_1,
                        EXISTING_USER_ID_1,
                        EXISTING_EVENT_ID_1,
                        2,
                        false
                    ),
                    new ReservationDto(
                        EXISTING_RESERVATION_ID_2,
                        EXISTING_USER_ID_2,
                        EXISTING_EVENT_ID_1,
                        1,
                        false
                    )
                )
            ),
            Arguments.of(
                EXISTING_EVENT_ID_2,
                List.of(
                    new ReservationDto(
                        EXISTING_RESERVATION_ID_3,
                        EXISTING_USER_ID_3,
                        EXISTING_EVENT_ID_2,
                        3,
                        false
                    ),
                    new ReservationDto(
                        EXISTING_RESERVATION_ID_4,
                        EXISTING_USER_ID_4,
                        EXISTING_EVENT_ID_2,
                        1,
                        false
                    )
                )
            ),
            Arguments.of(
                EXISTING_EVENT_ID_3,
                List.of(
                    new ReservationDto(
                        EXISTING_RESERVATION_ID_5,
                        EXISTING_USER_ID_5,
                        EXISTING_EVENT_ID_3,
                        5,
                        false
                    )
                )
            )
        );
    }

    private static Stream<Arguments> getUserReservationById() {
        return Stream.of(
            Arguments.of(
                EXISTING_RESERVATION_ID_1,
                UserInfo.builder()
                    .id(EXISTING_USER_ID_1)
                    .roles(List.of("USER"))
                    .build(),
                new ReservationWithEventDto(
                    EXISTING_RESERVATION_ID_1,
                    EXISTING_USER_ID_1,
                    new ReservationEventDto(
                        EXISTING_EVENT_ID_1,
                        "Spring Boot Conference 2024",
                        "Annual Spring Boot developer conference",
                        Instant.parse("2024-12-15T10:00:00Z"),
                        28800L,
                        149.99,
                        false
                    ),
                    2,
                    false
                )
            ),
            Arguments.of(
                EXISTING_RESERVATION_ID_3,
                UserInfo.builder()
                    .id(EXISTING_USER_ID_3)
                    .roles(List.of("USER"))
                    .build(),
                new ReservationWithEventDto(
                    EXISTING_RESERVATION_ID_3,
                    EXISTING_USER_ID_3,
                    new ReservationEventDto(
                        EXISTING_EVENT_ID_2,
                        "Java Microservices Workshop",
                        "Hands-on workshop with Spring Cloud",
                        Instant.parse("2024-12-20T09:00:00Z"),
                        21600L,
                        89.50,
                        false
                    ),
                    3,
                    false
                )
            )
        );
    }

    private static Stream<Arguments> getInvalidUserReservationById() {
        UserInfo wrongUser = UserInfo.builder()
            .id(EXISTING_USER_ID_2)
            .roles(List.of("USER"))
            .build();

        return Stream.of(
            Arguments.of(EXISTING_RESERVATION_ID_1, wrongUser),
            Arguments.of(EXISTING_RESERVATION_ID_5, wrongUser)
        );
    }

    private static Stream<Arguments> getReservationsForInsert() {
        UserInfo user1 = UserInfo.builder()
            .id(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"))
            .roles(List.of("USER"))
            .build();

        CreateReservationRequestDto request1 = new CreateReservationRequestDto(
            EXISTING_EVENT_ID_3,
            2
        );
        ReservationDto expected1 = new ReservationDto(
            null,
            user1.getId(),
            EXISTING_EVENT_ID_3,
            2,
            false
        );

        UserInfo user2 = UserInfo.builder()
            .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
            .roles(List.of("USER"))
            .build();

        CreateReservationRequestDto request2 = new CreateReservationRequestDto(
            EXISTING_EVENT_ID_1,
            3
        );
        ReservationDto expected2 = new ReservationDto(
            null,
            user2.getId(),
            EXISTING_EVENT_ID_1,
            3,
            false
        );

        return Stream.of(
            Arguments.of(request1, user1, expected1),
            Arguments.of(request2, user2, expected2)
        );
    }

    private static Stream<Arguments> getReservationsForInsertWithErrors() {
        UserInfo user = UserInfo.builder()
            .id(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"))
            .roles(List.of("USER"))
            .build();

        return Stream.of(
            Arguments.of(
                new CreateReservationRequestDto(
                    UUID.fromString("12345678-90ab-cdef-1234-567890abcdf0"),
                    2
                ),
                user,
                EntityNotFoundException.class,
                "Event not found"
            ),
            Arguments.of(
                new CreateReservationRequestDto(
                    EXISTING_EVENT_ID_1,
                    1000
                ),
                user,
                NotEnoughFreeSeatsException.class,
                "Not enough available seats. Available: 197"
            ),
            Arguments.of(
                new CreateReservationRequestDto(
                    EXISTING_EVENT_ID_1,
                    2
                ),
                UserInfo.builder()
                    .id(EXISTING_USER_ID_1)
                    .roles(List.of("USER"))
                    .build(),
                RepeatedActiveReservationException.class,
                "You already have an active reservation for this event"
            )
        );
    }

    private static Stream<Arguments> getReservationsForUpdate() {
        UserInfo user1 = UserInfo.builder()
            .id(EXISTING_USER_ID_1)
            .roles(List.of("USER"))
            .build();

        UpdateReservationRequestDto request1 = new UpdateReservationRequestDto(
            5,
            true
        );
        ReservationDto expected1 = new ReservationDto(
            EXISTING_RESERVATION_ID_1,
            EXISTING_USER_ID_1,
            EXISTING_EVENT_ID_1,
            5,
            true
        );

        UserInfo user3 = UserInfo.builder()
            .id(EXISTING_USER_ID_3)
            .roles(List.of("USER"))
            .build();

        UpdateReservationRequestDto request2 = new UpdateReservationRequestDto(
            2,
            false
        );
        ReservationDto expected2 = new ReservationDto(
            EXISTING_RESERVATION_ID_3,
            EXISTING_USER_ID_3,
            EXISTING_EVENT_ID_2,
            2,
            false
        );

        return Stream.of(
            Arguments.of(EXISTING_RESERVATION_ID_1, request1, user1, expected1),
            Arguments.of(EXISTING_RESERVATION_ID_3, request2, user3, expected2)
        );
    }

    private static Stream<Arguments> getReservationsForUpdateWithErrors() {
        UserInfo user1 = UserInfo.builder()
            .id(EXISTING_USER_ID_1)
            .roles(List.of("USER"))
            .build();

        UserInfo wrongUser = UserInfo.builder()
            .id(EXISTING_USER_ID_2)
            .roles(List.of("USER"))
            .build();

        return Stream.of(
            Arguments.of(
                UUID.fromString("41000000-0000-0000-0000-0000000000f0"),
                new UpdateReservationRequestDto(1, false),
                user1,
                EntityNotFoundException.class,
                "Reservation not found with id: 41000000-0000-0000-0000-0000000000f0"
            ),
            Arguments.of(
                EXISTING_RESERVATION_ID_1,
                new UpdateReservationRequestDto(1, false),
                wrongUser,
                AccessViolationException.class,
                "Access denied"
            ),
            Arguments.of(
                EXISTING_RESERVATION_ID_1,
                new UpdateReservationRequestDto(1000, false),
                user1,
                NotEnoughFreeSeatsException.class,
                "Not enough available seats. Available: 199"
            )
        );
    }
}