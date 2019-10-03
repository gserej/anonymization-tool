package com.github.gserej.anonymizationtool;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
class RectangleBoxLists {


    @Getter
    @Setter
    static List<RectangleBox> rectangleBoxListOriginal = new ArrayList<>();

    @Getter
    @Setter
    static List<RectangleBox> rectangleBoxListParsed = new ArrayList<>();

    @Getter
    @Setter
    static List<RectangleBox> rectangleBoxListMarked = new ArrayList<>();

}

