package vn.edu.fpt.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_favorites")
@IdClass(ProductFavoriteId.class)
public class ProductFavorite {

    @Id
    @Column(name = "username", length = 20) // Phải khớp với độ dài cột username bên bảng Accounts
    private String username;

    @Id
    @Column(name = "sku_code", length = 50) // Phải khớp với độ dài cột sku_code bên bảng ProductSKU
    private String skuCode;

    private LocalDateTime createdAt = LocalDateTime.now();

    public ProductFavorite() {}
    public ProductFavorite(String username, String skuCode) {
        this.username = username;
        this.skuCode = skuCode;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}