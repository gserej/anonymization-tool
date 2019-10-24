package com.github.gserej.anonymizationtool.model;

import lombok.Getter;

public class Ratio {
    @Getter
    private static float ratio;


    public Ratio(float ratio) {
        Ratio.ratio = ratio;
    }
}
