package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.StockReservation;
import java.util.UUID;

@Repository
public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {
    // Dùng để tìm lại dòng giữ hàng khi khách hàng hủy đơn
    StockReservation findByOrderIdAndSkuCode(String orderId, String skuCode);
}