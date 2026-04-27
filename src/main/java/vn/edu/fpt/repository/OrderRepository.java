package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    // 1. Hàm này ông đang có (Dùng cho khách xem lịch sử của họ)
    List<Order> findByEmailOrderByOrderDateDesc(String email);

    // 2. THÊM DÒNG NÀY VÀO ĐÂY (Dùng cho Admin xem toàn bộ đơn hàng, xếp cái mới lên đầu)
    List<Order> findAllByOrderByOrderDateDesc();

    @Query("SELECT CAST(o.orderDate AS date) as date, SUM(o.totalAmount) as amount " +
            "FROM Order o " +
            "WHERE o.status IN (vn.edu.fpt.entity.OrderStatus.PAID, vn.edu.fpt.entity.OrderStatus.COMPLETED) " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(o.orderDate AS date) " +
            "ORDER BY CAST(o.orderDate AS date) ASC")
    List<Object[]> getRevenueByRange(@Param("startDate") java.time.LocalDateTime startDate,
                                     @Param("endDate") java.time.LocalDateTime endDate);

}