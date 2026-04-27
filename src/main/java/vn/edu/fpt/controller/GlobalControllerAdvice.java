package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vn.edu.fpt.entity.Account;
import vn.edu.fpt.entity.CartItem;
import vn.edu.fpt.repository.AccountRepository;
import vn.edu.fpt.repository.CartItemRepository;
import vn.edu.fpt.service.CartService;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired private AccountRepository accountRepo;
    @Autowired private CartService cartService;
    @Autowired private CartItemRepository cartItemRepo;

    @ModelAttribute
    public void addAttributes(Model model, Authentication auth) {
        int cartCount = 0;
        Account user = null;

        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String username = auth.getName();

            // 1. Lấy thông tin User
            if (auth.getPrincipal() instanceof OAuth2User) {
                String email = ((OAuth2User) auth.getPrincipal()).getAttribute("email");
                user = accountRepo.findByEmail(email);
            } else {
                user = accountRepo.findByUsername(username);
            }

            // 2. TÍNH TỔNG SỐ LƯỢNG TRONG DATABASE (1 + 1 + 2 = 4)
            List<CartItem> dbItems = cartItemRepo.findAllByUsername(username);
            // Thay vì dùng .size(), mình dùng stream để cộng dồn quantity
            cartCount = dbItems.stream().mapToInt(CartItem::getQuantity).sum();

        } else {
            // 3. TÍNH TỔNG SỐ LƯỢNG TRONG SESSION
            // Đảm bảo hàm getCount() trong CartService cũng đã sửa logic cộng dồn quantity
            cartCount = cartService.getCount();
        }

        model.addAttribute("user", user);
        model.addAttribute("cartCount", cartCount);
    }
}