package com.github.gserej.anonymizationtool;

import com.github.gserej.anonymizationtool.storage.StorageProperties;
import com.github.gserej.anonymizationtool.storage.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
@Slf4j
public class AnonymizationToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnonymizationToolApplication.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }

}
