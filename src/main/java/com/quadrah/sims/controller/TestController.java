package com.quadrah.sims.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/students")
    public String students() {
        return "Test students endpoint works!";
    }
}