package vn.edu.fpt.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import vn.edu.fpt.entity.CartItem;
import vn.edu.fpt.entity.ProductSKU;
import java.math.BigDecimal;
import java.util.*;

@Service
@SessionScope
public class CartService {

    // Tên biến của ông là 'map'
    private Map<String, CartItem> map = new HashMap<>();

    public void add(ProductSKU sku, int qty) {
        CartItem item = map.get(sku.getSkuCode());

        if (item == null) {
            item = new CartItem();
            item.setSkuCode(sku.getSkuCode());
            item.setProductName(sku.getProductName());
            item.setImageUrl(sku.getImageUrl());
            item.setPrice(sku.getBasePrice());
            item.setQuantity(qty);
            item.setTotalPrice(sku.getBasePrice().multiply(BigDecimal.valueOf(qty)));
            map.put(sku.getSkuCode(), item);
        } else {
            int newQty = item.getQuantity() + qty;
            item.setQuantity(newQty);
            item.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(newQty)));
        }
    }

    public void remove(String skuCode) {
        map.remove(skuCode);
    }

    public void clear() {
        map.clear();
    }

    public Collection<CartItem> getItems() {
        return map.values();
    }

    /**
     * TÍNH TỔNG SỐ LƯỢNG (ĐÃ FIX TÊN BIẾN 'MAP')
     */
    public int getCount() {
        // Phải dùng map.values() mới đúng tên biến ông đã khai báo ở trên
        return map.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * TÍNH TỔNG TIỀN
     */
    public BigDecimal getAmount() {
        return map.values().stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void update(String skuCode, int qty) {
        CartItem item = map.get(skuCode);
        if (item != null) {
            item.setQuantity(qty);
            item.setTotalPrice(item.getPrice().multiply(BigDecimal.valueOf(qty)));
        }
    }
}