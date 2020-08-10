package com.github.gserej.anonymizationtool.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@RedisHash("DocumentMetaInfo")
public class Document {

    @Id
    private UUID uuid;

    private String documentName;

    private List<String> imageList;

    private float imageRatio;

    private String currentMessage;

    private Set<RectangleBox> originalRectangles;
    private Set<RectangleBox> parsedRectangles;
    private Set<RectangleBox> markedRectangles;

    public Document(UUID uuid) {
        this.uuid = uuid;
    }
}
