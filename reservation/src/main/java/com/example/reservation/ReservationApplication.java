package com.example.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ReservationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Reservation Service!";
    }

    @GetMapping("/")
    public String root() {
        return "Reservation Service is running!";
    }
}