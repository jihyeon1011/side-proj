package com.example.onbid.service;

import com.example.onbid.dto.Wishlist;
import com.example.onbid.mapper.WishlistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class WishlistService {
    
    private final WishlistMapper wishlistMapper;
    private final PropertyService propertyService;
    
    public boolean addWishlist(String username, String propertyId) {
        if (wishlistMapper.existsWishlist(username, propertyId)) {
            return false;
        }
        
        Wishlist wishlist = new Wishlist();
        wishlist.setUsername(username);
        wishlist.setPropertyId(propertyId);
        wishlistMapper.insertWishlist(wishlist);
        return true;
    }
    
    public void removeWishlist(String username, String propertyId) {
        wishlistMapper.deleteWishlist(username, propertyId);
    }
    
    public boolean isInWishlist(String username, String propertyId) {
        return wishlistMapper.existsWishlist(username, propertyId);
    }
    
    public List<String> getUserWishlist(String username) {
        return wishlistMapper.findPropertyIdsByUsername(username);
    }
    
    public String processAddWishlist(String propertyId, String username) {
        if (username == null) {
            return "login_required";
        }
        
        boolean added = addWishlist(username, propertyId);
        return added ? "success" : "already_exists";
    }
    
    public String processRemoveWishlist(String propertyId, String username) {
        if (username == null) {
            return "login_required";
        }
        
        removeWishlist(username, propertyId);
        return "success";
    }
    
    public String processWishlistPage(org.springframework.ui.Model model, String username) {
        if (username == null) {
            return "redirect:/login";
        }
        
        List<String> wishlistIds = getUserWishlist(username);
        List<Map<String, String>> wishlistProperties = new ArrayList<>();
        
        for (String propertyId : wishlistIds) {
            Map<String, String> property = propertyService.getPropertyDetail(propertyId);
            if (!property.isEmpty()) {
                wishlistProperties.add(property);
            }
        }
        
        model.addAttribute("wishlistProperties", wishlistProperties);
        model.addAttribute("username", username);
        return "wishlist";
    }
}