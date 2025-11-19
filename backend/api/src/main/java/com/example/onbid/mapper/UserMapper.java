package com.example.onbid.mapper;

import com.example.onbid.dto.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    void insertUser(User user);
    User findByUsername(String username);
    boolean existsByUsername(String username);
}