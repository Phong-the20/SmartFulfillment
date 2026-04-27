package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.entity.Voucher;
import vn.edu.fpt.repository.VoucherRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepo;

    /**
     * Hàm kiểm tra và tính toán số tiền được giảm
     */
    public BigDecimal validateAndCalculateDiscount(String code, BigDecimal orderAmount) throws Exception {
        // 1. Kiểm tra tồn tại
        Voucher v = voucherRepo.findById(code)
                .orElseThrow(() -> new Exception("Mã giảm giá không tồn tại!"));

        // 2. Kiểm tra trạng thái hoạt động
        if (!v.isActive()) {
            throw new Exception("Mã giảm giá này hiện đã bị khóa hoặc ngừng sử dụng!");
        }

        // 3. Kiểm tra thời hạn (Bắt lỗi logic thời gian thực tế)
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(v.getStartDate())) {
            throw new Exception("Mã này chưa đến ngày bắt đầu sử dụng!");
        }
        if (now.isAfter(v.getEndDate())) {
            throw new Exception("Rất tiếc, mã giảm giá này đã hết hạn!");
        }

        // 4. Kiểm tra lượt dùng
        if (v.getUsedCount() != null && v.getUsageLimit() != null) {
            if (v.getUsedCount() >= v.getUsageLimit()) {
                throw new Exception("Mã này đã đạt giới hạn lượt sử dụng!");
            }
        }

        // 5. Kiểm tra giá trị đơn hàng tối thiểu
        if (v.getMinOrderAmount() != null && orderAmount.compareTo(v.getMinOrderAmount()) < 0) {
            throw new Exception("Đơn hàng phải từ " + String.format("%,.0f", v.getMinOrderAmount()) + "₫ mới được áp dụng mã này!");
        }

        // 6. Tính toán số tiền giảm
        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENT".equals(v.getDiscountType())) {
            // Ví dụ: Giảm 10% -> orderAmount * 0.1
            discount = orderAmount.multiply(v.getDiscountValue().divide(new BigDecimal(100)));
        } else {
            // Ví dụ: Giảm thẳng 50k
            discount = v.getDiscountValue();
        }

        // --- CHỐT CHẶN LOGIC CUỐI CÙNG ---
        // Không để tiền giảm vượt quá tiền đơn hàng (tránh trường hợp đơn hàng 0đ hoặc âm tiền)
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount.setScale(0, RoundingMode.HALF_UP); // Làm tròn về số nguyên cho tiền VNĐ
    }
}