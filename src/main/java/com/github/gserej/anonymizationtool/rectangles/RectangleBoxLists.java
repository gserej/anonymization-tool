package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RectangleBoxLists {

    @Getter
    @Setter
    private List<RectangleBox> rectangleBoxListOriginal = new ArrayList<>();

    @Getter
    @Setter
    private List<RectangleBox> rectangleBoxListMarked = new ArrayList<>();

    public void addRectangle(RectangleBox rectangleBox) {
        this.rectangleBoxListOriginal.add(rectangleBox);
    }
}

