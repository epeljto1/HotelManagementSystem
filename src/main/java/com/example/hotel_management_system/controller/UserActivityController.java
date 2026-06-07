package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.model.mongo.UserLog;
import com.example.hotel_management_system.service.UserActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/activity")
@Tag(name = "User Activity", description = "Endpoints for retrieving data from both Oracle and MongoDB")
public class UserActivityController {

    private final UserActivityService userActivityService;

    public UserActivityController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @Operation(summary = "Get full user report", description = "Fetches user details from Oracle and activity logs from MongoDB")
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserReport(@PathVariable Long userId) {
        try {
            Map<String, Object> report = userActivityService.getUserFullReport(userId);
            return ResponseEntity.ok(report);
        } catch (RuntimeException e) {
            // Ovo će se desiti ako korisnik ne postoji u Oracle bazi
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching combined data: " + e.getMessage());
        }
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Create a new activity log in MongoDB")
    @PostMapping("/log")
    public ResponseEntity<UserLog> createLog(@RequestBody UserLog log) {
        try {
            UserLog savedLog = userActivityService.saveLog(log);
            return ResponseEntity.status(201).body(savedLog);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}