package com.github.gserej.anonymizationtool.service.datacategory;

import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
public class CsvNameValidationService {

    private static final String NAMES_CSV_FILE_PATH = "named_entities_dataset/names.csv";
    private static final Set<String> namesSet = new HashSet<>(260_000);

    private static void setUp() {

        ClassLoader classLoader = CsvNameValidationService.class.getClassLoader();
        File csvFile = new File(Objects.requireNonNull(classLoader.getResource(NAMES_CSV_FILE_PATH)).getFile());

        try (CSVReader csvReader = new CSVReader(new FileReader(csvFile))) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line[1].equals("P-N") || line[1].equals("P-L")) {
                    namesSet.add(line[0]);
                }
            }
        } catch (IOException e) {
            log.error("Failed to open CSV file" + e);
        }
    }

    boolean isPolishFirstOrLastName(String word) {

        boolean doesStartWithUpperCase = Character.isUpperCase(word.codePointAt(0));
        if (!doesStartWithUpperCase) {
            return false;
        }

        if (namesSet.isEmpty()) {
            setUp();
        }

        return namesSet.contains(word.toLowerCase());
    }
}
