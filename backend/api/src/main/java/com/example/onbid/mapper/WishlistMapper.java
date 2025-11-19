package com.example.onbid.mapper;

import com.example.onbid.dto.Wishlist;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface WishlistMapper {
    void insertWishlist(Wishlist wishlist);
    void deleteWishlist(String username, String propertyId);
    boolean existsWishlist(String username, String propertyId);
    List<String> findPropertyIdsByUsername(String username);
}