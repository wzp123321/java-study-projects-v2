package com.example.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntry {

    @Id
    private String id;

    @Indexed
    private String level;

    @Indexed
    private String service;

    private String message;

    private Object metadata;

    private LocalDateTime timestamp;
}
