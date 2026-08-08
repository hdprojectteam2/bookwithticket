package com.example.bookwithticket.global.common;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@EnableScheduling
@Controller
public class TestController {

    @GetMapping("/")
    public String root() {
        return "cartTest";
    }
}
