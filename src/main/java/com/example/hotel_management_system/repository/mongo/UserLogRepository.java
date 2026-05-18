package com.example.hotel_management_system.repository.mongo;

import com.example.hotel_management_system.model.mongo.UserLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface UserLogRepository extends MongoRepository<UserLog, String> {
    // Spring će sam generisati upit koji traži logove po userId
    List<UserLog> findByUserId(Long userId);
}