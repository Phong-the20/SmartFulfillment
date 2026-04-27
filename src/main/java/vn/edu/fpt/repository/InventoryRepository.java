package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Inventory;
import vn.edu.fpt.entity.ProductSKU;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {

    void deleteByProduct(ProductSKU product);

    @Query("SELECT i FROM Inventory i WHERE i.warehouse.warehouseId = :warehouseId AND i.product.skuCode = :sku")
    Optional<Inventory> findByWarehouseAndSku(@Param("warehouseId") Integer warehouseId, @Param("sku") String sku);

    // FIX LỖI: Tìm theo mã SKU (truy cập vào thuộc tính skuCode của Product)
    List<Inventory> findByProduct_SkuCode(String skuCode);

    // Hoặc tìm theo đối tượng Product (ông đã có dòng này rồi)
    List<Inventory> findByProduct(ProductSKU product);

    @Query("SELECT SUM(i.quantity) FROM Inventory i WHERE i.product.skuCode = :skuCode")
    Integer sumQuantityBySkuCode(@Param("skuCode") String skuCode);
}