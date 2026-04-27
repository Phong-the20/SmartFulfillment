package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.ProductFavorite;
import vn.edu.fpt.entity.ProductFavoriteId;

import java.util.List;

@Repository
public interface ProductFavoriteRepository extends JpaRepository<ProductFavorite, ProductFavoriteId> {

    // Kiểm tra xem user có yêu thích sản phẩm này chưa
    boolean existsByUsernameAndSkuCode(String username, String skuCode);

    // Xóa một sản phẩm khỏi danh sách yêu thích
    void deleteByUsernameAndSkuCode(String username, String skuCode);

    // Lấy tất cả danh sách yêu thích của một user (để in ra trang cá nhân nếu cần)
    List<ProductFavorite> findAllByUsername(String username);
}