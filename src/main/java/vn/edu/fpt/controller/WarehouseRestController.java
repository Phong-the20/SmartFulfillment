package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.fpt.entity.Warehouse;
import vn.edu.fpt.repository.WarehouseRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseRestController {

    @Autowired
    private WarehouseRepository warehouseRepo;

    // 1. Lấy danh sách tất cả kho để vẽ Marker lên bản đồ
    // Trong file WarehouseRestController.java
    @GetMapping
    public List<Map<String, Object>> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseRepo.findAll();

        return warehouses.stream().map(wh -> {
            Map<String, Object> map = new HashMap<>();
            map.put("warehouseId", wh.getWarehouseId());
            map.put("name", wh.getName());
            map.put("address", wh.getAddress());
            map.put("latitude", wh.getLatitude());
            map.put("longitude", wh.getLongitude());

            long totalQuantity = wh.getInventories().stream()
                    .mapToLong(vn.edu.fpt.entity.Inventory::getQuantity).sum();

            // LOGIC 3 MÀU MỚI
            String color;
            if (totalQuantity == 0) {
                color = "#dc3545"; // Đỏ - Hết hàng
            } else if (totalQuantity <= 50) {
                color = "#ffc107"; // Vàng - Sắp hết (Cảnh báo)
            } else {
                color = "#28a745"; // Xanh - An toàn
            }

            map.put("statusColor", color);
            map.put("totalQuantity", totalQuantity); // Gửi thêm số lượng để hiện tooltip nếu cần
            return map;
        }).collect(Collectors.toList());
    }

    // 2. Lấy tồn kho chi tiết để hiển thị lên Sidebar khi click vào Marker
    @GetMapping("/{id}/inventory")
    public List<Map<String, Object>> getWarehouseInventory(@PathVariable Integer id) {
        Warehouse wh = warehouseRepo.findById(id).orElse(null);
        if (wh == null) return new ArrayList<>();

        // Trả về Map giúp JSON gọn nhẹ và không bị lỗi vòng lặp vô tận
        return wh.getInventories().stream().map(inv -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productName", inv.getProduct().getProductName());
            map.put("sku", inv.getProduct().getSkuCode());
            map.put("quantity", inv.getQuantity());
            return map;
        }).collect(Collectors.toList());
    }

    @GetMapping("/stats")
    public Map<String, Object> getDashboardStats() {
        List<Warehouse> warehouses = warehouseRepo.findAll();

        // 1. Tổng số kho
        long totalWarehouses = warehouses.size();

        // 2. Đếm số kho đang báo động (Tổng tồn kho của kho đó = 0)
        long outOfStockCount = warehouses.stream()
                .filter(wh -> wh.getInventories().stream()
                        .mapToLong(vn.edu.fpt.entity.Inventory::getQuantity).sum() == 0)
                .count();

        // 3. Tính tổng tồn kho toàn hệ thống (Cộng dồn tất cả sản phẩm ở tất cả các kho)
        long totalItems = warehouses.stream()
                .flatMap(wh -> wh.getInventories().stream())
                .mapToLong(vn.edu.fpt.entity.Inventory::getQuantity)
                .sum();

        // Đóng gói dữ liệu gửi về cho Frontend
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalWarehouses", totalWarehouses);
        stats.put("outOfStockCount", outOfStockCount);
        stats.put("totalItems", totalItems);

        return stats;
    }
}