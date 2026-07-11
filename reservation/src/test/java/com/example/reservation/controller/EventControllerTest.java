package com.example.reservation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.reservation.dto.CreateEventRequestDto;
import com.example.reservation.dto.EventCreatedDto;
import com.example.reservation.dto.EventDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.UpdateEventRequestDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.exceptions.EntityNotFoundException;
import com.example.reservation.service.EventService;
import com.example.reservation.service.JwtService;
import com.example.reservation.service.ReservationService;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private JwtService jwtService;

    private static final UUID EVENT_1_ID = UUID.fromString("01934567-89ab-7cde-8123-456789abcdef");
    private static final UUID EVENT_2_ID = UUID.fromString("01934567-89ab-7cde-8223-456789abcdef");
    private static final UUID EVENT_3_ID = UUID.fromString("01934567-89ab-7cde-8323-456789abcdef");
    private static final UUID USER_1_ID = UUID.fromString("01934567-89ab-7cde-9123-456789abcdef");
    private static final UUID RESERVATION_1_ID = UUID.fromString("01934567-89ab-7cde-a123-456789abcdef");
    private static final UUID RESERVATION_2_ID = UUID.fromString("01934567-89ab-7cde-a223-456789abcdef");
    private static final UUID RESERVATION_3_ID = UUID.fromString("01934567-89ab-7cde-a323-456789abcdef");
    private static final UUID RESERVATION_4_ID = UUID.fromString("01934567-89ab-7cde-a423-456789abcdef");
    private static final UUID RESERVATION_5_ID = UUID.fromString("01934567-89ab-7cde-a523-456789abcdef");
    private static final UUID USER_RES_1_ID = UUID.fromString("01934567-89ab-7cde-9223-456789abcdef");
    private static final UUID USER_RES_2_ID = UUID.fromString("01934567-89ab-7cde-9323-456789abcdef");
    private static final UUID USER_RES_3_ID = UUID.fromString("01934567-89ab-7cde-9423-456789abcdef");
    private static final UUID USER_RES_4_ID = UUID.fromString("01934567-89ab-7cde-9523-456789abcdef");
    private static final UUID USER_RES_5_ID = UUID.fromString("01934567-89ab-7cde-9623-456789abcdef");

    @Test
    @WithMockUser
    void shouldListAllEvents() throws Exception {
        var expectedResult = getDbEventDtos();
        
        when(eventService.getAllEvents()).thenReturn(expectedResult);
        
        mockMvc.perform(get("/api/v1/event"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));
        
        verify(eventService).getAllEvents();
    }

    @ParameterizedTest
    @MethodSource("getDbEventDtos")
    @WithMockUser
    void shouldGetEventById(EventDto expectedResult) throws Exception {
        when(eventService.getEventById(expectedResult.id())).thenReturn(expectedResult);
        
        mockMvc.perform(get("/api/v1/event/{id}", expectedResult.id()))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));
        
        verify(eventService).getEventById(expectedResult.id());
    }

    @ParameterizedTest
    @MethodSource("getCreateEventTestData")
    void shouldCreateEvent(CreateEventRequestDto requestDto, EventDto expectedEvent, UserInfo user) throws Exception {
        when(eventService.createEvent(eq(requestDto), any(UserInfo.class))).thenReturn(expectedEvent);
        
        setSecurityContext(user);

        mockMvc.perform(post("/api/v1/event")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(content().json(objectMapper.writeValueAsString(
                new EventCreatedDto(expectedEvent.id())
            )));

        verify(eventService).createEvent(eq(requestDto), any(UserInfo.class));
    }

    @ParameterizedTest
    @MethodSource("getUpdateEventTestData")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateEvent(UpdateEventRequestDto requestDto, UUID eventId, EventDto expectedEvent) throws Exception {
        when(eventService.updateEvent(eventId, requestDto)).thenReturn(expectedEvent);
        
        mockMvc.perform(put("/api/v1/event/{id}", eventId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedEvent)));
        
        verify(eventService).updateEvent(eventId, requestDto);
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenEventNotFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(eventService.getEventById(eventId)).thenThrow(new EntityNotFoundException("Event not found"));
        
        mockMvc.perform(get("/api/v1/event/{id}", eventId))
            .andExpect(status().isNotFound());
        
        verify(eventService).getEventById(eventId);
    }

    @Test
    @WithMockUser
    void shouldReturn500WhenRuntimeException() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(eventService.getEventById(eventId)).thenThrow(new RuntimeException("Internal error"));
        
        mockMvc.perform(get("/api/v1/event/{id}", eventId))
            .andExpect(status().isInternalServerError());
        
        verify(eventService).getEventById(eventId);
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenInvalidInput() throws Exception {
        CreateEventRequestDto invalidRequest = new CreateEventRequestDto(
            "",
            "",
            null,
            null,
            null,
            null
        );
        
        mockMvc.perform(post("/api/v1/event")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenUpdateWithInvalidInput() throws Exception {
        UpdateEventRequestDto invalidRequest = new UpdateEventRequestDto(
            "",
            "",
            null,
            null,
            null,
            null,
            false
        );
        UUID eventId = UUID.randomUUID();
        
        mockMvc.perform(put("/api/v1/event/{id}", eventId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldGetEventReservations() throws Exception {
        UUID eventId = EVENT_1_ID;
        var expectedResult = getDbReservationDtos();
        
        when(reservationService.getEventReservations(eventId)).thenReturn(expectedResult);
        
        mockMvc.perform(get("/api/v1/event/{id}/reserve", eventId))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));
        
        verify(reservationService).getEventReservations(eventId);
    }

    private static List<EventDto> getDbEventDtos() {
        return List.of(
            new EventDto(
                EVENT_1_ID,
                USER_1_ID,
                "Spring Boot Conference 2024",
                "Annual Spring Boot developer conference",
                Instant.parse("2024-12-15T10:00:00Z"),
                28800L,
                149.99,
                200,
                false
            ),
            new EventDto(
                EVENT_2_ID,
                USER_1_ID,
                "Java Microservices Workshop",
                "Hands-on workshop with Spring Cloud",
                Instant.parse("2024-12-20T09:00:00Z"),
                21600L,
                89.50,
                50,
                false
            ),
            new EventDto(
                EVENT_3_ID,
                USER_1_ID,
                "Tech Meetup: Modern Java",
                "Monthly meetup about Java features",
                Instant.parse("2024-12-25T18:30:00Z"),
                7200L,
                0.00,
                100,
                false
            )
        );
    }

    private static List<ReservationDto> getDbReservationDtos() {
        return List.of(
            new ReservationDto(
                RESERVATION_1_ID,
                USER_RES_1_ID,
                EVENT_1_ID,
                2,
                false
            ),
            new ReservationDto(
                RESERVATION_2_ID,
                USER_RES_2_ID,
                EVENT_1_ID,
                1,
                false
            ),
            new ReservationDto(
                RESERVATION_3_ID,
                USER_RES_3_ID,
                EVENT_2_ID,
                3,
                false
            ),
            new ReservationDto(
                RESERVATION_4_ID,
                USER_RES_4_ID,
                EVENT_2_ID,
                1,
                false
            ),
            new ReservationDto(
                RESERVATION_5_ID,
                USER_RES_5_ID,
                EVENT_3_ID,
                5,
                false
            )
        );
    }

    private static List<Object[]> getCreateEventTestData() {
        UserInfo adminUser = UserInfo.builder()
            .id(USER_1_ID)
            .roles(List.of("ADMIN"))
            .build();
            
        return List.of(
            new Object[]{
                new CreateEventRequestDto(
                    "New Event 1",
                    "Description 1",
                    Instant.now().plusSeconds(86400),
                    7200L,
                    50.0,
                    100
                ),
                new EventDto(
                    UUID.randomUUID(),
                    USER_1_ID,
                    "New Event 1",
                    "Description 1",
                    Instant.now().plusSeconds(86400),
                    7200L,
                    50.0,
                    100,
                    false
                ),
                adminUser
            },
            new Object[]{
                new CreateEventRequestDto(
                    "New Event 2",
                    "Description 2",
                    Instant.now().plusSeconds(172800),
                    10800L,
                    75.5,
                    150
                ),
                new EventDto(
                    UUID.randomUUID(),
                    USER_1_ID,
                    "New Event 2",
                    "Description 2",
                    Instant.now().plusSeconds(172800),
                    10800L,
                    75.5,
                    150,
                    false
                ),
                adminUser
            }
        );
    }

    private static List<Object[]> getUpdateEventTestData() {
        return List.of(
            new Object[]{
                new UpdateEventRequestDto(
                    "Updated Event 1",
                    "Updated Description 1",
                    Instant.parse("2025-01-15T10:00:00Z"),
                    28800L,
                    199.99,
                    200,
                    false
                ),
                EVENT_1_ID,
                new EventDto(
                    EVENT_1_ID,
                    USER_1_ID,
                    "Updated Event 1",
                    "Updated Description 1",
                    Instant.parse("2025-01-15T10:00:00Z"),
                    28800L,
                    199.99,
                    200,
                    false
                )
            },
            new Object[]{
                new UpdateEventRequestDto(
                    "Updated Event 2",
                    "Updated Description 2",
                    Instant.parse("2025-02-20T09:00:00Z"),
                    21600L,
                    99.50,
                    50,
                    true
                ),
                EVENT_2_ID,
                new EventDto(
                    EVENT_2_ID,
                    USER_1_ID,
                    "Updated Event 2",
                    "Updated Description 2",
                    Instant.parse("2025-02-20T09:00:00Z"),
                    21600L,
                    99.50,
                    50,
                    true
                )
            }
        );
    }

    private void setSecurityContext(UserInfo user) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(user, null,
                user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList()
            )
        );
    }
}