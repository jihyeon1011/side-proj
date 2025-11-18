package com.example.onbid.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Property {
    private String propertyId;
    private String propertyName;
    private String address;
    private String minBidPrice;
    private String appraisalPrice;
    private String bidStartDate;
    private String bidEndDate;
    private String status;
    private String announceCount;
    private String historyNumbers;
    private LocalDateTime lastUpdated;
}