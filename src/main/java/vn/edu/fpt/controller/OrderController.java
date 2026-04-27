package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.Account;
import vn.edu.fpt.entity.Order;
import vn.edu.fpt.repository.AccountRepository;
import vn.edu.fpt.repository.OrderRepository;
import vn.edu.fpt.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepo;
    @Autowired
    private AccountRepository accountRepo;

    @GetMapping("/mock-checkout")
    public String mockCheckout(@RequestParam String sku, @RequestParam int qty) {
        // Giả lập tọa độ của khách đang ở Ninh Kiều, Cần Thơ (10.03, 105.78)
        double fakeCustomerLat = 10.03;
        double fakeCustomerLng = 105.78;

        return orderService.placeOrder(sku, qty, fakeCustomerLat, fakeCustomerLng);
    }

    @GetMapping
    public String viewMyOrders(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. Lấy thông tin user
        Account currentAcc = accountRepo.findByUsername(auth.getName());
        if (currentAcc == null) return "redirect:/login";

        // 2. PHẢI CÓ DÒNG NÀY: Để fix lỗi "cannot convert from null to boolean"
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ADMIN")
                        || a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        // 3. Lấy danh sách đơn hàng
        List<Order> myOrders = orderRepo.findByEmailOrderByOrderDateDesc(currentAcc.getEmail());
        model.addAttribute("orders", myOrders);

        return "orders";
    }

    @GetMapping("/detail/{id}")
    public String viewOrderDetail(@PathVariable String id, Model model, Authentication auth) {
        Order order = orderRepo.findById(id).orElse(null);
        if (order == null) return "redirect:/orders";

        // 1. Check quyền ADMIN (Xử lý cả trường hợp có hoặc không có tiền tố ROLE_)
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ADMIN")
                        || a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));

        // 2. Lấy Email người đang đăng nhập
        String currentEmail = "";
        if (auth.getPrincipal() instanceof OAuth2User) {
            currentEmail = ((OAuth2User) auth.getPrincipal()).getAttribute("email");
        } else {
            Account acc = accountRepo.findByUsername(auth.getName());
            currentEmail = (acc != null) ? acc.getEmail() : "";
        }

        // 3. Bảo mật: Nếu không phải Admin VÀ cũng không phải chủ đơn hàng thì mới đuổi đi
        if (!isAdmin && !order.getEmail().equals(currentEmail)) {
            return "redirect:/orders";
        }
        model.addAttribute("isAdmin", isAdmin);

        model.addAttribute("order", order);
        return "order-detail";
    }
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable String id, RedirectAttributes ra) {
        try {
            orderService.cancelOrder(id);
            ra.addFlashAttribute("successMsg", "Đã hủy đơn hàng thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/orders/detail/" + id;
    }
}
