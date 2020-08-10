package com.github.gserej.anonymizationtool.repositories;

import com.github.gserej.anonymizationtool.models.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentRepository extends CrudRepository<Document, UUID> {
}
