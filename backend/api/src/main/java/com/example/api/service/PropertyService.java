package com.example.api.service;

import com.example.api.dto.Property;
import com.example.api.mapper.PropertyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PropertyService {
    
    private static final Logger logger = LoggerFactory.getLogger(PropertyService.class);
    private static final int MAX_BACKUP_COUNT = 5;
    
    @Autowired
    private PropertyMapper propertyMapper;
    
    @Autowired
    private OnbidService onbidService;
    
    @Autowired
    private XmlParserService xmlParserService;
    
    public List<Property> getAllProperties() {
        return propertyMapper.findAll();
    }
    
    public void saveProperties(List<Map<String, String>> items) {
        for (Map<String, String> item : items) {
            Property property = new Property();
            property.setPropertyId(item.get("물건번호"));
            property.setPropertyName(item.get("물건명"));
            property.setAddress(item.get("물건소재지"));
            property.setMinBidPrice(item.get("최저입찰가"));
            property.setAppraisalPrice(item.get("감정가"));
            property.setBidStartDate(item.get("입찰시작일시"));
            property.setBidEndDate(item.get("입찰마감일시"));
            property.setStatus(item.get("물건상태"));
            property.setAnnounceCount(item.get("공고건수"));
            property.setHistoryNumbers(item.get("이력번호들"));
            
            propertyMapper.insertProperty(property);
        }
    }
    
    public void refreshData() {
        logger.info("데이터 새로고침 시작");
        
        // 1. 새 데이터 가져오기
        String xmlData = onbidService.getOnbidData();
        List<Map<String, String>> newItems = xmlParserService.parseOnbidXmlGroupedByItem(xmlData);
        
        if (newItems.isEmpty()) {
            logger.info("새로운 데이터가 없습니다.");
            return;
        }
        
        // 2. 기존 데이터 해시값 계산
        String oldDataHash = null;
        try {
            oldDataHash = propertyMapper.getDataHash();
        } catch (Exception e) {
            logger.info("기존 데이터가 없습니다. 첫 번째 데이터 저장을 진행합니다.");
        }
        
        // 3. 임시로 새 데이터 저장해서 해시값 비교
        String tempTableName = "property_temp_" + System.currentTimeMillis();
        try {
            // 임시 테이블 생성 및 새 데이터 저장
            propertyMapper.createBackupTable(tempTableName);
            
            // 기존 데이터를 임시로 백업
            if (oldDataHash != null) {
                propertyMapper.copyDataToBackup(tempTableName);
            }
            
            // 새 데이터로 교체
            try {
                propertyMapper.disableForeignKeyChecks();
                propertyMapper.deleteAll();
            } finally {
                propertyMapper.enableForeignKeyChecks();
            }
            saveProperties(newItems);
            
            // 새 데이터 해시값 계산
            String newDataHash = propertyMapper.getDataHash();
            
            // 4. 데이터 변경 여부 확인
            if (oldDataHash != null && oldDataHash.equals(newDataHash)) {
                logger.info("데이터 변경사항이 없습니다. 기존 데이터를 복원합니다.");
                // 기존 데이터 복원
                try {
                    propertyMapper.disableForeignKeyChecks();
                    propertyMapper.deleteAll();
                } finally {
                    propertyMapper.enableForeignKeyChecks();
                }
                propertyMapper.copyDataToBackup("property");
                propertyMapper.dropBackupTable(tempTableName);
                return;
            }
            
            // 5. 데이터가 변경된 경우 백업 처리
            if (oldDataHash != null) {
                logger.info("데이터 변경이 감지되었습니다. 백업을 생성합니다.");
                createBackup();
            }
            
            // 임시 테이블 삭제
            propertyMapper.dropBackupTable(tempTableName);
            
            logger.info("데이터 새로고침 완료. 총 {}건의 데이터가 저장되었습니다.", newItems.size());
            
        } catch (Exception e) {
            logger.error("데이터 새로고침 중 오류 발생: {}", e.getMessage());
            // 오류 발생 시 임시 테이블 정리
            try {
                propertyMapper.dropBackupTable(tempTableName);
            } catch (Exception cleanupError) {
                logger.error("임시 테이블 정리 중 오류: {}", cleanupError.getMessage());
            }
            throw e;
        }
    }
    
    private void createBackup() {
        try {
            // 현재 시간으로 백업 테이블명 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupTableName = "property_backup_" + timestamp;
            
            // 백업 테이블 생성 및 데이터 복사
            propertyMapper.createBackupTable(backupTableName);
            propertyMapper.copyDataToBackup(backupTableName);
            
            logger.info("백업 테이블 생성 완료: {}", backupTableName);
            
            // 백업 개수 제한 (최대 5개)
            cleanupOldBackups();
            
        } catch (Exception e) {
            logger.error("백업 생성 중 오류 발생: {}", e.getMessage());
        }
    }
    
    private void cleanupOldBackups() {
        try {
            List<String> backupTables = propertyMapper.getBackupTableNames();
            
            if (backupTables.size() > MAX_BACKUP_COUNT) {
                // 오래된 백업부터 삭제 (최신 5개만 유지)
                for (int i = MAX_BACKUP_COUNT; i < backupTables.size(); i++) {
                    String oldBackupTable = backupTables.get(i);
                    propertyMapper.dropBackupTable(oldBackupTable);
                    logger.info("오래된 백업 테이블 삭제: {}", oldBackupTable);
                }
            }
            
        } catch (Exception e) {
            logger.error("백업 정리 중 오류 발생: {}", e.getMessage());
        }
    }
    
    public int getDataCount() {
        return propertyMapper.count();
    }
    
    public List<String> getBackupList() {
        return propertyMapper.getBackupTableNames();
    }
}