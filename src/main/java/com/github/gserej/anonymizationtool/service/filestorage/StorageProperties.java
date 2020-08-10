package com.github.gserej.anonymizationtool.service.filestorage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class StorageProperties {

    @Getter
    @Setter
    private String location = "uploaded-files";

}
