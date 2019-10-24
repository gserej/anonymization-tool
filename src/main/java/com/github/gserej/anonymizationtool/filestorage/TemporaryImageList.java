package com.github.gserej.anonymizationtool.filestorage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TemporaryImageList {

    @Getter
    @Setter
    public static List<String> tempImagesList = new ArrayList<>();


}
