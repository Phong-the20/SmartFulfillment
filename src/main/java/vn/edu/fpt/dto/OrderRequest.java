package vn.edu.fpt.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String orderId;
    private String customerId; // UUID dưới dạng String
    private String shippingAddress;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private String skuCode;
        private String warehouseId;
        private int quantity;
    }
}