package com.github.gserej.anonymizationtool.rectangles;

import com.github.gserej.anonymizationtool.rectangles.model.RectangleBox;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class RectangleBoxSets {

    @Getter
    @Setter
    private Set<RectangleBox> rectangleBoxSetOriginal = new HashSet<>();

    @Getter
    @Setter
    private Set<RectangleBox> rectangleBoxSetMarked = new HashSet<>();

    public void addRectangle(RectangleBox rectangleBox) {
        this.rectangleBoxSetOriginal.add(rectangleBox);
    }
}

