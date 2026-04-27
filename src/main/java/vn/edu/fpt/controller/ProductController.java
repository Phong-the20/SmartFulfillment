package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.entity.Account;
import vn.edu.fpt.entity.OrderStatus;
import vn.edu.fpt.entity.ProductSKU;
import vn.edu.fpt.entity.ProductFavorite;
import vn.edu.fpt.repository.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    @Autowired private InventoryRepository inventoryRepo;
    @Autowired private ProductSKURepository productRepo;
    @Autowired private OrderDetailRepository orderDetailRepo;
    @Autowired private ReviewRepository reviewRepo;
    @Autowired private AccountRepository accountRepo;
    @Autowired private ProductFavoriteRepository favoriteRepo;

    @GetMapping("/products")
    public String showAllProducts(@RequestParam(defaultValue = "0") int page, Model model, Authentication auth) {
        Pageable pageable = PageRequest.of(page, 6);
        Page<ProductSKU> productPage = productRepo.findAll(pageable);
        calculateStockForList(productPage.getContent());
        addFavoriteStatusToModel(model, auth);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("isAllProducts", true);
        model.addAttribute("resultCount", productPage.getTotalElements());
        return "index";
    }

    @GetMapping("/products/search")
    public String searchAndFilter(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "min", required = false) Double min,
            @RequestParam(value = "max", required = false) Double max,
            @RequestParam(defaultValue = "0") int page,
            Model model, Authentication auth) {
        double minPrice = (min == null || min < 0) ? 0 : min;
        double maxPrice = (max == null || max <= 0) ? 999999999 : max;
        Pageable pageable = PageRequest.of(page, 6);
        Page<ProductSKU> productPage = productRepo.findByProductNameContainingIgnoreCaseAndBasePriceBetweenOrderByBasePriceAsc(
                keyword.trim(), minPrice, maxPrice, pageable);
        calculateStockForList(productPage.getContent());
        addFavoriteStatusToModel(model, auth);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("min", minPrice);
        model.addAttribute("max", maxPrice);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "index";
    }

    @GetMapping("/products/detail/{id}")
    public String showProductDetail(@PathVariable("id") String id, Model model, Authentication auth) {
        ProductSKU product = productRepo.findById(id).orElse(null);
        if (product == null) return "redirect:/products";

        // Logic tim đỏ
        addFavoriteStatusToModel(model, auth);

        // Tính tồn kho
        Integer totalStock = inventoryRepo.sumQuantityBySkuCode(id);
        product.setTotalQuantity(totalStock != null ? totalStock : 0);

        // Sản phẩm liên quan
        List<ProductSKU> relatedProducts = productRepo.findByCategory(product.getCategory());
        relatedProducts.removeIf(p -> p.getSkuCode().equals(id));
        calculateStockForList(relatedProducts);

        // --- ĐOẠN ĐÁNH GIÁ ---
        boolean canReview = checkUserCanReview(auth, id);
        model.addAttribute("p", product);
        model.addAttribute("related", relatedProducts);
        model.addAttribute("stock", product.getTotalQuantity());
        model.addAttribute("canReview", canReview);
        model.addAttribute("reviews", reviewRepo.findBySkuCodeOrderByCreatedAtDesc(id));

        return "product-detail";
    }

    private boolean checkUserCanReview(Authentication auth, String skuCode) {
        if (auth == null || !auth.isAuthenticated()) return false;

        String email = "";
        // 1. Lấy Email chuẩn (Dùng cho cả 2 loại login)
        if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            email = ((org.springframework.security.oauth2.core.user.OAuth2User) auth.getPrincipal()).getAttribute("email");
        } else {
            vn.edu.fpt.entity.Account acc = accountRepo.findByUsername(auth.getName());
            if (acc != null) email = acc.getEmail();
        }

        // 2. ĐẾM THEO EMAIL HẾT CHO TUI
        int boughtCount = orderDetailRepo.countPurchased(email, skuCode, OrderStatus.COMPLETED);
        int reviewCount = reviewRepo.countByEmailAndSkuCode(email, skuCode);

        // THÊM ĐOẠN NÀY ĐỂ DEBUG TRÊN MÀN HÌNH CONSOLE (Chữ màu trắng ở dưới IntelliJ)
        System.out.println("=== TEST ĐÁNH GIÁ ===");
        System.out.println("Email đang check: " + email);
        System.out.println("Số lượng đã mua COMPLETED: " + boughtCount);
        System.out.println("Số lần đã review: " + reviewCount);
        System.out.println("Form có hiện không?: " + (boughtCount > 0 && reviewCount == 0));

        // 3. SO SÁNH: Mua ít nhất 1 lần và chưa review thì cho hiện
        return boughtCount > 0 && reviewCount == 0;
    }
    private void addFavoriteStatusToModel(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            List<String> favoritedSkus = favoriteRepo.findAllByUsername(auth.getName())
                    .stream().map(ProductFavorite::getSkuCode).toList();
            model.addAttribute("favoritedSkus", favoritedSkus);
        }
    }

    private void calculateStockForList(List<ProductSKU> products) {
        if (products != null) {
            for (ProductSKU p : products) {
                Integer stock = inventoryRepo.sumQuantityBySkuCode(p.getSkuCode());
                p.setTotalQuantity(stock != null ? stock : 0);
            }
        }
    }

    @GetMapping("/favorites")
    public String showFavorites(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return "redirect:/login";
        String username = auth.getName();
        List<String> skuCodes = favoriteRepo.findAllByUsername(username).stream().map(ProductFavorite::getSkuCode).toList();
        List<ProductSKU> products = productRepo.findAllById(skuCodes);
        calculateStockForList(products);
        model.addAttribute("products", products);
        model.addAttribute("favoritedSkus", skuCodes);
        model.addAttribute("title", "Sản phẩm bạn đã yêu thích");
        return "favorites";
    }
}