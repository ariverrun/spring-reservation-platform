package com.example.reservation.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
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
import com.example.reservation.service.ReservationService;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private ReservationService reservationService;

    @Test
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
        when(eventService.createEvent(requestDto, user)).thenReturn(expectedEvent);

        mockMvc.perform(post("/api/v1/event")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(content().json(objectMapper.writeValueAsString(
                new EventCreatedDto(expectedEvent.id())
            )));
    
        verify(eventService).createEvent(requestDto, user);
    }

    @ParameterizedTest
    @MethodSource("getUpdateEventTestData")
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
    void shouldReturn404WhenEventNotFound() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(eventService.getEventById(eventId)).thenThrow(new EntityNotFoundException("Event not found"));
        
        mockMvc.perform(get("/api/v1/event/{id}", eventId))
            .andExpect(status().isNotFound());
        
        verify(eventService).getEventById(eventId);
    }

    @Test
    void shouldReturn500WhenRuntimeException() throws Exception {
        UUID eventId = UUID.randomUUID();
        when(eventService.getEventById(eventId)).thenThrow(new RuntimeException("Internal error"));
        
        mockMvc.perform(get("/api/v1/event/{id}", eventId))
            .andExpect(status().isInternalServerError());
        
        verify(eventService).getEventById(eventId);
    }

    @Test
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
    void shouldGetEventReservations() throws Exception {
        UUID eventId = UUID.fromString("1234567890abcdef1234567890abcdef");
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
                UUID.fromString("1234567890abcdef1234567890abcdef"),
                UUID.fromString("abcdef1234567890abcdef1234567890"),
                "Spring Boot Conference 2024",
                "Annual Spring Boot developer conference",
                Instant.parse("2024-12-15T10:00:00Z"),
                28800L,
                149.99,
                200,
                false
            ),
            new EventDto(
                UUID.fromString("2234567890abcdef1234567890abcdef"),
                UUID.fromString("abcdef1234567890abcdef1234567890"),
                "Java Microservices Workshop",
                "Hands-on workshop with Spring Cloud",
                Instant.parse("2024-12-20T09:00:00Z"),
                21600L,
                89.50,
                50,
                false
            ),
            new EventDto(
                UUID.fromString("3234567890abcdef1234567890abcdef"),
                UUID.fromString("abcdef1234567890abcdef1234567890"),
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
                UUID.fromString("41000000000000000000000000000001"),
                UUID.fromString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"),
                UUID.fromString("1234567890abcdef1234567890abcdef"),
                2,
                false
            ),
            new ReservationDto(
                UUID.fromString("41000000000000000000000000000002"),
                UUID.fromString("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb"),
                UUID.fromString("1234567890abcdef1234567890abcdef"),
                1,
                false
            ),
            new ReservationDto(
                UUID.fromString("41000000000000000000000000000003"),
                UUID.fromString("cccccccccccccccccccccccccccccccc"),
                UUID.fromString("2234567890abcdef1234567890abcdef"),
                3,
                false
            ),
            new ReservationDto(
                UUID.fromString("41000000000000000000000000000004"),
                UUID.fromString("dddddddddddddddddddddddddddddddd"),
                UUID.fromString("2234567890abcdef1234567890abcdef"),
                1,
                false
            ),
            new ReservationDto(
                UUID.fromString("41000000000000000000000000000005"),
                UUID.fromString("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"),
                UUID.fromString("3234567890abcdef1234567890abcdef"),
                5,
                false
            )
        );
    }

    private static List<EventDto> getCreateEventTestData() {
        return List.of(
            new EventDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "New Event 1",
                "Description 1",
                Instant.now().plusSeconds(86400),
                7200L,
                50.0,
                100,
                false
            ),
            new EventDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "New Event 2",
                "Description 2",
                Instant.now().plusSeconds(172800),
                10800L,
                75.5,
                150,
                false
            )
        );
    }

    private static List<EventDto> getUpdateEventTestData() {
        return List.of(
            new EventDto(
                UUID.fromString("1234567890abcdef1234567890abcdef"),
                UUID.fromString("abcdef1234567890abcdef1234567890"),
                "Updated Event 1",
                "Updated Description 1",
                Instant.parse("2025-01-15T10:00:00Z"),
                28800L,
                199.99,
                200,
                false
            ),
            new EventDto(
                UUID.fromString("2234567890abcdef1234567890abcdef"),
                UUID.fromString("abcdef1234567890abcdef1234567890"),
                "Updated Event 2",
                "Updated Description 2",
                Instant.parse("2025-02-20T09:00:00Z"),
                21600L,
                99.50,
                50,
                true
            )
        );
    }
}