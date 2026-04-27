package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.dto.OrderRequest;
import vn.edu.fpt.service.InventoryService;
import vn.edu.fpt.service.OrderService;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private OrderService orderService;

    // API để test thử tính năng giữ hàng
    // Ví dụ: /api/v1/inventory/reserve?orderId=ORD001&sku=IPHONE-15-PRO-256&warehouseId=WH-HCM&qty=1
    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestParam String orderId,
                                          @RequestParam String sku,
                                          @RequestParam String warehouseId,
                                          @RequestParam int qty) {
        try {
            inventoryService.reserveStock(orderId, sku, warehouseId, qty);
            return ResponseEntity.ok("Giữ hàng thành công cho đơn: " + orderId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/place-order")
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request) {
        try {
            orderService.placeOrder(request);
            return ResponseEntity.ok("Đặt hàng và Giữ hàng thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}