package com.example.onbid.service;

import com.example.onbid.dto.User;
import com.example.onbid.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    
    public boolean register(String name, String email, String phone, String username, String password) {
        // 중복 체크
        if (userMapper.existsByUsername(username)) {
            return false; // 이미 존재하는 사용자명
        }
        
        if (userMapper.existsByEmail(email)) {
            return false; // 이미 존재하는 이메일
        }
        
        if (userMapper.existsByPhone(phone)) {
            return false; // 이미 존재하는 전화번호
        }
        
        // 입력값 검증
        if (name == null || name.trim().isEmpty() ||
            username == null || username.trim().isEmpty() || 
            email == null || email.trim().isEmpty() || 
            phone == null || phone.trim().isEmpty() ||
            password == null || password.length() < 4) {
            return false; // 유효하지 않은 입력
        }
        
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setPhone(phone.trim());
        user.setUsername(username.trim());
        user.setPassword(password); // TODO: 암호화 필요
        
        userMapper.insertUser(user);
        return true;
    }
    
    public boolean login(String username, String password) {
        User user = userMapper.findByUsername(username);
        return user != null && user.getPassword().equals(password);
    }
}