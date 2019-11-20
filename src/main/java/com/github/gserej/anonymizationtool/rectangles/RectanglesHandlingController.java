package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.fileprocessing.FileProcessingService;
import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Slf4j
@Controller
public class RectanglesHandlingController {

    private MarkedRectanglesProcessingService markedRectanglesProcessingService;
    private FileProcessingService fileProcessingService;

    @Autowired
    public RectanglesHandlingController(MarkedRectanglesProcessingService markedRectanglesProcessingService, FileProcessingService fileProcessingService) {
        this.markedRectanglesProcessingService = markedRectanglesProcessingService;
        this.fileProcessingService = fileProcessingService;
    }


    @PostMapping(value = "/api/rectangles")
    public String postJson(@RequestBody Set<RectangleBox> rectangleBoxesMarked, RedirectAttributes redirectAttributes) {

        markedRectanglesProcessingService.processReceivedRectangleSet(rectangleBoxesMarked);

        redirectAttributes.addFlashAttribute("fileReadyMessage",
                "Your file was converted, click the link below to download it.");

        return "redirect:/";
    }

    @ResponseBody
    @GetMapping("/api/rectangles")
    public Set<RectangleBox> sendRectangles() {
        return fileProcessingService.getRectSet();
    }

}
