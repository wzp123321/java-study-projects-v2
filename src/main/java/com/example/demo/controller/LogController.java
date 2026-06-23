package com.example.demo.controller;

import com.example.demo.entity.LogEntry;
import com.example.demo.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.middleware.mongo.enabled", havingValue = "true", matchIfMissing = true)
public class LogController {

    private final LogService logService;

    @PostMapping
    public ResponseEntity<LogEntry> createLog(@RequestBody LogEntry logEntry) {
        return ResponseEntity.ok(logService.saveLog(logEntry));
    }

    @GetMapping
    public ResponseEntity<List<LogEntry>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<List<LogEntry>> getLogsByLevel(@PathVariable String level) {
        return ResponseEntity.ok(logService.getLogsByLevel(level));
    }

    @GetMapping("/service/{service}")
    public ResponseEntity<List<LogEntry>> getLogsByService(@PathVariable String service) {
        return ResponseEntity.ok(logService.getLogsByService(service));
    }

    @GetMapping("/search")
    public ResponseEntity<List<LogEntry>> searchLogs(@RequestParam String keyword) {
        return ResponseEntity.ok(logService.searchLogsByMessage(keyword));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable String id) {
        logService.deleteLog(id);
        return ResponseEntity.ok().build();
    }
}
