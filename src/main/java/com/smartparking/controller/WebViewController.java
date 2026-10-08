package com.smartparking.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebViewController {

    @GetMapping({"/", "/login", "/dashboard", "/entry-exit", "/slots", "/history", "/rates", "/employees", "/analytics", "/audit-logs", "/customer"})
    public String index() {
        return "forward:/index.html";
    }
}
