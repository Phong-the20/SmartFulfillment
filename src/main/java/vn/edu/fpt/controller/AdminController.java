package vn.edu.fpt.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.*;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.repository.*;
import vn.edu.fpt.service.CloudinaryService;
import vn.edu.fpt.service.OrderService;
import vn.edu.fpt.service.VoucherService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "Order Management", description = "Các API liên quan đến quản lý đơn hàng")
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private ProductSKURepository productRepo;
    @Autowired private WarehouseRepository warehouseRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private OrderService orderService;
    @Autowired private OrderDetailRepository orderDetailRepo;
    @Autowired private InventoryRepository inventoryRepo;
    @Autowired private VoucherRepository voucherRepo;
    @Autowired private VoucherService voucherService;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private ReviewRepository reviewRepo;
    @Autowired private AccountRepository accountRepo;
    @Autowired private RoleRepository roleRepo;


    @ModelAttribute
    public void addCommonAttributes(Model model, Authentication auth) {
        if (auth != null) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ADMIN")
                            || a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
            model.addAttribute("isAdmin", isAdmin);
        }
    }

    @GetMapping({"", "/"})
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    // =========================================================
    // 1. DASHBOARD & THỐNG KÊ
    // =========================================================
    @GetMapping("/dashboard")
    public String showDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model) {

        LocalDate start = (startDate != null) ? startDate : LocalDate.now().minusDays(6);
        LocalDate end = (endDate != null) ? endDate : LocalDate.now();

        LocalDateTime startDT = start.atStartOfDay();
        LocalDateTime endDT = end.atTime(23, 59, 59);

        // KPIs
        model.addAttribute("totalOrders", orderRepo.count());
        model.addAttribute("totalProducts", productRepo.count());

        BigDecimal totalRevenue = orderRepo.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.PAID || o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("totalRevenue", totalRevenue);

        // Biểu đồ Doanh thu
        List<Object[]> rawData = orderRepo.getRevenueByRange(startDT, endDT);
        Map<LocalDate, BigDecimal> statsMap = new HashMap<>();
        for (Object[] row : rawData) {
            statsMap.put(((java.sql.Date) row[0]).toLocalDate(), (BigDecimal) row[1]);
        }

        List<String> labels = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            labels.add(date.format(formatter));
            values.add(statsMap.getOrDefault(date, BigDecimal.ZERO));
        }

        // Top 4 Sản phẩm bán chạy
        org.springframework.data.domain.Pageable topFour = org.springframework.data.domain.PageRequest.of(0, 4);
        List<Object[]> topSellingRaw = orderDetailRepo.findTopSellingProducts(topFour);

        List<Map<String, Object>> topProducts = topSellingRaw.stream().limit(4).map(result -> {
            ProductSKU p = (ProductSKU) result[0];
            Map<String, Object> map = new HashMap<>();
            map.put("name", p.getProductName());
            map.put("sku", p.getSkuCode());
            map.put("image", p.getImageUrl());
            map.put("price", p.getBasePrice());
            map.put("sold", result[1]);
            return map;
        }).collect(Collectors.toList());

        model.addAttribute("chartLabels", labels);
        model.addAttribute("chartData", values);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("topProducts", topProducts);
        model.addAttribute("view", "admin/dashboard");
        model.addAttribute("activeTab", "dashboard");
        return "admin/admin-layout";
    }

    // =========================================================
    // 2. QUẢN LÝ SẢN PHẨM
    // =========================================================
    @GetMapping("/products")
    public String showProductManager(Model model) {
        model.addAttribute("products", productRepo.findAll());
        model.addAttribute("view", "admin/products-manage");
        model.addAttribute("activeTab", "products");
        return "admin/admin-layout";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute ProductSKU product, @RequestParam("imageFile") MultipartFile imageFile, RedirectAttributes ra) {
        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                product.setImageUrl(cloudinaryService.uploadImage(imageFile));
            }
            product.setActive(true);
            productRepo.save(product);
            ra.addFlashAttribute("successMsg", "Đã thêm sản phẩm mới!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/update")
    public String updateProduct(@ModelAttribute ProductSKU product, @RequestParam("imageFile") MultipartFile imageFile, RedirectAttributes ra) {
        try {
            ProductSKU existing = productRepo.findById(product.getSkuCode()).orElseThrow(() -> new Exception("Không tìm thấy"));
            existing.setProductName(product.getProductName());
            existing.setBasePrice(product.getBasePrice());
            existing.setCategory(product.getCategory());
            existing.setWeightGram(product.getWeightGram());
            if (imageFile != null && !imageFile.isEmpty()) {
                existing.setImageUrl(cloudinaryService.uploadImage(imageFile));
            }
            productRepo.save(existing);
            ra.addFlashAttribute("successMsg", "Cập nhật thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/toggle/{sku}")
    public String toggleProductStatus(@PathVariable String sku, RedirectAttributes ra) {
        ProductSKU product = productRepo.findById(sku).orElseThrow(() -> new ResourceNotFoundException("Not found"));
        product.setActive(!product.isActive());
        productRepo.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") String sku, RedirectAttributes ra) {
        if (orderDetailRepo.existsByProduct_SkuCode(sku)) {
            ra.addFlashAttribute("errorMsg", "Sản phẩm đã có trong đơn hàng, không thể xóa!");
        } else {
            productRepo.deleteById(sku);
            ra.addFlashAttribute("successMsg", "Đã xóa vĩnh viễn sản phẩm!");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/products/api/{id}")
    @ResponseBody
    public ProductSKU getProductDetails(@PathVariable String id) {
        return productRepo.findById(id).orElse(null);
    }

    // =========================================================
    // 3. QUẢN LÝ KHO HÀNG (HÀM CŨ ÔNG CẦN ĐÂY)
    // =========================================================
    @GetMapping("/map")
    public String showWarehouseMap(Model model) {
        model.addAttribute("view", "admin/warehouse-map");
        model.addAttribute("activeTab", "map");
        return "admin/admin-layout";
    }

    @PostMapping("/warehouses/add")
    public String addWarehouse(@ModelAttribute Warehouse warehouse, RedirectAttributes ra) {
        warehouseRepo.save(warehouse);
        ra.addFlashAttribute("successMsg", "Đã thêm kho mới!");
        return "redirect:/admin/map";
    }

    @GetMapping("/warehouses/api/{id}")
    @ResponseBody
    public Warehouse getWarehouseDetails(@PathVariable Integer id) {
        return warehouseRepo.findById(id).orElse(null);
    }

    @PostMapping("/warehouses/update")
    public String updateWarehouse(@ModelAttribute Warehouse warehouse, RedirectAttributes ra) {
        Warehouse existing = warehouseRepo.findById(warehouse.getWarehouseId()).get();
        existing.setName(warehouse.getName());
        existing.setAddress(warehouse.getAddress());
        existing.setLatitude(warehouse.getLatitude());
        existing.setLongitude(warehouse.getLongitude());
        warehouseRepo.save(existing);
        return "redirect:/admin/map";
    }

    @GetMapping("/warehouses/delete/{id}")
    public String deleteWarehouse(@PathVariable Integer id, RedirectAttributes ra) {
        Warehouse wh = warehouseRepo.findById(id).orElse(null);
        long totalStock = (wh != null && wh.getInventories() != null) ?
                wh.getInventories().stream().mapToLong(Inventory::getQuantity).sum() : 0;
        if (totalStock > 0) {
            ra.addFlashAttribute("errorMsg", "Kho còn hàng, không thể xóa!");
        } else {
            warehouseRepo.deleteById(id);
            ra.addFlashAttribute("successMsg", "Đã xóa kho!");
        }
        return "redirect:/admin/map";
    }

    @GetMapping("/inventory")
    public String showInventoryForm(Model model) {
        model.addAttribute("warehouses", warehouseRepo.findAll());
        model.addAttribute("products", productRepo.findAll());
        model.addAttribute("view", "admin/inventory-manage");
        model.addAttribute("activeTab", "inventory");
        return "admin/admin-layout";
    }

    @PostMapping("/warehouses/transfer")
    @Transactional
    public String transferStock(@RequestParam Integer fromWarehouseId, @RequestParam Integer toWarehouseId,
                                @RequestParam String skuCode, @RequestParam Integer quantity, RedirectAttributes ra) {
        try {
            Inventory sourceInv = inventoryRepo.findByWarehouseAndSku(fromWarehouseId, skuCode).orElseThrow(() -> new Exception("Kho nguồn không có hàng!"));
            if (sourceInv.getQuantity() < quantity) throw new Exception("Không đủ hàng!");
            sourceInv.setQuantity(sourceInv.getQuantity() - quantity);
            inventoryRepo.save(sourceInv);

            Inventory destInv = inventoryRepo.findByWarehouseAndSku(toWarehouseId, skuCode).orElse(new Inventory());
            if (destInv.getInventoryId() == null) {
                destInv.setWarehouse(warehouseRepo.getReferenceById(toWarehouseId));
                destInv.setProduct(productRepo.getReferenceById(skuCode));
                destInv.setQuantity(quantity);
            } else {
                destInv.setQuantity(destInv.getQuantity() + quantity);
            }
            inventoryRepo.save(destInv);
            ra.addFlashAttribute("successMsg", "Điều phối thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/map";
    }

    // =========================================================
    // 4. QUẢN LÝ ĐƠN HÀNG & XUẤT EXCEL
    // =========================================================
    @GetMapping("/orders")
    public String showOrderManager(Model model) {
        model.addAttribute("orders", orderRepo.findAllByOrderByOrderDateDesc());
        model.addAttribute("view", "admin/orders-manage");
        model.addAttribute("activeTab", "orders");
        return "admin/admin-layout";
    }

    @GetMapping("/orders/shipping/{id}")
    public String shipOrder(@PathVariable String id) {
        orderService.updateStatus(id, OrderStatus.SHIPPING);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/complete/{id}")
    public String completeOrder(@PathVariable String id) {
        orderService.updateStatus(id, OrderStatus.COMPLETED);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/cancel/{id}")
    public String cancelOrder(@PathVariable String id) {
        orderService.updateStatus(id, OrderStatus.CANCELLED);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/export")
    public void exportOrdersToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=bao_cao_" + LocalDate.now() + ".xlsx");
        List<Order> listOrders = orderRepo.findAll();
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Doanh Thu");
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Mã đơn");
            headerRow.createCell(1).setCellValue("Ngày đặt");
            headerRow.createCell(2).setCellValue("Tổng tiền");
            headerRow.createCell(3).setCellValue("Trạng thái");

            int rowCount = 1;
            for (Order order : listOrders) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowCount++);
                row.createCell(0).setCellValue(order.getOrderId());
                row.createCell(1).setCellValue(order.getOrderDate().toString());
                row.createCell(2).setCellValue(order.getTotalAmount().doubleValue());
                row.createCell(3).setCellValue(order.getStatus().toString());
            }
            workbook.write(response.getOutputStream());
        }
    }

    // =========================================================
    // 5. QUẢN LÝ VOUCHER (MÃ GIẢM GIÁ)
    // =========================================================
    @GetMapping("/vouchers")
    public String manageVouchers(Model model) {
        model.addAttribute("vouchers", voucherRepo.findAll());
        model.addAttribute("view", "admin/vouchers-manage");
        model.addAttribute("activeTab", "vouchers");
        return "admin/admin-layout";
    }

    @PostMapping("/vouchers/save")
    public String saveVoucher(@ModelAttribute Voucher voucher, RedirectAttributes ra) {
        try {
            if (voucher.getStartDate().isAfter(voucher.getEndDate())) throw new Exception("Ngày không hợp lệ!");
            voucherRepo.save(voucher);
            ra.addFlashAttribute("successMsg", "Lưu voucher thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/vouchers/update")
    public String updateVoucher(@ModelAttribute Voucher voucher, RedirectAttributes ra) {
        try {
            if (voucher.getUsageLimit() != null && voucher.getUsageLimit() < 0) throw new Exception("Lượt dùng không được âm!");
            Voucher existing = voucherRepo.findById(voucher.getVoucherCode()).orElseThrow(() -> new Exception("Mã không tồn tại"));
            existing.setDescription(voucher.getDescription());
            existing.setDiscountValue(voucher.getDiscountValue());
            existing.setUsageLimit(voucher.getUsageLimit());
            existing.setStartDate(voucher.getStartDate());
            existing.setEndDate(voucher.getEndDate());
            existing.setActive(voucher.isActive());
            voucherRepo.save(existing);
            ra.addFlashAttribute("successMsg", "Cập nhật voucher thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    // 1. Sửa hàm XÓA
    @GetMapping("/vouchers/delete") // Xóa bớt chữ /{code}
    public String deleteVoucher(@RequestParam String code, RedirectAttributes ra) {
        if (voucherRepo.isVoucherUsedInOrder(code)) {
            ra.addFlashAttribute("errorMsg", "Mã [" + code + "] đã nằm trong lịch sử đơn hàng nên KHÔNG THỂ XÓA! Nếu muốn ngừng áp dụng, vui lòng bấm nút Sửa và chuyển sang trạng thái Ẩn.");
        } else {
            voucherRepo.deleteById(code);
            ra.addFlashAttribute("successMsg", "Đã xóa vĩnh viễn mã giảm giá [" + code + "] thành công!");
        }
        return "redirect:/admin/vouchers";
    }

    // 2. Sửa hàm LẤY DỮ LIỆU (Cho nút Sửa)
    @GetMapping("/vouchers/api") // Xóa bớt chữ /{code}
    @ResponseBody
    public Voucher getVoucherApi(@RequestParam String code) {
        return voucherRepo.findById(code).orElse(null);
    }

    // =========================================================
    // 6. QUẢN LÝ ĐÁNH GIÁ (REVIEWS)
    // =========================================================
    @GetMapping("/reviews")
    public String manageReviews(Model model) {
        // Lấy tất cả đánh giá, cái nào mới nhất thì hiện lên đầu
        model.addAttribute("reviews", reviewRepo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")));
        model.addAttribute("view", "admin/reviews-manage");
        model.addAttribute("activeTab", "reviews");
        return "admin/admin-layout";
    }

    // Nút xóa đánh giá nếu thấy nó quá "toxic" hoặc rác
    @GetMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id, RedirectAttributes ra) {
        reviewRepo.deleteById(id);
        ra.addFlashAttribute("successMsg", "Đã xóa đánh giá thành công!");
        return "redirect:/admin/reviews";
    }

    // 1. Trang danh sách tài khoản
    @GetMapping("/accounts")
    public String manageAccounts(Model model) {
        // Gọi hàm findNonAdminAccounts() thay vì cái hàm bị lỗi kia
        model.addAttribute("accounts", accountRepo.findNonAdminAccounts());
        model.addAttribute("view", "admin/accounts-manage");
        model.addAttribute("activeTab", "accounts");
        return "admin/admin-layout";
    }

    @GetMapping("/accounts/change-role/{username}")
    @Transactional
    public String changeRole(@PathVariable String username, RedirectAttributes ra) {
        Account acc = accountRepo.findByUsername(username);
        if (acc != null) {
            // Kiểm tra quyền STAFF hiện tại
            boolean isStaff = acc.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("STAFF"));

            acc.getRoles().clear(); // Xóa quyền cũ

            if (isStaff) {
                // Đang là STAFF -> Hạ xuống USER
                roleRepo.findByName("USER").ifPresent(r -> acc.getRoles().add(r));
                ra.addFlashAttribute("successMsg", "Đã hạ cấp " + username + " xuống Người dùng!");
            } else {
                // Đang là USER -> Lên STAFF
                roleRepo.findByName("STAFF").ifPresent(r -> acc.getRoles().add(r));
                ra.addFlashAttribute("successMsg", "Đã nâng cấp " + username + " thành Nhân viên!");
            }
            accountRepo.save(acc);
        }
        return "redirect:/admin/accounts";
    }

    // =========================================================
    // 8. TRUNG TÂM CSKH (LIVE CHAT)
    // =========================================================
    @GetMapping("/chat")
    public String showLiveChat(Model model) {
        model.addAttribute("view", "admin/live-chat");
        model.addAttribute("activeTab", "chat");
        return "admin/admin-layout";
    }

}