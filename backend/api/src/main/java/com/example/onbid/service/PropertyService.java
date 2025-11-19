package com.example.onbid.service;

import com.example.onbid.dto.Property;
import com.example.onbid.mapper.PropertyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {
    
    private static final int MAX_BACKUP_COUNT = 5;
    
    private final PropertyMapper propertyMapper;
    private final OnbidApiService onbidApiService;
    private final XmlParserService xmlParserService;
    
    // CRUD
    public List<Property> getAllProperties() {
        return propertyMapper.findAll();
    }
    
    public void saveProperties(List<Map<String, String>> items) {
        for (Map<String, String> item : items) {
            Property property = new Property();
            property.setPropertyId(item.get("물건번호"));
            property.setPropertyName(item.get("물건명"));
            property.setAddress(item.get("물건소재지"));
            property.setDisposalMethod(item.get("처분방식"));
            property.setBidMethod(item.get("입찰방식"));
            property.setMinBidPrice(item.get("최저입찰가"));
            property.setAppraisalPrice(item.get("감정가"));
            property.setMinBidRate(item.get("최저입찰가율"));
            property.setBidStartDate(item.get("입찰시작일시"));
            property.setBidEndDate(item.get("입찰마감일시"));
            property.setStatus(item.get("물건상태"));
            property.setAnnounceCount(item.get("공고건수"));
            property.setHistoryNumbers(item.get("이력번호들"));
            property.setFailedBidCount(item.get("유찰횟수"));
            property.setViewCount(item.get("조회수"));
            property.setDetailInfo(item.get("물건상세정보"));
            
            propertyMapper.insertProperty(property);
        }
    }
    
    public int getDataCount() {
        return propertyMapper.count();
    }
    
    // 데이터 동기화
    public void refreshData() {
        log.info("데이터 새로고침 시작");
        
        String xmlData = onbidApiService.fetchOnbidData();
        List<Map<String, String>> newItems = xmlParserService.parseOnbidXmlGroupedByItem(xmlData);
        
        if (newItems.isEmpty()) {
            log.info("새로운 데이터가 없습니다.");
            return;
        }
        
        updateDataWithBackup(newItems);
        log.info("데이터 새로고침 완료. 총 {}건의 데이터가 저장되었습니다.", newItems.size());
    }
    
    private void updateDataWithBackup(List<Map<String, String>> newItems) {
        String oldDataHash = getExistingDataHash();
        String tempTableName = "property_temp_" + System.currentTimeMillis();
        
        try {
            createTempBackup(tempTableName, oldDataHash);
            replaceDataAndCheck(newItems, oldDataHash, tempTableName);
            
        } catch (Exception e) {
            cleanupTempTable(tempTableName);
            throw e;
        }
    }
    
    private String getExistingDataHash() {
        try {
            return propertyMapper.getDataHash();
        } catch (Exception e) {
            log.info("기존 데이터가 없습니다. 첫 번째 데이터 저장을 진행합니다.");
            return null;
        }
    }
    
    private void createTempBackup(String tempTableName, String oldDataHash) {
        propertyMapper.createBackupTable(tempTableName);
        if (oldDataHash != null) {
            propertyMapper.copyDataToBackup(tempTableName);
        }
    }
    
    private void replaceDataAndCheck(List<Map<String, String>> newItems, String oldDataHash, String tempTableName) {
        replaceAllData(newItems);
        
        String newDataHash = propertyMapper.getDataHash();
        
        if (oldDataHash != null && oldDataHash.equals(newDataHash)) {
            log.info("데이터 변경사항이 없습니다. 기존 데이터를 복원합니다.");
            restoreFromTemp(tempTableName);
            return;
        }
        
        if (oldDataHash != null) {
            log.info("데이터 변경이 감지되었습니다. 백업을 생성합니다.");
            createBackup();
        }
        
        propertyMapper.dropBackupTable(tempTableName);
    }
    
    private void replaceAllData(List<Map<String, String>> newItems) {
        try {
            propertyMapper.disableForeignKeyChecks();
            propertyMapper.deleteAll();
            saveProperties(newItems);
        } finally {
            propertyMapper.enableForeignKeyChecks();
        }
    }
    
    private void restoreFromTemp(String tempTableName) {
        try {
            propertyMapper.disableForeignKeyChecks();
            propertyMapper.deleteAll();
            propertyMapper.copyDataToBackup("property");
        } finally {
            propertyMapper.enableForeignKeyChecks();
        }
        propertyMapper.dropBackupTable(tempTableName);
    }
    
    private void cleanupTempTable(String tempTableName) {
        try {
            propertyMapper.dropBackupTable(tempTableName);
        } catch (Exception cleanupError) {
            log.error("임시 테이블 정리 중 오류: {}", cleanupError.getMessage());
        }
    }
    
    // 백업 관리
    private void createBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupTableName = "property_backup_" + timestamp;
            
            propertyMapper.createBackupTable(backupTableName);
            propertyMapper.copyDataToBackup(backupTableName);
            
            log.info("백업 테이블 생성 완료: {}", backupTableName);
            
            cleanupOldBackups();
            
        } catch (Exception e) {
            log.error("백업 생성 중 오류 발생: {}", e.getMessage());
        }
    }
    
    private void cleanupOldBackups() {
        try {
            List<String> backupTables = propertyMapper.getBackupTableNames();
            
            if (backupTables.size() > MAX_BACKUP_COUNT) {
                for (int i = MAX_BACKUP_COUNT; i < backupTables.size(); i++) {
                    String oldBackupTable = backupTables.get(i);
                    propertyMapper.dropBackupTable(oldBackupTable);
                    log.info("오래된 백업 테이블 삭제: {}", oldBackupTable);
                }
            }
            
        } catch (Exception e) {
            log.error("백업 정리 중 오류 발생: {}", e.getMessage());
        }
    }
    
    public List<String> getBackupList() {
        return propertyMapper.getBackupTableNames();
    }
    
    // 데이터 변환
    private Map<String, String> propertyToMap(Property property) {
        Map<String, String> map = new HashMap<>();
        map.put("물건번호", property.getPropertyId() != null ? property.getPropertyId() : "");
        map.put("물건명", property.getPropertyName() != null ? property.getPropertyName() : "");
        map.put("물건소재지", property.getAddress() != null ? property.getAddress() : "");
        map.put("처분방식", property.getDisposalMethod() != null ? property.getDisposalMethod() : "");
        map.put("입찰방식", property.getBidMethod() != null ? property.getBidMethod() : "");
        map.put("최저입찰가", property.getMinBidPrice() != null ? property.getMinBidPrice() : "");
        map.put("감정가", property.getAppraisalPrice() != null ? property.getAppraisalPrice() : "");
        map.put("최저입찰가율", property.getMinBidRate() != null ? property.getMinBidRate() : "");
        map.put("입찰시작일시", property.getBidStartDate() != null ? property.getBidStartDate() : "");
        map.put("입찰마감일시", property.getBidEndDate() != null ? property.getBidEndDate() : "");
        map.put("물건상태", property.getStatus() != null ? property.getStatus() : "");
        map.put("공고건수", property.getAnnounceCount() != null ? property.getAnnounceCount() : "");
        map.put("이력번호들", property.getHistoryNumbers() != null ? property.getHistoryNumbers() : "");
        map.put("유찰횟수", property.getFailedBidCount() != null ? property.getFailedBidCount() : "");
        map.put("조회수", property.getViewCount() != null ? property.getViewCount() : "");
        map.put("물건상세정보", property.getDetailInfo() != null ? property.getDetailInfo() : "");
        return map;
    }
    
    // 컨트롤러 위임 메서드들
    public Map<String, Object> getOnbidPageData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Property> properties = getAllProperties();
            
            if (properties.isEmpty()) {
                refreshData();
                properties = getAllProperties();
            }
            
            List<Map<String, String>> items = properties.stream()
                .map(this::propertyToMap)
                .collect(Collectors.toList());
            
            result.put("items", items);
            result.put("dataCount", getDataCount());
            
        } catch (Exception e) {
            log.error("데이터 로드 실패: {}", e.getMessage());
            result.put("items", List.of());
            result.put("dataCount", 0);
            result.put("error", "데이터 로드 실패: " + e.getMessage());
        }
        
        return result;
    }

    public String processRefresh(org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            refreshData();
            redirectAttributes.addFlashAttribute("success", "데이터 새로고침 완료");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "새로고침 실패: " + e.getMessage());
        }
        return "redirect:/onbid";
    }
    
    public String processDetail(String propertyId, org.springframework.ui.Model model) {
        Map<String, String> property = getPropertyDetail(propertyId);
        if (property.isEmpty()) {
            model.addAttribute("error", "해당 물건을 찾을 수 없습니다.");
        } else {
            model.addAttribute("property", property);
        }
        return "detail";
    }
    
    public String processOnbidPage(org.springframework.ui.Model model) {
        Map<String, Object> pageData = getOnbidPageData();
        
        model.addAttribute("items", pageData.get("items"));
        model.addAttribute("dataCount", pageData.get("dataCount"));
        if (pageData.containsKey("error")) {
            model.addAttribute("error", pageData.get("error"));
        }
        
        return "onbid";
    }
    
    public Map<String, Object> refreshDataApi() {
        try {
            refreshData();
            int count = getDataCount();
            return Map.of("success", true, "message", "데이터 새로고침 성공", "count", count);
        } catch (Exception e) {
            return Map.of("success", false, "message", "데이터 새로고침 실패: " + e.getMessage());
        }
    }
    
    public Map<String, Object> getBackupsApi() {
        try {
            List<String> backups = getBackupList();
            return Map.of("success", true, "backups", backups);
        } catch (Exception e) {
            return Map.of("success", false, "message", "백업 목록 조회 실패: " + e.getMessage());
        }
    }
    
    public Map<String, String> getPropertyDetail(String propertyId) {
        try {
            Property property = propertyMapper.findByPropertyId(propertyId);
            return property != null ? propertyToMap(property) : Map.of();
        } catch (Exception e) {
            log.error("상세 정보 조회 실패: {}", e.getMessage());
            return Map.of();
        }
    }
}