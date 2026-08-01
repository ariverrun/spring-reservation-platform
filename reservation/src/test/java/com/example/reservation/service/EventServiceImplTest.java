package com.example.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.reservation.dto.CreateEventRequestDto;
import com.example.reservation.dto.EventDto;
import com.example.reservation.dto.UpdateEventRequestDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.entity.Event;
import com.example.reservation.exceptions.EntityNotFoundException;
import com.example.reservation.repository.EventRepository;
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
@Import(EventServiceImpl.class)
class EventServiceImplTest {

    @Autowired
    private EventServiceImpl eventService;

    @Autowired
    private EventRepository eventRepository;

    private static final UUID EXISTING_USER_ID = UUID.fromString("abcdef12-3456-7890-abcd-ef1234567890");
    private static final UUID EXISTING_EVENT_ID_1 = UUID.fromString("12345678-90ab-cdef-1234-567890abcdef");
    private static final UUID EXISTING_EVENT_ID_2 = UUID.fromString("22345678-90ab-cdef-1234-567890abcdef");
    private static final UUID EXISTING_EVENT_ID_3 = UUID.fromString("32345678-90ab-cdef-1234-567890abcdef");

    @ParameterizedTest
    @MethodSource("getDbEvents")
    void shouldFindAllEvents(List<EventDto> expectedEvents) {
        List<EventDto> actualEvents = eventService.getAllEvents();
        assertThat(actualEvents)
            .usingRecursiveComparison()
            .isEqualTo(expectedEvents);
    }

    @ParameterizedTest
    @MethodSource("getDbEventsById")
    void shouldFindEventById(UUID eventId, EventDto expectedEvent) {
        EventDto actualEvent = eventService.getEventById(eventId);
        assertThat(actualEvent)
            .usingRecursiveComparison()
            .isEqualTo(expectedEvent);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "12345678-90ab-cdef-1234-567890abcdf0",
        "12345678-90ab-cdef-1234-567890abcdf1"
    })
    void shouldThrowExceptionWhenEventNotFound(UUID nonExistentId) {
        assertThatThrownBy(() -> eventService.getEventById(nonExistentId))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Event not found with id: " + nonExistentId);
    }

    @ParameterizedTest
    @MethodSource("getEventsForInsert")
    @Transactional
    @Rollback
    void shouldCreateEvent(CreateEventRequestDto request, UserInfo user, EventDto expectedEvent) {
        EventDto createdEvent = eventService.createEvent(request, user);
        assertThat(createdEvent)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(expectedEvent);
        assertThat(createdEvent.id()).isNotNull();
        
        Event savedEvent = eventRepository.findById(createdEvent.id()).orElse(null);
        assertThat(savedEvent).isNotNull();
        assertThat(savedEvent.getName()).isEqualTo(request.name());
        assertThat(savedEvent.getUserId()).isEqualTo(user.getId());
    }

    @ParameterizedTest
    @MethodSource("getEventsForUpdate")
    @Transactional
    @Rollback
    void shouldUpdateEvent(UUID eventId, UpdateEventRequestDto request, EventDto expectedEvent) {
        EventDto updatedEvent = eventService.updateEvent(eventId, request);
        assertThat(updatedEvent)
            .usingRecursiveComparison()
            .isEqualTo(expectedEvent);

        Event dbEvent = eventRepository.findById(eventId).orElse(null);
        assertThat(dbEvent).isNotNull();
        assertThat(dbEvent.getName()).isEqualTo(request.name());
        assertThat(dbEvent.getDescription()).isEqualTo(request.description());
        assertThat(dbEvent.getStartTime()).isEqualTo(request.startTime());
        assertThat(dbEvent.getDurationSeconds()).isEqualTo(request.durationSeconds());
        assertThat(dbEvent.getTicketPrice()).isEqualTo(request.ticketPrice());
        assertThat(dbEvent.getTotalSeats()).isEqualTo(request.totalSeats());
        assertThat(dbEvent.getIsCanceled()).isEqualTo(request.isCanceled());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "12345678-90ab-cdef-1234-567890abcdf0",
        "12345678-90ab-cdef-1234-567890abcdf1"
    })
    @Transactional
    @Rollback
    void shouldThrowExceptionOnUpdateWhenEventNotFound(UUID nonExistentId) {
        UpdateEventRequestDto request = new UpdateEventRequestDto(
            "Updated Event",
            "Updated Description",
            Instant.now().plusSeconds(2592000),
            7200L,
            200.00,
            150,
            false
        );

        assertThatThrownBy(() -> eventService.updateEvent(nonExistentId, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessage("Event not found with id: " + nonExistentId);
    }

    @ParameterizedTest
    @MethodSource("getUserEvents")
    void shouldGetUserEvents(UserInfo user, List<EventDto> expectedEvents) {
        List<EventDto> actualEvents = eventService.getUserEvents(user);
        assertThat(actualEvents)
            .usingRecursiveComparison()
            .isEqualTo(expectedEvents);
    }

    @ParameterizedTest
    @MethodSource("getUserEventsEmpty")
    void shouldReturnEmptyListWhenUserHasNoEvents(UserInfo user) {
        List<EventDto> actualEvents = eventService.getUserEvents(user);
        assertThat(actualEvents).isEmpty();
    }

    private static Stream<Arguments> getDbEvents() {
        return Stream.of(
            Arguments.of(
                List.of(
                    new EventDto(
                        EXISTING_EVENT_ID_1,
                        EXISTING_USER_ID,
                        "Spring Boot Conference 2024",
                        "Annual Spring Boot developer conference",
                        Instant.parse("2024-12-15T10:00:00Z"),
                        28800L,
                        149.99,
                        200,
                        false
                    ),
                    new EventDto(
                        EXISTING_EVENT_ID_2,
                        EXISTING_USER_ID,
                        "Java Microservices Workshop",
                        "Hands-on workshop with Spring Cloud",
                        Instant.parse("2024-12-20T09:00:00Z"),
                        21600L,
                        89.50,
                        50,
                        false
                    ),
                    new EventDto(
                        EXISTING_EVENT_ID_3,
                        EXISTING_USER_ID,
                        "Tech Meetup: Modern Java",
                        "Monthly meetup about Java features",
                        Instant.parse("2024-12-25T18:30:00Z"),
                        7200L,
                        0.00,
                        100,
                        false
                    )
                )
            )
        );
    }

    private static Stream<Arguments> getDbEventsById() {
        return Stream.of(
            Arguments.of(
                EXISTING_EVENT_ID_1,
                new EventDto(
                    EXISTING_EVENT_ID_1,
                    EXISTING_USER_ID,
                    "Spring Boot Conference 2024",
                    "Annual Spring Boot developer conference",
                    Instant.parse("2024-12-15T10:00:00Z"),
                    28800L,
                    149.99,
                    200,
                    false
                )
            ),
            Arguments.of(
                EXISTING_EVENT_ID_2,
                new EventDto(
                    EXISTING_EVENT_ID_2,
                    EXISTING_USER_ID,
                    "Java Microservices Workshop",
                    "Hands-on workshop with Spring Cloud",
                    Instant.parse("2024-12-20T09:00:00Z"),
                    21600L,
                    89.50,
                    50,
                    false
                )
            ),
            Arguments.of(
                EXISTING_EVENT_ID_3,
                new EventDto(
                    EXISTING_EVENT_ID_3,
                    EXISTING_USER_ID,
                    "Tech Meetup: Modern Java",
                    "Monthly meetup about Java features",
                    Instant.parse("2024-12-25T18:30:00Z"),
                    7200L,
                    0.00,
                    100,
                    false
                )
            )
        );
    }

    private static Stream<Arguments> getEventsForInsert() {
        UserInfo user = UserInfo.builder()
            .id(UUID.fromString("abcdef12-3456-7890-abcd-ef1234567890"))
            .roles(List.of("USER"))
            .build();

        Instant startTime1 = Instant.parse("2025-01-15T10:00:00Z");
        CreateEventRequestDto request1 = new CreateEventRequestDto(
            "New Conference 2025",
            "Annual tech conference",
            startTime1,
            28800L,
            199.99,
            300
        );
        EventDto expected1 = new EventDto(
            null,
            user.getId(),
            "New Conference 2025",
            "Annual tech conference",
            startTime1,
            28800L,
            199.99,
            300,
            false
        );

        Instant startTime2 = Instant.parse("2025-02-20T09:00:00Z");
        CreateEventRequestDto request2 = new CreateEventRequestDto(
            "Spring Boot Workshop",
            "Hands-on workshop",
            startTime2,
            21600L,
            149.50,
            50
        );
        EventDto expected2 = new EventDto(
            null,
            user.getId(),
            "Spring Boot Workshop",
            "Hands-on workshop",
            startTime2,
            21600L,
            149.50,
            50,
            false
        );

        return Stream.of(
            Arguments.of(request1, user, expected1),
            Arguments.of(request2, user, expected2)
        );
    }

    private static Stream<Arguments> getEventsForUpdate() {
        Instant newStartTime1 = Instant.parse("2024-12-20T14:00:00Z");
        UpdateEventRequestDto request1 = new UpdateEventRequestDto(
            "Updated Spring Boot Conference 2024",
            "Updated annual conference description",
            newStartTime1,
            32400L,
            189.99,
            250,
            true
        );
        EventDto expected1 = new EventDto(
            EXISTING_EVENT_ID_1,
            EXISTING_USER_ID,
            "Updated Spring Boot Conference 2024",
            "Updated annual conference description",
            newStartTime1,
            32400L,
            189.99,
            250,
            true
        );

        Instant newStartTime2 = Instant.parse("2024-12-22T10:00:00Z");
        UpdateEventRequestDto request2 = new UpdateEventRequestDto(
            "Updated Java Microservices Workshop",
            "Updated workshop description with new content",
            newStartTime2,
            25200L,
            99.50,
            60,
            false
        );
        EventDto expected2 = new EventDto(
            EXISTING_EVENT_ID_2,
            EXISTING_USER_ID,
            "Updated Java Microservices Workshop",
            "Updated workshop description with new content",
            newStartTime2,
            25200L,
            99.50,
            60,
            false
        );

        return Stream.of(
            Arguments.of(EXISTING_EVENT_ID_1, request1, expected1),
            Arguments.of(EXISTING_EVENT_ID_2, request2, expected2)
        );
    }

    private static Stream<Arguments> getUserEvents() {
        UserInfo user = UserInfo.builder()
            .id(EXISTING_USER_ID)
            .roles(List.of("USER"))
            .build();

        return Stream.of(
            Arguments.of(
                user,
                List.of(
                    new EventDto(
                        EXISTING_EVENT_ID_1,
                        EXISTING_USER_ID,
                        "Spring Boot Conference 2024",
                        "Annual Spring Boot developer conference",
                        Instant.parse("2024-12-15T10:00:00Z"),
                        28800L,
                        149.99,
                        200,
                        false
                    ),
                    new EventDto(
                        EXISTING_EVENT_ID_2,
                        EXISTING_USER_ID,
                        "Java Microservices Workshop",
                        "Hands-on workshop with Spring Cloud",
                        Instant.parse("2024-12-20T09:00:00Z"),
                        21600L,
                        89.50,
                        50,
                        false
                    ),
                    new EventDto(
                        EXISTING_EVENT_ID_3,
                        EXISTING_USER_ID,
                        "Tech Meetup: Modern Java",
                        "Monthly meetup about Java features",
                        Instant.parse("2024-12-25T18:30:00Z"),
                        7200L,
                        0.00,
                        100,
                        false
                    )
                )
            )
        );
    }

    private static Stream<Arguments> getUserEventsEmpty() {
        UserInfo user = UserInfo.builder()
            .id(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"))
            .roles(List.of("USER"))
            .build();

        return Stream.of(
            Arguments.of(user)
        );
    }    
}