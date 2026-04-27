package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 1. Kiểm tra xem User này đã đánh giá sản phẩm này chưa (Trả về true/false)
    boolean existsByUsernameAndSkuCode(String username, String skuCode);

    // 2. Đếm số lần 1 user đã đánh giá 1 sản phẩm
    int countByUsernameAndSkuCode(String username, String skuCode);
    int countByEmailAndSkuCode(String email, String skuCode);

    // 3. Lấy danh sách đánh giá của 1 sản phẩm (Sắp xếp mới nhất lên đầu)
    List<Review> findBySkuCodeOrderByCreatedAtDesc(String skuCode);
}