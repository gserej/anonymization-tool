package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
public class RectanglesHandlingController {

    @Getter
    @Setter
    private Object rectObject;

    private MarkedRectanglesProcessingService markedRectanglesProcessingService;

    @Autowired
    public RectanglesHandlingController(MarkedRectanglesProcessingService markedRectanglesProcessingService) {
        this.markedRectanglesProcessingService = markedRectanglesProcessingService;
    }


    @PostMapping(value = "/api/rectangles")
    public String postJson(@RequestBody List<RectangleBox> rectangleBoxesMarked, RedirectAttributes redirectAttributes) {

        markedRectanglesProcessingService.processReceivedRectangleList(rectangleBoxesMarked);

        redirectAttributes.addFlashAttribute("fileReadyMessage",
                "Your file was converted, click the link below to download it.");

        return "redirect:/";
    }

    @ResponseBody
    @GetMapping("/api/rectangles")
    public Object sendRectangles() {
        return getRectObject();
    }

}
