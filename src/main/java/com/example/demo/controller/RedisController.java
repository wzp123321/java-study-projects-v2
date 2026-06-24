package com.example.demo.controller;

import com.example.demo.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.middleware.redis.enabled", havingValue = "true")
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/set")
    public ResponseEntity<String> setValue(@RequestParam String key, @RequestParam String value) {
        redisService.set(key, value);
        return ResponseEntity.ok("Set successfully");
    }

    @PostMapping("/set-with-ttl")
    public ResponseEntity<String> setValueWithTtl(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam long seconds) {
        redisService.set(key, value, seconds, TimeUnit.SECONDS);
        return ResponseEntity.ok("Set with TTL successfully");
    }

    @GetMapping("/get")
    public ResponseEntity<Object> getValue(@RequestParam String key) {
        return ResponseEntity.ok(redisService.get(key));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Boolean> deleteKey(@RequestParam String key) {
        return ResponseEntity.ok(redisService.delete(key));
    }

    @GetMapping("/has-key")
    public ResponseEntity<Boolean> hasKey(@RequestParam String key) {
        return ResponseEntity.ok(redisService.hasKey(key));
    }

    @PostMapping("/hash")
    public ResponseEntity<String> setHash(
            @RequestParam String key,
            @RequestParam String hashKey,
            @RequestParam String value) {
        redisService.setHash(key, hashKey, value);
        return ResponseEntity.ok("Hash set successfully");
    }

    @GetMapping("/hash")
    public ResponseEntity<Object> getHash(
            @RequestParam String key,
            @RequestParam String hashKey) {
        return ResponseEntity.ok(redisService.getHash(key, hashKey));
    }

    @PostMapping("/list")
    public ResponseEntity<String> setList(@RequestParam String key, @RequestParam String value) {
        redisService.setList(key, value);
        return ResponseEntity.ok("List item added successfully");
    }

    @GetMapping("/list")
    public ResponseEntity<Object> getList(@RequestParam String key, @RequestParam long index) {
        return ResponseEntity.ok(redisService.getList(key, index));
    }

    @PostMapping("/increment")
    public ResponseEntity<Long> increment(@RequestParam String key, @RequestParam long delta) {
        return ResponseEntity.ok(redisService.increment(key, delta));
    }
}
