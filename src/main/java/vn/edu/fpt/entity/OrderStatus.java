package vn.edu.fpt.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Chờ xử lý"),
    PAID("Đã thanh toán (MoMo)"),
    SHIPPING("Đang giao"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
}