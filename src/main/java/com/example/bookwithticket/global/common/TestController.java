package com.example.bookwithticket.global.common;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@EnableScheduling
@Controller
public class TestController {

<<<<<<< HEAD
    @GetMapping("/")
    public String root() {
        return "cartTest";
=======
    @GetMapping("/api/health")
    public String healthCheck() {
        return "Running Successfully";
>>>>>>> 2b355d0dcf6e30160bfe72f0783497efbcf5d7c1
    }
}
