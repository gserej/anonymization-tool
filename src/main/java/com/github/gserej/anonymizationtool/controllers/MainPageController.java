package com.github.gserej.anonymizationtool.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainPageController {

    @GetMapping("/")
    public String getMainPage() {
        return "pageviewer.html";
    }
}
