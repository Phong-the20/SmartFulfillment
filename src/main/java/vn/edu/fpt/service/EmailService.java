package vn.edu.fpt.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import vn.edu.fpt.entity.Order;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    // --- 1. GỬI MÃ OTP (Giữ nguyên của ông) ---
    public void sendVerificationCode(String to, String code) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        String htmlMsg = "<div style='font-family: Arial, sans-serif; border: 1px solid #ddd; padding: 20px; border-radius: 10px; max-width: 500px;'>"
                + "<h2 style='color: #007bff;'>Xác thực tài khoản Smart-Tech</h2>"
                + "<p>Chào bạn,</p>"
                + "<p>Mã OTP của bạn là: <span style='color:red; font-size:24px; font-weight:bold; letter-spacing: 5px;'>" + code + "</span></p>"
                + "<p style='color: #555;'>Mã có hiệu lực trong <b>5 phút</b>. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>"
                + "<hr>"
                + "<b>Trân trọng,</b><br>Đội ngũ IT Smart-Tech"
                + "</div>";

        helper.setTo(to);
        helper.setSubject("Mã xác thực Smart-Tech của bạn");
        helper.setText(htmlMsg, true);
        mailSender.send(mimeMessage);
        logger.info("Đã gửi mail OTP thành công cho {}", to);
    }

    // --- 2. GỬI XÁC NHẬN ĐƠN HÀNG MỚI ---
    public void sendOrderConfirmation(Order order) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; border: 1px solid #eee; padding: 20px;'>"
                    + "<h2 style='color: #28a745;'>Cảm ơn ông đã đặt hàng tại Smart-Tech!</h2>"
                    + "<p>Chào <b>" + order.getFullname() + "</b>,</p>"
                    + "<p>Đơn hàng <b>#" + order.getOrderId() + "</b> của ông đã được hệ thống ghi nhận thành công.</p>"
                    + "<div style='background: #f8f9fa; padding: 15px; border-radius: 5px;'>"
                    + "<p><b>Tổng thanh toán:</b> <span style='color: #d9534f;'>" + String.format("%,.0f", order.getTotalAmount()) + " ₫</span></p>"
                    + "<p><b>Địa chỉ nhận hàng:</b> " + order.getShippingAddress() + "</p>"
                    + "<p><b>Phương thức thanh toán:</b> " + order.getPaymentMethod() + "</p>"
                    + "</div>"
                    + "<p>Hệ thống sẽ sớm thông báo khi đơn hàng bắt đầu được giao.</p>"
                    + "<hr><p style='font-size: 12px; color: #888;'>Smart-Tech - Công nghệ trong tầm tay</p>"
                    + "</div>";

            helper.setTo(order.getEmail());
            helper.setSubject("Xác nhận đơn hàng #" + order.getOrderId() + " - SmartTech");
            helper.setText(htmlMsg, true);
            mailSender.send(mimeMessage);
            logger.info("📧 Đã gửi mail xác nhận đơn hàng: {}", order.getOrderId());
        } catch (Exception e) {
            logger.error("❌ Lỗi gửi mail xác nhận đơn: {}", e.getMessage());
        }
    }

    // --- 3. THÔNG BÁO CẬP NHẬT TRẠNG THÁI (Giao hàng/Hủy đơn/Hoàn thành) ---
    public void sendOrderStatusUpdate(Order order) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");

            String statusColor = order.getStatus().name().equals("CANCELLED") ? "#dc3545" : "#007bff";

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; padding: 20px; border: 1px solid #eee;'>"
                    + "<h2>Cập nhật trạng thái đơn hàng</h2>"
                    + "<p>Chào " + order.getFullname() + ",</p>"
                    + "<p>Đơn hàng <b>#" + order.getOrderId() + "</b> của ông đã được chuyển sang trạng thái:</p>"
                    + "<h3 style='color: " + statusColor + ";'>" + order.getStatus().getDisplayName() + "</h3>"
                    + "<p>Vui lòng theo dõi hành trình đơn hàng trong mục <i>Lịch sử đơn hàng</i> trên Website.</p>"
                    + "<hr><p style='font-size: 11px;'>Đây là mail tự động từ Smart-Tech.</p>"
                    + "</div>";

            helper.setTo(order.getEmail());
            helper.setSubject("Cập nhật đơn hàng #" + order.getOrderId() + " - " + order.getStatus().getDisplayName());
            helper.setText(htmlMsg, true);
            mailSender.send(mimeMessage);
            logger.info("📧 Đã gửi mail cập nhật trạng thái đơn: {} -> {}", order.getOrderId(), order.getStatus());
        } catch (Exception e) {
            logger.error("❌ Lỗi gửi mail cập nhật trạng thái: {}", e.getMessage());
        }
    }
}