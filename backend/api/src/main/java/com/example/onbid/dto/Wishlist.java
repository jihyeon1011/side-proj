package com.example.onbid.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Wishlist {
    private Long id;
    private String username;
    private String propertyId;
    private LocalDateTime createdAt;
}