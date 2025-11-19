package com.example.onbid.dto;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String username;
    private String password;
    private java.time.LocalDateTime createdAt;
}