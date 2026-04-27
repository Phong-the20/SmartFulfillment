package vn.edu.fpt.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.entity.Account;
import vn.edu.fpt.entity.CartItem;
import vn.edu.fpt.entity.Order;
import vn.edu.fpt.entity.OrderStatus;
import vn.edu.fpt.entity.Voucher;
import vn.edu.fpt.repository.AccountRepository;
import vn.edu.fpt.repository.CartItemRepository; // 1. Thêm cái này
import vn.edu.fpt.repository.ProductSKURepository; // Để lấy thông tin sản phẩm hiện lên
import vn.edu.fpt.repository.VoucherRepository;
import vn.edu.fpt.service.CartService;
import vn.edu.fpt.service.MoMoService;
import vn.edu.fpt.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired private CartService cartService;
    @Autowired private OrderService orderService;
    @Autowired private AccountRepository accountRepo;
    @Autowired private MoMoService moMoService;
    @Autowired private vn.edu.fpt.service.EmailService emailService;
    @Autowired private vn.edu.fpt.repository.OrderRepository orderRepo;
    @Autowired private VoucherRepository voucherRepo;
    @Autowired private CartItemRepository cartItemRepo; // 2. Autowired nó vào
    @Autowired private ProductSKURepository productRepo;

    @GetMapping
    public String showCheckout(Model model, Authentication auth) {
        List<CartItem> displayItems = new ArrayList<>();
        BigDecimal cartTotal = BigDecimal.ZERO;
        Account currentUser = null;

        // --- BƯỚC 1: LẤY DỮ LIỆU GIỎ HÀNG CHUẨN (DB HOẶC SESSION) ---
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String username = auth.getName();
            currentUser = accountRepo.findByUsername(username);

            List<CartItem> dbItems = cartItemRepo.findAllByUsername(username);
            for (CartItem item : dbItems) {
                productRepo.findById(item.getSkuCode()).ifPresent(p -> {
                    item.setProductName(p.getProductName());
                    item.setPrice(p.getBasePrice());
                    item.setImageUrl(p.getImageUrl());
                    displayItems.add(item);
                });
                // Tính tổng tiền từ DB
                cartTotal = cartTotal.add(item.getPrice() != null ? item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO);
            }
        } else {
            displayItems.addAll(cartService.getItems());
            cartTotal = cartService.getAmount();
        }

        // --- BƯỚC 2: KIỂM TRA GIỎ TRỐNG (FIX LỖI NHẤN NÚT KHÔNG ĂN) ---
        if (displayItems.isEmpty()) {
            return "redirect:/cart";
        }

        Order order = new Order();
        if (currentUser != null) {
            order.setFullname(currentUser.getFullName());
            order.setEmail(currentUser.getEmail());
            order.setPhone(currentUser.getPhone());
            order.setShippingAddress(currentUser.getAddress());
        }

        // --- BƯỚC 3: XỬ LÝ VOUCHER ---
        LocalDateTime now = LocalDateTime.now();
        BigDecimal finalCartTotal = cartTotal; // Dùng biến tạm cho Lambda
        List<Voucher> availableVouchers = voucherRepo.findAll().stream()
                .filter(Voucher::isActive)
                .filter(v -> v.getStartDate().isBefore(now) && v.getEndDate().isAfter(now))
                .filter(v -> v.getUsageLimit() > v.getUsedCount())
                .filter(v -> v.getMinOrderAmount() != null && v.getMinOrderAmount().compareTo(finalCartTotal) <= 0)
                .collect(Collectors.toList());

        model.addAttribute("availableVouchers", availableVouchers);
        model.addAttribute("order", order);
        model.addAttribute("cartItems", displayItems); // Đẩy list đã map tên/ảnh ra
        model.addAttribute("total", cartTotal);

        return "checkout";
    }

    @PostMapping
    public String processCheckout(
            @ModelAttribute("order") Order order,
            @RequestParam(required = false) String appliedVoucherCode,
            Model model) {

        Order savedOrder = orderService.createOrder(order, appliedVoucherCode);

        if ("MOMO".equalsIgnoreCase(order.getPaymentMethod())) {
            long amount = savedOrder.getTotalAmount().longValue();
            String payUrl = moMoService.createMoMoPayment(savedOrder.getOrderId(), amount);
            return "redirect:" + payUrl;
        }

        model.addAttribute("orderId", savedOrder.getOrderId());
        return "checkout-success";
    }

    @GetMapping("/momo-return")
    public String momoReturn(@RequestParam int resultCode, @RequestParam String orderId, Model model) {
        if (resultCode == 0) {
            orderService.updateStatus(orderId, OrderStatus.PAID);
            orderRepo.findById(orderId).ifPresent(emailService::sendOrderConfirmation);
            model.addAttribute("orderId", orderId);
            return "checkout-success";
        }
        return "redirect:/checkout?error=PaymentFailed";
    }

    @GetMapping("/success")
    public String showSuccess() {
        return "checkout-success";
    }
}