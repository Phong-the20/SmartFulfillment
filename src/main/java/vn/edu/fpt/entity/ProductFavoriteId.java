package vn.edu.fpt.entity;
import java.io.Serializable;
import java.util.Objects;
// Class này dùng để định nghĩa khóa cho bảng ProductFavorite
public class ProductFavoriteId implements Serializable {
    private String username;
    private String skuCode;
    public ProductFavoriteId() {}
    public ProductFavoriteId(String username, String skuCode) {
        this.username = username;
        this.skuCode = skuCode;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductFavoriteId that = (ProductFavoriteId) o;
        return Objects.equals(username, that.username) && Objects.equals(skuCode, that.skuCode);
    }
    @Override
    public int hashCode() { return Objects.hash(username, skuCode); }
    // Gen Getters/Setters
}