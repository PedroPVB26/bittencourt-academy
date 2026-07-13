package dev.pedrobittencourt.bittencourt_academy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @GetMapping
    public String test(){
        return "test";
    }
}
