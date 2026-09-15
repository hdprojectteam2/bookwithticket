package com.example.bookwithticket.delivery.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DeliveryTrackingPageController {

    @GetMapping("/delivery/tracking")
    public String trackingPage() {

        return "history/deliveryTracking";
    }
}