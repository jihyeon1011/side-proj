package com.example.onbid.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserService userService;
    
    public String processLogin(String username, String password, HttpSession session, RedirectAttributes redirectAttributes) {
        if (userService.login(username, password)) {
            session.setAttribute("username", username);
            return "redirect:/onbid";
        } else {
            redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 잘못되었습니다.");
            return "redirect:/login";
        }
    }
    
    public String processRegister(String name, String email, String phone, String username, String password, RedirectAttributes redirectAttributes) {
        if (userService.register(name, email, phone, username, password)) {
            redirectAttributes.addFlashAttribute("success", "회원가입이 완료되었습니다.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "이미 존재하는 아이디, 이메일 또는 전화번호입니다.");
            return "redirect:/register";
        }
    }
    
    public String processLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}