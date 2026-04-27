package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.fpt.service.VoucherService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherRestController {

    @Autowired
    private VoucherService voucherService;

    @PostMapping("/apply")
    public ResponseEntity<?> applyVoucher(@RequestParam String code, @RequestParam BigDecimal totalAmount) {
        try {
            // Gọi Service "xịn" mà anh em mình vừa viết ở bước trước
            BigDecimal discount = voucherService.validateAndCalculateDiscount(code, totalAmount);

            // Trả về kết quả thành công kèm số tiền được giảm
            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "discount", discount,
                    "message", "Áp dụng mã thành công!"
            ));
        } catch (Exception e) {
            // Trả về lỗi (hết hạn, không đủ điều kiện...) để hiện thông báo
            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}