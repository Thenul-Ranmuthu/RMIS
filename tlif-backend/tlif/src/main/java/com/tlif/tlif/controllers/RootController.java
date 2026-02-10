package com.tlif.tlif.controllers;

import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;


@AllArgsConstructor
@RestController
// @RequestMapping(path = "/")
public class RootController {
    @GetMapping("/")
    public String rootGreeting() {
        return "Welcome to the root of the TLIF backend !!";
    }
    
}
