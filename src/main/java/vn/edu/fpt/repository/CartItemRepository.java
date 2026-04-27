package vn.edu.fpt.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.CartItem;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByUsername(String username);
    Optional<CartItem> findByUsernameAndSkuCode(String username, String skuCode);
    void deleteByUsernameAndSkuCode(String username, String skuCode);


    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.username = ?1")
    void deleteAllByUsername(String username);
}