package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.InventoryLevel;
import vn.edu.fpt.entity.StockReservation;
import vn.edu.fpt.exception.OutOfStockException;
import vn.edu.fpt.repository.InventoryLevelRepository;
import vn.edu.fpt.repository.StockReservationRepository;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    @Autowired
    private InventoryLevelRepository inventoryRepo;

    @Autowired
    private StockReservationRepository reservationRepo;

    /**
     * LOGIC 1: GIỮ HÀNG (RESERVE)
     * Chạy khi khách nhấn "Thanh toán"
     */
    @Transactional
    public void reserveStock(String orderId, String skuCode, String warehouseId, int quantity) {
        // 1. Tìm tồn kho
        InventoryLevel inventory = inventoryRepo.findBySkuCodeAndWarehouseId(skuCode, warehouseId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại trong kho này!"));

        // 2. Kiểm tra tồn kho khả dụng (Available Qty)
        if (inventory.getAvailableQty() < quantity) {
            throw new OutOfStockException("Rất tiếc, sản phẩm " + skuCode + " chỉ còn " + inventory.getAvailableQty() + " món.");
        }

        // 3. Trừ số lượng khả dụng
        inventory.setAvailableQty(inventory.getAvailableQty() - quantity);
        inventoryRepo.save(inventory);

        // 4. Lưu nhật ký giữ hàng vào bảng Stock_Reservation
        StockReservation reservation = new StockReservation();
        reservation.setOrderId(orderId);
        reservation.setSkuCode(skuCode);
        reservation.setWarehouseId(warehouseId);
        reservation.setReservedQty(quantity);
        reservation.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // Hết hạn sau 30p

        reservationRepo.save(reservation);
    }

    /**
     * LOGIC 2: NHẢ HÀNG (RELEASE)
     * Chạy khi khách hủy đơn hoặc hết hạn thanh toán
     */
    @Transactional
    public void releaseStock(String orderId, String skuCode) {
        // 1. Tìm thông tin đã giữ chỗ
        StockReservation res = reservationRepo.findByOrderIdAndSkuCode(orderId, skuCode);

        if (res != null && "ACTIVE".equals(res.getStatus())) {
            // 2. Tìm tồn kho để cộng trả lại
            InventoryLevel inventory = inventoryRepo.findBySkuCodeAndWarehouseId(skuCode, res.getWarehouseId())
                    .orElseThrow(() -> new RuntimeException("Lỗi hệ thống khi hoàn hàng!"));

            // 3. Cộng lại vào Available Qty
            inventory.setAvailableQty(inventory.getAvailableQty() + res.getReservedQty());
            inventoryRepo.save(inventory);

            // 4. Cập nhật trạng thái giữ hàng thành RELEASED
            res.setStatus("RELEASED");
            reservationRepo.save(res);
        }
    }
}