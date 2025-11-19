package com.example.onbid.service;

import com.example.onbid.dto.User;
import com.example.onbid.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    
    public boolean register(String username, String password) {
        if (userMapper.existsByUsername(username)) {
            return false; // 이미 존재하는 사용자
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // 실제로는 암호화해야 함
        
        userMapper.insertUser(user);
        return true;
    }
    
    public boolean login(String username, String password) {
        User user = userMapper.findByUsername(username);
        return user != null && user.getPassword().equals(password);
    }
}