package com.example.onbid.controller;

import com.example.onbid.service.PropertyService;
import com.example.onbid.service.AuthService;
import com.example.onbid.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class OnbidController {

    private final PropertyService propertyService;
    private final AuthService authService;
    private final WishlistService wishlistService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/onbid")
    public String onbidPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        model.addAttribute("isLoggedIn", username != null);
        if (username != null) {
            model.addAttribute("userWishlist", wishlistService.getUserWishlist(username));
        }
        return propertyService.processOnbidPage(model);
    }
    
    @GetMapping("/refresh")
    public String refreshData(RedirectAttributes redirectAttributes) {
        return propertyService.processRefresh(redirectAttributes);
    }
    
    @GetMapping("/detail/{propertyId}")
    public String propertyDetail(@PathVariable String propertyId, Model model) {
        return propertyService.processDetail(propertyId, model);
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, 
                       HttpSession session, RedirectAttributes redirectAttributes) {
        return authService.processLogin(username, password, session, redirectAttributes);
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email, @RequestParam String phone,
                          @RequestParam String username, @RequestParam String password, 
                          RedirectAttributes redirectAttributes) {
        return authService.processRegister(name, email, phone, username, password, redirectAttributes);
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        return authService.processLogout(session);
    }
    
    @PostMapping("/wishlist/add")
    @ResponseBody
    public String addWishlist(@RequestParam String propertyId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        return wishlistService.processAddWishlist(propertyId, username);
    }
    
    @PostMapping("/wishlist/remove")
    @ResponseBody
    public String removeWishlist(@RequestParam String propertyId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        return wishlistService.processRemoveWishlist(propertyId, username);
    }
    
    @GetMapping("/wishlist")
    public String wishlistPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        return wishlistService.processWishlistPage(model, username);
    }

}