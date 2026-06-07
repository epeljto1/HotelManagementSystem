package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.model.User;
import com.example.hotel_management_system.model.mongo.UserLog;
import com.example.hotel_management_system.repository.UserRepository;
import com.example.hotel_management_system.repository.mongo.UserLogRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserActivityService {
    private final UserRepository userRepository;
    private final UserLogRepository userLogRepository;
    private final MongoTemplate mongoTemplate;

    public UserActivityService(UserRepository userRepository, UserLogRepository userLogRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.userLogRepository = userLogRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Map<String, Object> getUserFullReport(Long userId) throws SQLException {

        System.out.println("Spring je povezan na MongoDB bazu: " + mongoTemplate.getDb().getName());
        Map<String, Object> report = new HashMap<>();

        try (Connection conn = DbConfig.getConnection()) {
            User user = userRepository.findById(userId, conn)
                    .orElseThrow(() -> new RuntimeException("User not found in Oracle"));
            report.put("userDetails", user);
        }

        List<UserLog> logs = userLogRepository.findByUserId(userId);
        System.out.println("Pronadjeno logova u Mongu: " + logs.size());
        report.put("activityLogs", logs);

        return report;
    }

    public UserLog saveLog(UserLog log) {
        if (log.getTimestamp() == null) {
            log.setTimestamp(java.time.LocalDateTime.now());
        }
        return userLogRepository.save(log);
    }
}