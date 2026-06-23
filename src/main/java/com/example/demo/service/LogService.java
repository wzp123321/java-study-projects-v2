package com.example.demo.service;

import com.example.demo.entity.LogEntry;
import com.example.demo.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.middleware.mongo.enabled", havingValue = "true", matchIfMissing = true)
public class LogService {

    private final LogRepository logRepository;

    public LogEntry saveLog(LogEntry logEntry) {
        if (logEntry.getTimestamp() == null) {
            logEntry.setTimestamp(LocalDateTime.now());
        }
        return logRepository.save(logEntry);
    }

    public List<LogEntry> getAllLogs() {
        return logRepository.findAll();
    }

    public List<LogEntry> getLogsByLevel(String level) {
        return logRepository.findByLevel(level);
    }

    public List<LogEntry> getLogsByService(String service) {
        return logRepository.findByService(service);
    }

    public List<LogEntry> searchLogsByMessage(String keyword) {
        return logRepository.searchByMessage(keyword);
    }

    public void deleteLog(String id) {
        logRepository.deleteById(id);
    }
}
