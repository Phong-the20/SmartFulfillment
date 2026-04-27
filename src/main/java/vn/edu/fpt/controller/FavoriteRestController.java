package vn.edu.fpt.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.entity.ProductFavorite;
import vn.edu.fpt.repository.ProductFavoriteRepository;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteRestController {

    @Autowired
    private ProductFavoriteRepository favoriteRepo;

    // API: POST /api/favorite/toggle?sku=...
    @PostMapping("/toggle")
    @Transactional // Rất quan trọng để deleteBy chạy được
    public ResponseEntity<Map<String, Object>> toggleFavorite(@RequestParam String sku, Authentication auth) {
        Map<String, Object> response = new HashMap<>();

        // 1. Kiểm tra đăng nhập (An toàn bảo mật)
        if (auth == null || !auth.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập để thực hiện chức năng này.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String username = auth.getName(); // Lấy username từ Spring Security

        // 2. Logic "Bật/Tắt" (Toggle)
        if (favoriteRepo.existsByUsernameAndSkuCode(username, sku)) {
            // Nếu đã thích rồi -> XÓA
            favoriteRepo.deleteByUsernameAndSkuCode(username, sku);
            response.put("favorited", false);
            response.put("message", "Đã xóa khỏi danh sách yêu thích.");
        } else {
            // Nếu chưa thích -> THÊM MỚI
            ProductFavorite favorite = new ProductFavorite(username, sku);
            favoriteRepo.save(favorite);
            response.put("favorited", true);
            response.put("message", "Đã thêm vào danh sách yêu thích.");
        }

        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}