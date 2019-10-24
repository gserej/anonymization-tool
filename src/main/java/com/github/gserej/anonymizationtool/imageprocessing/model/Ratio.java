package com.github.gserej.anonymizationtool.imageprocessing.model;

import lombok.Getter;

public class Ratio {
    @Getter
    private static float ratio;


    public Ratio(float ratio) {
        Ratio.ratio = ratio;
    }
}
