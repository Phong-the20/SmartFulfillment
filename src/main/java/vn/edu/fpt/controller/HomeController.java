package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import vn.edu.fpt.entity.ProductFavorite;
import vn.edu.fpt.entity.ProductSKU;
import vn.edu.fpt.repository.InventoryRepository;
import vn.edu.fpt.repository.OrderDetailRepository;
import vn.edu.fpt.repository.ProductFavoriteRepository;
import vn.edu.fpt.repository.ProductSKURepository;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    @Autowired private ProductSKURepository productRepo;
    @Autowired private InventoryRepository inventoryRepo;
    @Autowired private OrderDetailRepository orderDetailRepo;
    @Autowired private ProductFavoriteRepository favoriteRepo;

    @GetMapping("/")
    public String homePage(Model model, Authentication auth) {
        // 1. Lấy tất cả sản phẩm và tính tồn kho
        List<ProductSKU> products = productRepo.findAll();
        for (ProductSKU p : products) {
            Integer stock = inventoryRepo.sumQuantityBySkuCode(p.getSkuCode());
            p.setTotalQuantity(stock != null ? stock : 0);
        }
        model.addAttribute("products", products);

        // 2. Top 4 bán chạy
        Pageable topPage = PageRequest.of(0, 4);
        List<Object[]> topSellingRaw = orderDetailRepo.findTopSellingProducts(topPage);
        List<ProductSKU> bestSellers = topSellingRaw.stream()
                .map(result -> {
                    ProductSKU p = (ProductSKU) result[0];
                    Integer stock = inventoryRepo.sumQuantityBySkuCode(p.getSkuCode());
                    p.setTotalQuantity(stock != null ? stock : 0);
                    return p;
                }).collect(Collectors.toList());
        model.addAttribute("bestSellers", bestSellers);

        // 3. Check tim đỏ
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            List<String> favoritedSkus = favoriteRepo.findAllByUsername(username)
                    .stream().map(ProductFavorite::getSkuCode).toList();
            model.addAttribute("favoritedSkus", favoritedSkus);
        }

        return "index";
    }

    @GetMapping("favicon.ico")
    @ResponseBody
    void returnNoFavicon() {}
}