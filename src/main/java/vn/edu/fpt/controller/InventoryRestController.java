package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.entity.Inventory;
import vn.edu.fpt.repository.InventoryRepository;
import vn.edu.fpt.repository.ProductSKURepository;
import vn.edu.fpt.repository.WarehouseRepository;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
public class InventoryRestController {

    @Autowired
    private InventoryRepository inventoryRepo;
    @Autowired
    private WarehouseRepository warehouseRepo;
    @Autowired
    private ProductSKURepository productRepo;

    @PostMapping("/import")
    public ResponseEntity<?> importStock(@RequestBody Map<String, Object> payload) {
        Integer warehouseId = Integer.parseInt(payload.get("warehouseId").toString());
        String skuCode = payload.get("skuCode").toString();
        Integer quantity = Integer.parseInt(payload.get("quantity").toString());

        // Logic xử lý: Nếu có rồi thì cộng dồn, chưa có thì tạo mới
        Optional<Inventory> invOpt = inventoryRepo.findByWarehouseAndSku(warehouseId, skuCode);

        Inventory inventory;
        if (invOpt.isPresent()) {
            inventory = invOpt.get();
            inventory.setQuantity(inventory.getQuantity() + quantity);
        } else {
            inventory = new Inventory();
            inventory.setWarehouse(warehouseRepo.findById(warehouseId).get());
            inventory.setProduct(productRepo.findById(skuCode).get());
            inventory.setQuantity(quantity);
        }

        inventoryRepo.save(inventory);
        return ResponseEntity.ok("Nhập hàng thành công!");
    }
}