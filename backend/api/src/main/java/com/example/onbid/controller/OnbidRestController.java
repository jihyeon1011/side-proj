package com.example.onbid.controller;

import com.example.onbid.service.*;
import com.example.onbid.dto.Property;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "OnBid API", description = "경상북도 부동산 경매 정보 API")
public class OnbidRestController {

    private final OnbidApiService onbidApiService;
    private final PropertyService propertyService;

    @GetMapping("/onbid")
    @Operation(summary = "OnBid 원본 데이터", description = "OnBid API에서 가져온 원본 XML 데이터를 반환합니다")
    public String getOnbidData() {
        return onbidApiService.fetchOnbidData();
    }
    
    @GetMapping("/properties")
    @Operation(summary = "경매 데이터 조회", description = "데이터베이스에 저장된 경매 데이터를 JSON 형태로 반환합니다")
    public List<Property> getProperties() {
        return propertyService.getAllProperties();
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "데이터 새로고침", description = "OnBid API에서 새 데이터를 가져와서 데이터베이스를 업데이트합니다")
    public Map<String, Object> refreshData() {
        return propertyService.refreshDataApi();
    }
    
    @GetMapping("/backups")
    @Operation(summary = "백업 목록 조회", description = "생성된 백업 테이블 목록을 반환합니다")
    public Map<String, Object> getBackups() {
        return propertyService.getBackupsApi();
    }
}