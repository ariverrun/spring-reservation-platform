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
import com.example.reservation.dto.CreateReservationRequestDto;
import com.example.reservation.dto.ReservationCreatedDto;
import com.example.reservation.dto.ReservationDto;
import com.example.reservation.dto.ReservationEventDto;
import com.example.reservation.dto.ReservationWithEventDto;
import com.example.reservation.dto.UpdateReservationRequestDto;
import com.example.reservation.dto.UserInfo;
import com.example.reservation.exceptions.EntityNotFoundException;
import com.example.reservation.service.JwtService;
import com.example.reservation.service.ReservationService;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private JwtService jwtService;

    private static final UUID EVENT_1_ID = UUID.fromString("01934567-89ab-7cde-8123-456789abcdef");
    private static final UUID EVENT_2_ID = UUID.fromString("01934567-89ab-7cde-8223-456789abcdef");
    private static final UUID EVENT_3_ID = UUID.fromString("01934567-89ab-7cde-8323-456789abcdef");
    private static final UUID USER_1_ID = UUID.fromString("01934567-89ab-7cde-9123-456789abcdef");
    private static final UUID USER_2_ID = UUID.fromString("01934567-89ab-7cde-9223-456789abcdef");
    private static final UUID USER_3_ID = UUID.fromString("01934567-89ab-7cde-9323-456789abcdef");
    private static final UUID USER_4_ID = UUID.fromString("01934567-89ab-7cde-9423-456789abcdef");
    private static final UUID USER_5_ID = UUID.fromString("01934567-89ab-7cde-9523-456789abcdef");
    private static final UUID RESERVATION_1_ID = UUID.fromString("01934567-89ab-7cde-a123-456789abcdef");
    private static final UUID RESERVATION_2_ID = UUID.fromString("01934567-89ab-7cde-a223-456789abcdef");
    private static final UUID RESERVATION_3_ID = UUID.fromString("01934567-89ab-7cde-a323-456789abcdef");
    private static final UUID RESERVATION_4_ID = UUID.fromString("01934567-89ab-7cde-a423-456789abcdef");
    private static final UUID RESERVATION_5_ID = UUID.fromString("01934567-89ab-7cde-a523-456789abcdef");

    @Test
    void shouldGetUserReservations() throws Exception {
        UserInfo user = getUserInfo();
        var expectedResult = getReservationWithEventDtos();
        
        setSecurityContext(user);
        
        when(reservationService.getUserReservations(user)).thenReturn(expectedResult);
        
        mockMvc.perform(get("/api/v1/reserve"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));
        
        verify(reservationService).getUserReservations(user);
    }

    @Test
    void shouldGetReservationById() throws Exception {
        UUID reservationId = RESERVATION_1_ID;
        UserInfo user = getUserInfo();
        var expectedResult = getReservationWithEventDtos().get(0);
        
        setSecurityContext(user);
        
        when(reservationService.getUserReservationById(reservationId, user)).thenReturn(expectedResult);
        
        mockMvc.perform(get("/api/v1/reserve/{reservationId}", reservationId))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)));
        
        verify(reservationService).getUserReservationById(reservationId, user);
    }

    @ParameterizedTest
    @MethodSource("getCreateReservationTestData")
    void shouldCreateReservation(CreateReservationRequestDto requestDto, ReservationDto expectedReservation, UserInfo user) throws Exception {
        when(reservationService.createReservation(requestDto, user)).thenReturn(expectedReservation);
        
        setSecurityContext(user);

        mockMvc.perform(post("/api/v1/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isCreated())
            .andExpect(content().json(objectMapper.writeValueAsString(
                new ReservationCreatedDto(expectedReservation.id())
            )));
    
        verify(reservationService).createReservation(requestDto, user);
    }

    @ParameterizedTest
    @MethodSource("getUpdateReservationTestData")
    void shouldUpdateReservation(UpdateReservationRequestDto requestDto, UUID reservationId, ReservationDto expectedReservation, UserInfo user) throws Exception {
        when(reservationService.updateReservation(reservationId, requestDto, user)).thenReturn(expectedReservation);
        
        setSecurityContext(user);

        mockMvc.perform(put("/api/v1/reserve/{id}", reservationId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(expectedReservation)));
        
        verify(reservationService).updateReservation(reservationId, requestDto, user);
    }

    @Test
    void shouldReturn404WhenReservationNotFound() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UserInfo user = getUserInfo();
        
        setSecurityContext(user);
        
        when(reservationService.getUserReservationById(reservationId, user))
            .thenThrow(new EntityNotFoundException("Reservation not found"));
        
        mockMvc.perform(get("/api/v1/reserve/{reservationId}", reservationId))
            .andExpect(status().isNotFound());
        
        verify(reservationService).getUserReservationById(reservationId, user);
    }

    @Test
    void shouldReturn500WhenRuntimeException() throws Exception {
        UUID reservationId = UUID.randomUUID();
        UserInfo user = getUserInfo();
        
        setSecurityContext(user);
        
        when(reservationService.getUserReservationById(reservationId, user))
            .thenThrow(new RuntimeException("Internal error"));
        
        mockMvc.perform(get("/api/v1/reserve/{reservationId}", reservationId))
            .andExpect(status().isInternalServerError());
        
        verify(reservationService).getUserReservationById(reservationId, user);
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenCreateWithInvalidInput() throws Exception {
        CreateReservationRequestDto invalidRequest = new CreateReservationRequestDto(
            null,
            0
        );
        
        mockMvc.perform(post("/api/v1/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenCreateWithNullEventId() throws Exception {
        CreateReservationRequestDto invalidRequest = new CreateReservationRequestDto(
            null,
            5
        );
        
        mockMvc.perform(post("/api/v1/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenCreateWithZeroSeats() throws Exception {
        CreateReservationRequestDto invalidRequest = new CreateReservationRequestDto(
            EVENT_1_ID,
            0
        );
        
        mockMvc.perform(post("/api/v1/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenCreateWithNegativeSeats() throws Exception {
        CreateReservationRequestDto invalidRequest = new CreateReservationRequestDto(
            EVENT_1_ID,
            -5
        );
        
        mockMvc.perform(post("/api/v1/reserve")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenUpdateWithInvalidInput() throws Exception {
        UpdateReservationRequestDto invalidRequest = new UpdateReservationRequestDto(
            0,
            null
        );
        UUID reservationId = RESERVATION_1_ID;
        
        mockMvc.perform(put("/api/v1/reserve/{id}", reservationId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenUpdateWithZeroSeats() throws Exception {
        UpdateReservationRequestDto invalidRequest = new UpdateReservationRequestDto(
            0,
            false
        );
        UUID reservationId = RESERVATION_1_ID;
        
        mockMvc.perform(put("/api/v1/reserve/{id}", reservationId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenUpdateWithNegativeSeats() throws Exception {
        UpdateReservationRequestDto invalidRequest = new UpdateReservationRequestDto(
            -5,
            false
        );
        UUID reservationId = RESERVATION_1_ID;
        
        mockMvc.perform(put("/api/v1/reserve/{id}", reservationId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenUpdateWithNullCanceled() throws Exception {
        UpdateReservationRequestDto invalidRequest = new UpdateReservationRequestDto(
            3,
            null
        );
        UUID reservationId = RESERVATION_1_ID;
        
        mockMvc.perform(put("/api/v1/reserve/{id}", reservationId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest());
    }

    private static UserInfo getUserInfo() {
        return UserInfo.builder()
            .id(USER_1_ID)
            .roles(List.of("USER"))
            .build();
    }

    private static List<ReservationWithEventDto> getReservationWithEventDtos() {
        return List.of(
            new ReservationWithEventDto(
                RESERVATION_1_ID,
                USER_1_ID,
                new ReservationEventDto(
                    EVENT_1_ID,
                    "Spring Boot Conference 2024",
                    "Annual Spring Boot developer conference",
                    Instant.parse("2024-12-15T10:00:00Z"),
                    28800L,
                    149.99,
                    false
                ),
                2,
                false
            ),
            new ReservationWithEventDto(
                RESERVATION_2_ID,
                USER_2_ID,
                new ReservationEventDto(
                    EVENT_1_ID,
                    "Spring Boot Conference 2024",
                    "Annual Spring Boot developer conference",
                    Instant.parse("2024-12-15T10:00:00Z"),
                    28800L,
                    149.99,
                    false
                ),
                1,
                false
            ),
            new ReservationWithEventDto(
                RESERVATION_3_ID,
                USER_3_ID,
                new ReservationEventDto(
                    EVENT_2_ID,
                    "Java Microservices Workshop",
                    "Hands-on workshop with Spring Cloud",
                    Instant.parse("2024-12-20T09:00:00Z"),
                    21600L,
                    89.50,
                    false
                ),
                3,
                false
            ),
            new ReservationWithEventDto(
                RESERVATION_4_ID,
                USER_4_ID,
                new ReservationEventDto(
                    EVENT_2_ID,
                    "Java Microservices Workshop",
                    "Hands-on workshop with Spring Cloud",
                    Instant.parse("2024-12-20T09:00:00Z"),
                    21600L,
                    89.50,
                    false
                ),
                1,
                false
            ),
            new ReservationWithEventDto(
                RESERVATION_5_ID,
                USER_5_ID,
                new ReservationEventDto(
                    EVENT_3_ID,
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
        );
    }

    private static List<Object[]> getCreateReservationTestData() {
        return List.of(
            new Object[] {
                new CreateReservationRequestDto(
                    EVENT_1_ID,
                    2
                ),
                new ReservationDto(
                    RESERVATION_1_ID,
                    USER_1_ID,
                    EVENT_1_ID,
                    2,
                    false
                ),
                getUserInfo()
            },
            new Object[] {
                new CreateReservationRequestDto(
                    EVENT_2_ID,
                    5
                ),
                new ReservationDto(
                    UUID.randomUUID(),
                    USER_1_ID,
                    EVENT_2_ID,
                    5,
                    false
                ),
                getUserInfo()
            }
        );
    }

    private static List<Object[]> getUpdateReservationTestData() {
        return List.of(
            new Object[] {
                new UpdateReservationRequestDto(
                    3,
                    false
                ),
                RESERVATION_1_ID,
                new ReservationDto(
                    RESERVATION_1_ID,
                    USER_1_ID,
                    EVENT_1_ID,
                    3,
                    false
                ),
                getUserInfo()
            },
            new Object[] {
                new UpdateReservationRequestDto(
                    1,
                    true
                ),
                RESERVATION_2_ID,
                new ReservationDto(
                    RESERVATION_2_ID,
                    USER_2_ID,
                    EVENT_1_ID,
                    1,
                    true
                ),
                getUserInfo()
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