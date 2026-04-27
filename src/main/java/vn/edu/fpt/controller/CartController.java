package vn.edu.fpt.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.CartItem;
import vn.edu.fpt.entity.Inventory;
import vn.edu.fpt.entity.ProductSKU;
import vn.edu.fpt.repository.CartItemRepository;
import vn.edu.fpt.repository.InventoryRepository;
import vn.edu.fpt.repository.ProductSKURepository;
import vn.edu.fpt.service.CartService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired private ProductSKURepository productRepo;
    @Autowired private CartService cartService;
    @Autowired private InventoryRepository inventoryRepo;
    @Autowired private CartItemRepository cartItemRepo;

    // 1. HIỂN THỊ GIỎ HÀNG
    @GetMapping
    public String viewCart(Model model, Authentication auth) {
        List<CartItem> displayItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (auth != null && auth.isAuthenticated()) {
            // --- KHÁCH ĐÃ ĐĂNG NHẬP: Lấy từ Database ---
            List<CartItem> dbItems = cartItemRepo.findAllByUsername(auth.getName());

            for (CartItem item : dbItems) {
                ProductSKU p = productRepo.findById(item.getSkuCode()).orElse(null);
                if (p != null) {
                    // "Hồi sinh" các trường @Transient để hiện lên HTML
                    item.setProductName(p.getProductName());
                    item.setImageUrl(p.getImageUrl());
                    item.setPrice(p.getBasePrice());
                    item.setTotalPrice(p.getBasePrice().multiply(BigDecimal.valueOf(item.getQuantity())));

                    displayItems.add(item);
                    totalAmount = totalAmount.add(item.getTotalPrice());
                }
            }
        } else {
            // --- KHÁCH VÃNG LAI: Lấy từ Session ---
            displayItems.addAll(cartService.getItems());
            // FIX LỖI Ở ĐÂY: Gán trực tiếp vì getAmount() đã là BigDecimal rồi
            totalAmount = cartService.getAmount();
        }

        model.addAttribute("cartItems", displayItems);
        model.addAttribute("totalAmount", totalAmount);
        return "cart";
    }

    // 2. THÊM VÀO GIỎ HÀNG
    @PostMapping("/add")
    @Transactional
    public String addToCart(@RequestParam("skuCode") String skuCode,
                            @RequestParam("quantity") int quantity,
                            Authentication auth,
                            RedirectAttributes ra) {

        // Chống hack số lượng
        if (quantity <= 0) return "redirect:/products";

        ProductSKU product = productRepo.findById(skuCode).orElse(null);
        if (product == null) return "redirect:/products";

        // Kiểm tra tồn kho thực tế
        int realStock = inventoryRepo.findByProduct(product).stream().mapToInt(Inventory::getQuantity).sum();
        if (quantity > realStock) {
            ra.addFlashAttribute("errorMsg", "Kho chỉ còn " + realStock + " sản phẩm!");
            return "redirect:/products/detail/" + skuCode;
        }

        if (auth != null && auth.isAuthenticated()) {
            // Lưu vào DB nếu đã đăng nhập
            CartItem item = cartItemRepo.findByUsernameAndSkuCode(auth.getName(), skuCode)
                    .orElse(new CartItem(auth.getName(), skuCode, 0));
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepo.save(item);
        } else {
            // Lưu vào Session nếu chưa đăng nhập
            cartService.add(product, quantity);
        }

        ra.addFlashAttribute("successMsg", "Đã thêm vào giỏ hàng!");
        return "redirect:/cart";
    }

    // 3. XÓA SẢN PHẨM KHỎI GIỎ
    @GetMapping("/remove/{sku}")
    @Transactional
    public String removeFromCart(@PathVariable("sku") String sku, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            cartItemRepo.deleteByUsernameAndSkuCode(auth.getName(), sku);
        } else {
            cartService.remove(sku);
        }
        return "redirect:/cart";
    }

    // 4. MUA NGAY
    @GetMapping("/buy-now/{skuCode}")
    public String buyNow(@PathVariable String skuCode, Authentication auth, RedirectAttributes ra) {
        // Tự động thêm 1 sản phẩm vào giỏ và đi tới trang thanh toán
        this.addToCart(skuCode, 1, auth, ra);
        return "redirect:/checkout";
    }
}