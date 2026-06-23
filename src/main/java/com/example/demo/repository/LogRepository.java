package com.example.demo.repository;

import com.example.demo.entity.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<LogEntry, String> {

    List<LogEntry> findByLevel(String level);

    List<LogEntry> findByService(String service);

    @Query("{ 'timestamp': { $gte: ?0, $lte: ?1 } }")
    List<LogEntry> findByTimeRange(LocalDateTime start, LocalDateTime end);

    @Query("{ 'message': { $regex: ?0, $options: 'i' } }")
    List<LogEntry> searchByMessage(String keyword);
}
