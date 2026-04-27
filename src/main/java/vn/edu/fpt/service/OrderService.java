package vn.edu.fpt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.dto.OrderRequest;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class OrderService {

    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderDetailRepository orderDetailRepo;
    @Autowired private ProductSKURepository productRepo;
    @Autowired private InventoryRepository inventoryRepo;
    @Autowired private WarehouseRepository warehouseRepo;
    @Autowired private CartService cartService;
    @Autowired private InventoryService inventoryService;
    @Autowired private EmailService emailService;
    @Autowired private VoucherRepository voucherRepo;
    @Autowired private VoucherService voucherService;
    @Autowired private CartItemRepository cartItemRepo; // PHẢI CÓ CÁI NÀY ĐỂ XỬ LÝ GIỎ HÀNG DB

    @Transactional
    public Order createOrder(Order order, String voucherCode) {
        log.info("WEB-ORDER: Bắt đầu xử lý đơn cho: {}, Mã Voucher: {}", order.getFullname(), voucherCode);

        double defaultLat = 10.0125; double defaultLng = 105.7331;
        order.setOrderId("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);

        // --- BƯỚC 1: XÁC ĐỊNH NGUỒN HÀNG (DB HOẶC SESSION) ---
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) ? auth.getName() : null;

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        if (username != null) {
            // LẤY TỪ DATABASE (Khách đã đăng nhập)
            List<CartItem> dbItems = cartItemRepo.findAllByUsername(username);
            for (CartItem item : dbItems) {
                ProductSKU p = productRepo.findById(item.getSkuCode())
                        .orElseThrow(() -> new ResourceNotFoundException("Lỗi: " + item.getSkuCode()));

                BigDecimal itemPrice = p.getBasePrice();
                subtotal = subtotal.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));

                details.add(createDetail(order, p, item.getQuantity(), itemPrice, defaultLat, defaultLng));
            }
            // order.setUsername(username);  <-- XÓA DÒNG NÀY ĐI (Dòng gây lỗi)
        } else {
            // LẤY TỪ SESSION (Khách vãng lai)
            subtotal = cartService.getAmount();
            for (CartItem item : cartService.getItems()) {
                ProductSKU p = productRepo.findById(item.getSkuCode()).get();
                details.add(createDetail(order, p, item.getQuantity(), p.getBasePrice(), defaultLat, defaultLng));
            }
        }

        // --- BƯỚC 2: XỬ LÝ VOUCHER & TỔNG TIỀN (FIX LỖI 0đ) ---
        BigDecimal discount = BigDecimal.ZERO;
        if (voucherCode != null && !voucherCode.isEmpty()) {
            try {
                discount = voucherService.validateAndCalculateDiscount(voucherCode, subtotal);
                Voucher v = voucherRepo.findById(voucherCode).orElse(null);
                if (v != null) {
                    order.setVoucher(v);
                    v.setUsedCount(v.getUsedCount() + 1);
                    voucherRepo.save(v);
                }
            } catch (Exception e) { log.error("Voucher error: {}", e.getMessage()); }
        }

        BigDecimal finalTotal = subtotal.subtract(discount);
        order.setTotalAmount(finalTotal.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : finalTotal);
        order.setOrderDetails(details);

        // --- BƯỚC 3: LƯU ĐƠN & XÓA GIỎ (FIX LỖI CÒN SỐ 1) ---
        Order savedOrder = orderRepo.save(order);

        if (username != null) {
            cartItemRepo.deleteAllByUsername(username); // Xóa trong DB
        }
        cartService.clear(); // Xóa trong Session

        if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
            emailService.sendOrderConfirmation(savedOrder);
        }

        return savedOrder;
    }

    // Hàm phụ hỗ trợ tạo Detail (Giữ nguyên logic kho của ông)
    private OrderDetail createDetail(Order order, ProductSKU product, int qty, BigDecimal price, double lat, double lng) {
        Warehouse bestWh = findBestWarehouse(product, qty, lat, lng);
        if (bestWh == null) throw new RuntimeException("Sản phẩm " + product.getProductName() + " đã hết hàng!");

        OrderDetail detail = new OrderDetail();
        detail.setOrder(order);
        detail.setProduct(product);
        detail.setQuantity(qty);
        detail.setPrice(price);
        return detail;
    }

    // --- CÁC HÀM PLACEORDER, CANCELORDER, UPDATESTATUS GIỮ NGUYÊN 100% ---
    @Transactional
    public void placeOrder(OrderRequest request) {
        log.info("API-ORDER: Nhận yêu cầu từ API cho OrderID: {}", request.getOrderId());
        Order order = new Order();
        order.setOrderId(request.getOrderId());
        order.setShippingAddress(request.getShippingAddress());
        order.setFullname("API External Customer");
        order.setTotalAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatus.PENDING);
        orderRepo.save(order);
        if (request.getItems() != null) {
            for (OrderRequest.OrderItemRequest item : request.getItems()) {
                inventoryService.reserveStock(order.getOrderId(), item.getSkuCode(), item.getWarehouseId(), item.getQuantity());
            }
        }
    }

    @Transactional
    public String placeOrder(String sku, int quantity, double custLat, double custLng) {
        log.info("MOCK-TEST: Trừ kho trực tiếp cho SKU: {}", sku);
        ProductSKU product = productRepo.findById(sku).orElseThrow(() -> new ResourceNotFoundException("SKU không tồn tại: " + sku));
        Warehouse bestWh = findBestWarehouse(product, quantity, custLat, custLng);
        if (bestWh == null) return "Hết hàng!";
        Inventory inv = inventoryRepo.findByWarehouseAndSku(bestWh.getWarehouseId(), sku).orElseThrow(() -> new RuntimeException("Lỗi!"));
        inv.setQuantity(inv.getQuantity() - quantity);
        inventoryRepo.save(inv);
        return "Thành công! Xuất từ kho: " + bestWh.getName();
    }

    @Transactional
    public void cancelOrder(String orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("ID: " + orderId));
        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == OrderStatus.SHIPPING || oldStatus == OrderStatus.COMPLETED) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Inventory inv = inventoryRepo.findByProduct_SkuCode(detail.getProduct().getSkuCode()).stream().findFirst().orElse(null);
                if (inv != null) {
                    inv.setQuantity(inv.getQuantity() + detail.getQuantity());
                    inventoryRepo.save(inv);
                }
            }
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepo.save(order);
        emailService.sendOrderStatusUpdate(order);
    }

    @Transactional
    public void updateStatus(String orderId, OrderStatus newStatus) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("ID: " + orderId));
        OrderStatus oldStatus = order.getStatus();
        if ((oldStatus == OrderStatus.PENDING || oldStatus == OrderStatus.PAID) && newStatus == OrderStatus.SHIPPING) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Warehouse bestWh = findBestWarehouse(detail.getProduct(), detail.getQuantity(), 10.03, 105.78);
                if (bestWh == null) throw new RuntimeException("Kho hết hàng: " + detail.getProduct().getProductName());
                Inventory inv = inventoryRepo.findByWarehouseAndSku(bestWh.getWarehouseId(), detail.getProduct().getSkuCode()).get();
                inv.setQuantity(inv.getQuantity() - detail.getQuantity());
                inventoryRepo.save(inv);
            }
        }
        order.setStatus(newStatus);
        orderRepo.save(order);
        emailService.sendOrderStatusUpdate(order);
    }

    private Warehouse findBestWarehouse(ProductSKU product, int qty, double lat, double lng) {
        if (product == null) return null;
        List<Warehouse> warehouses = warehouseRepo.findAll();
        Warehouse best = null;
        double minDistance = Double.MAX_VALUE;
        for (Warehouse wh : warehouses) {
            Optional<Inventory> invOpt = inventoryRepo.findByWarehouseAndSku(wh.getWarehouseId(), product.getSkuCode());
            if (invOpt.isPresent() && invOpt.get().getQuantity() >= qty) {
                double dist = Math.sqrt(Math.pow(wh.getLatitude() - lat, 2) + Math.pow(wh.getLongitude() - lng, 2));
                if (dist < minDistance) { minDistance = dist; best = wh; }
            }
        }
        return best;
    }
}