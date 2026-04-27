package vn.edu.fpt.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice // Đánh dấu đây là bộ xử lý lỗi toàn hệ thống
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi "Không tìm thấy tài nguyên" (Cái mình vừa tạo ở bước 1)
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFound(ResourceNotFoundException ex, RedirectAttributes ra) {
        log.error("❌ LỖI KHÔNG TÌM THẤY: {}", ex.getMessage());
        ra.addFlashAttribute("errorMsg", ex.getMessage());
        return "redirect:/error-page"; // Hoặc trang nào ông muốn
    }

    // 2. Xử lý tất cả các lỗi logic chung (Runtime)
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, RedirectAttributes ra) {
        log.error("🔥 LỖI HỆ THỐNG: {}", ex.getMessage());
        ra.addFlashAttribute("errorMsg", "Hệ thống đang bận: " + ex.getMessage());
        return "redirect:/admin/orders"; // Ví dụ: đá về trang quản lý kèm thông báo lỗi
    }

    // 3. Xử lý lỗi cực nặng (Exception chung)
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        log.error("🚨 LỖI NGHIÊM TRỌNG: ", ex);
        model.addAttribute("message", "Đã có sự cố xảy ra. Vui lòng liên hệ Admin!");
        return "error"; // Trả về file error.html trong templates
    }
}