package com.github.gserej.anonymizationtool.model;

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
    public static List<RectangleBox> rectangleBoxListOriginal = new ArrayList<>();

    @Getter
    @Setter
    public static List<RectangleBox> rectangleBoxListParsed = new ArrayList<>();

    @Getter
    @Setter
    public static List<RectangleBox> rectangleBoxListMarked = new ArrayList<>();

}

