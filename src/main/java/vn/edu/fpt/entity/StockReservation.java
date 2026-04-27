package vn.edu.fpt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Stock_Reservation")
@Data
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Tự động tạo UUID
    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    @Column(name = "warehouse_id", nullable = false)
    private String warehouseId;

    @Column(name = "reserved_qty", nullable = false)
    private Integer reservedQty;

    @Column(name = "status")
    private String status = "ACTIVE"; // ACTIVE, RELEASED, FULFILLED

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}