package vn.edu.fpt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username; // Để biết giỏ hàng của ai
    private String skuCode;  // Mã sản phẩm
    private int quantity;    // Số lượng khách chọn

    // --- CÁC TRƯỜNG DƯỚI ĐÂY LÀ @Transient (Chỉ dùng để hiển thị, không lưu vào DB) ---

    @Transient
    private String productName;

    @Transient
    private String imageUrl;

    @Transient
    private BigDecimal price;

    @Transient
    private BigDecimal totalPrice;

    // Constructor nhanh để dùng trong logic Add to Cart
    public CartItem(String username, String skuCode, int quantity) {
        this.username = username;
        this.skuCode = skuCode;
        this.quantity = quantity;
    }
}