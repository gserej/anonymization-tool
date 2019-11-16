package com.github.gserej.anonymizationtool.datatype;

import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;


@Service
public class CsvNameExtractionServiceImpl implements CsvNameExtractionService {

    private static final String NAMES_CSV_FILE_PATH = "named_entities_dataset/names.csv";

    @Override
    public boolean isPolishFirstOrLastName(String word) {

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            File csvFile = new File(Objects.requireNonNull(classLoader.getResource(NAMES_CSV_FILE_PATH)).getFile());

            CSVReader csvReader = new CSVReader(new FileReader(csvFile));
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (word.equalsIgnoreCase(line[0])) {
                    if (Character.isUpperCase(word.codePointAt(0))) {
                        if (line[1].equals("P-N") || line[1].equals("P-L")) {
                            return true;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
