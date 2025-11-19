package com.example.onbid.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Property {
    private String propertyId;
    private String propertyName;
    private String address;
    private String disposalMethod;
    private String bidMethod;
    private String minBidPrice;
    private String appraisalPrice;
    private String minBidRate;
    private String bidStartDate;
    private String bidEndDate;
    private String status;
    private String announceCount;
    private String historyNumbers;
    private String failedBidCount;
    private String viewCount;
    private String detailInfo;
    private LocalDateTime lastUpdated;
}