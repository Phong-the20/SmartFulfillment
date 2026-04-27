package vn.edu.fpt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Voucher {
    @Id
    private String voucherCode; // Ví dụ: SUMMER2026
    @Column(name = "description", columnDefinition = "nvarchar(255)")
    private String description;
    private BigDecimal discountValue;
    private String discountType; // "PERCENT" hoặc "FIXED"

    private BigDecimal minOrderAmount; // Điều kiện đơn tối thiểu
    private Integer usageLimit; // Tổng lượt dùng
    private Integer usedCount = 0; // Đã dùng bao nhiêu

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active = true;

}