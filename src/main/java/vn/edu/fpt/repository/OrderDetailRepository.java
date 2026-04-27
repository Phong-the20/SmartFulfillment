package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.OrderDetail;
import vn.edu.fpt.entity.OrderStatus;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    // Hàm này dùng để kiểm tra sản phẩm đã có trong đơn hàng nào chưa
    boolean existsByProduct_SkuCode(String skuCode);

    @Query("SELECT d.product, SUM(d.quantity) as totalQty " +
            "FROM OrderDetail d " +
            "GROUP BY d.product " +
            "ORDER BY SUM(d.quantity) DESC")
    List<Object[]> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);

    // 🚀 HÀM MỚI: Dùng email và truyền thẳng Enum OrderStatus vào cho chắc ăn
    @Query("SELECT COUNT(d) FROM OrderDetail d WHERE d.order.email = ?1 AND d.product.skuCode = ?2 AND d.order.status = ?3")
    int countPurchased(String email, String skuCode, OrderStatus status);

}