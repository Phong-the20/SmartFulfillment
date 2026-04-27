package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.InventoryLevel;
import java.util.Optional;

@Repository
public interface InventoryLevelRepository extends JpaRepository<InventoryLevel, Integer> {
    // Tìm kiếm tồn kho của 1 mã hàng tại 1 kho cụ thể
    Optional<InventoryLevel> findBySkuCodeAndWarehouseId(String skuCode, String warehouseId);
}