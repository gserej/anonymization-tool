package com.github.gserej.anonymizationtool.controllers;

import com.github.gserej.anonymizationtool.model.RectangleBox;
import com.github.gserej.anonymizationtool.services.MarkedRectanglesProcessingService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@RestController
public class RectanglesHandlingController {

    @Getter
    @Setter
    private Object rectObject;

    private MarkedRectanglesProcessingService markedRectanglesProcessingService;

    @Autowired
    public RectanglesHandlingController(MarkedRectanglesProcessingService markedRectanglesProcessingService) {
        this.markedRectanglesProcessingService = markedRectanglesProcessingService;
    }

    @PostMapping(value = "/api")
    public String postJson(@RequestBody List<RectangleBox> rectangleBoxesMarked, RedirectAttributes redirectAttributes) {

        markedRectanglesProcessingService.processReceivedRectangleList(rectangleBoxesMarked);

        redirectAttributes.addFlashAttribute("message",
                "Your file was converted, click the link below to download it.");

        return "redirect:/";
    }

    @GetMapping(value = {"/originalrectangles", "/additionalrectangles"})
    public Object sendRectangles() {
        return getRectObject();
    }

}
