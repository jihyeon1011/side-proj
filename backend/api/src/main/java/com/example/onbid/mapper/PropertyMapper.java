package com.example.onbid.mapper;

import com.example.onbid.dto.Property;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PropertyMapper {
    void insertProperty(Property property);
    List<Property> findAll();
    void deleteAll();
    int count();
    
    // 백업 관련 메서드
    void createBackupTable(String backupTableName);
    void copyDataToBackup(String backupTableName);
    void dropBackupTable(String backupTableName);
    List<String> getBackupTableNames();
    String getDataHash();
    
    // 외래키 제어
    void disableForeignKeyChecks();
    void enableForeignKeyChecks();
}