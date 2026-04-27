package vn.edu.fpt.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Product_SKU")
@Data
public class ProductSKU {
    @Id
    @Column(name = "sku_code", length = 50)
    private String skuCode;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    @Column(name = "weight_gram", nullable = false)
    private Integer weightGram;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "active")
    private boolean active = true; // Mặc định là đang kinh doanh

    @Transient
    private Integer totalQuantity;

}