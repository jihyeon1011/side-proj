package com.example.onbid.controller;

import com.example.onbid.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class OnbidController {

    private final PropertyService propertyService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/onbid")
    public String onbidPage(Model model) {
        Map<String, Object> pageData = propertyService.getOnbidPageData();
        
        model.addAttribute("items", pageData.get("items"));
        model.addAttribute("dataCount", pageData.get("dataCount"));
        if (pageData.containsKey("error")) {
            model.addAttribute("error", pageData.get("error"));
        }
        
        return "onbid";
    }
    
    @GetMapping("/refresh")
    public String refreshData(RedirectAttributes redirectAttributes) {
        try {
            propertyService.refreshData();
            redirectAttributes.addFlashAttribute("success", "데이터 새로고침 완료");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "새로고침 실패: " + e.getMessage());
        }
        return "redirect:/onbid";
    }
    

}