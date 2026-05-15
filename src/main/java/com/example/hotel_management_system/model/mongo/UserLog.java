package com.example.hotel_management_system.model.mongo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Document(collection = "user_activity")
public class UserLog {
    @Id
    private String id;

    @Field("userId")
    private Long userId;

    private String action;
    private LocalDateTime timestamp;
    private String details;
}