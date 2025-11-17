package com.example.api.controller;

import com.example.api.service.*;
import com.example.api.dto.Property;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Tag(name = "OnBid API", description = "경상북도 부동산 경매 정보 API")
public class ApiController {

    @Autowired
    private OnbidService onbidService;
    
    @Autowired
    private XmlParserService xmlParserService;
    
    @Autowired
    private PropertyService propertyService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("message", "Hello Thymeleaf!");
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("title", "About Page");
        return "about";
    }

    @ResponseBody
    @GetMapping("/api/hello")
    @Operation(summary = "Hello API", description = "간단한 인사 메시지를 반환합니다")
    public Map<String, String> hello() {
        return Map.of("message", "Hello from Spring Boot!");
    }

    @ResponseBody
    @PostMapping("/api/data")
    @Operation(summary = "데이터 수신", description = "클라이언트에서 보낸 데이터를 수신합니다")
    public Map<String, Object> receiveData(@RequestBody Map<String, Object> data) {
        return Map.of("received", data, "status", "success");
    }

    @ResponseBody
    @GetMapping("/api/onbid")
    @Operation(summary = "OnBid 원본 데이터", description = "OnBid API에서 가져온 원본 XML 데이터를 반환합니다")
    public String getOnbidData() {
        return onbidService.getOnbidData();
    }
    
    @ResponseBody
    @GetMapping("/api/properties")
    @Operation(summary = "경매 데이터 조회", description = "데이터베이스에 저장된 경매 데이터를 JSON 형태로 반환합니다")
    public List<Property> getProperties() {
        return propertyService.getAllProperties();
    }
    
    @ResponseBody
    @PostMapping("/api/refresh")
    @Operation(summary = "데이터 새로고침", description = "OnBid API에서 새 데이터를 가져와서 데이터베이스를 업데이트합니다")
    public Map<String, Object> refreshDataApi() {
        try {
            propertyService.refreshData();
            int count = propertyService.getDataCount();
            return Map.of("success", true, "message", "데이터 새로고침 성공", "count", count);
        } catch (Exception e) {
            return Map.of("success", false, "message", "데이터 새로고침 실패: " + e.getMessage());
        }
    }
    
    @ResponseBody
    @GetMapping("/api/backups")
    @Operation(summary = "백업 목록 조회", description = "생성된 백업 테이블 목록을 반환합니다")
    public Map<String, Object> getBackups() {
        try {
            List<String> backups = propertyService.getBackupList();
            return Map.of("success", true, "backups", backups);
        } catch (Exception e) {
            return Map.of("success", false, "message", "백업 목록 조회 실패: " + e.getMessage());
        }
    }

    @GetMapping("/onbid")
    public String onbidPage(Model model) {
        // 데이터베이스에서 데이터 조회
        List<Property> properties = propertyService.getAllProperties();
        
        if (properties.isEmpty()) {
            // 데이터가 없으면 API에서 가져와서 저장
            propertyService.refreshData();
            properties = propertyService.getAllProperties();
        }
        
        // Property 객체를 Map으로 변환
        List<Map<String, String>> items = properties.stream()
            .map(this::propertyToMap)
            .collect(Collectors.toList());
        
        model.addAttribute("items", items);
        model.addAttribute("dataCount", propertyService.getDataCount());
        return "onbid";
    }
    
    @GetMapping("/refresh")
    @Operation(summary = "웹 페이지 새로고침", description = "웹 페이지에서 데이터 새로고침 후 리다이렉트")
    public String refreshData() {
        try {
            propertyService.refreshData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/onbid";
    }
    
    private Map<String, String> propertyToMap(Property property) {
        return Map.of(
            "물건번호", property.getPropertyId() != null ? property.getPropertyId() : "",
            "물건명", property.getPropertyName() != null ? property.getPropertyName() : "",
            "물건소재지", property.getAddress() != null ? property.getAddress() : "",
            "최저입찰가", property.getMinBidPrice() != null ? property.getMinBidPrice() : "",
            "감정가", property.getAppraisalPrice() != null ? property.getAppraisalPrice() : "",
            "입찰시작일시", property.getBidStartDate() != null ? property.getBidStartDate() : "",
            "입찰마감일시", property.getBidEndDate() != null ? property.getBidEndDate() : "",
            "물건상태", property.getStatus() != null ? property.getStatus() : "",
            "공고건수", property.getAnnounceCount() != null ? property.getAnnounceCount() : "",
            "이력번호들", property.getHistoryNumbers() != null ? property.getHistoryNumbers() : ""
        );
    }
}