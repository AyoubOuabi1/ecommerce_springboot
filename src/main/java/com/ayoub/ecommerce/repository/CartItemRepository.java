package com.ayoub.ecommerce.repository;

import com.ayoub.ecommerce.Models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart_Id(Long cartId);
    void deleteByCart_Id(Long cartId);
}