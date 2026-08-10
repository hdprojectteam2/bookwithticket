package com.example.bookwithticket.global.common;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableScheduling
public class TestController {

    @GetMapping("/api/health")
    public String healthCheck() {
        return "Running Successfully";
    }
}
