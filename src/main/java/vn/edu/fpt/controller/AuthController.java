package vn.edu.fpt.controller;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.Account;
import vn.edu.fpt.repository.AccountRepository;
import vn.edu.fpt.service.AccountService;
import vn.edu.fpt.service.CustomUserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class AuthController {

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    // ==========================================================
    // 1. TRANG ĐĂNG NHẬP / ĐĂNG KÝ (Dùng chung file login.html)
    // ==========================================================
    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("account")) {
            model.addAttribute("account", new Account());
        }
        return "login";
    }

    // ==========================================================
    // 2. LOGIC ĐĂNG KÝ (Hợp nhất OTP & Lưu thông tin cá nhân)
    // ==========================================================
    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("account") Account account,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes) {

        // --- BƯỚC 1: Kiểm tra lỗi định dạng (Validation) ---
        if (result.hasErrors()) {
            String errorMessage = result.getFieldError().getDefaultMessage();
            redirectAttributes.addFlashAttribute("account", account);
            return buildErrorRedirect(redirectAttributes, errorMessage);
        }

        // --- BƯỚC 2: Kiểm tra khớp mật khẩu ---
        if (!account.getPassword_hash().equals(account.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("account", account);
            return buildErrorRedirect(redirectAttributes, "Mật khẩu xác nhận không khớp!");
        }

        // --- BƯỚC 3: Kiểm tra Email đã tồn tại chưa ---
        Account existingByEmail = accountRepo.findByEmail(account.getEmail());
        if (existingByEmail != null) {
            if (!existingByEmail.isVerified()) {
                // Nếu chưa xác thực -> Gửi lại OTP rồi bắt đi verify
                try {
                    accountService.resendOtp(account.getEmail());
                    return "redirect:/verify?email=" + account.getEmail() + "&resend=success";
                } catch (MessagingException e) {
                    return "redirect:/verify?email=" + account.getEmail() + "&error=system";
                }
            } else {
                redirectAttributes.addFlashAttribute("account", account);
                return buildErrorRedirect(redirectAttributes, "Email này đã được sử dụng!");
            }
        }

        // --- BƯỚC 4: Kiểm tra Username ---
        if (accountRepo.findByUsername(account.getUsername()) != null) {
            redirectAttributes.addFlashAttribute("account", account);
            return buildErrorRedirect(redirectAttributes, "Tên đăng nhập này đã có người xài!");
        }

        // --- BƯỚC 5: Đăng ký & Gửi OTP ---
        try {
            // Service này sẽ lưu Account (bao gồm fullname, phone, address) và gửi OTP
            accountService.register(account);
            return "redirect:/verify?email=" + account.getEmail();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("account", account);
            return buildErrorRedirect(redirectAttributes, "Có lỗi xảy ra, vui lòng thử lại sau!");
        }
    }

    // ==========================================================
    // 3. LOGIC XÁC THỰC OTP (Verify)
    // ==========================================================
    @GetMapping("/verify")
    public String showVerifyPage(@RequestParam String email,
                                 @RequestParam(required = false) String error,
                                 Model model) {
        model.addAttribute("email", email);
        model.addAttribute("error", error);

        Account account = accountRepo.findByEmail(email);
        if (account != null && account.getLockoutTime() != null) {
            long lockoutEndMillis = account.getLockoutTime()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant().toEpochMilli();
            model.addAttribute("lockoutEnd", lockoutEndMillis);
        }
        return "verify";
    }

    @PostMapping("/verify")
    public String processVerify(@RequestParam String email, @RequestParam String code) {
        int result = accountService.verify(email, code);
        if (result == 1) return "redirect:/login?verified=true";
        if (result == -1) return "redirect:/verify?email=" + email + "&error=locked";
        if (result == 2) return "redirect:/verify?email=" + email + "&error=wrong_code";
        return "redirect:/verify?email=" + email + "&error=system";
    }

    @PostMapping("/resend-otp")
    public String processResendOtp(@RequestParam String email) {
        try {
            String result = accountService.resendOtp(email);
            if (result.equals("locked")) return "redirect:/verify?email=" + email + "&error=locked";
            if (result.equals("success")) return "redirect:/verify?email=" + email + "&resend=success";
        } catch (MessagingException e) {
            return "redirect:/verify?email=" + email + "&error=system";
        }
        return "redirect:/verify?email=" + email;
    }

    // ==========================================================
    // 4. LOGIC QUÊN MẬT KHẨU (Forgot Password)
    // ==========================================================
    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes ra) {
        Account account = accountRepo.findByEmail(email);
        if (account == null) {
            ra.addFlashAttribute("error", "Email không tồn tại trong hệ thống!");
            return "redirect:/forgot-password";
        }
        try {
            accountService.resendOtp(email);
            return "redirect:/verify-reset?email=" + email;
        } catch (MessagingException e) {
            ra.addFlashAttribute("error", "Lỗi gửi mail, thử lại sau!");
            return "redirect:/forgot-password";
        }
    }

    @GetMapping("/verify-reset")
    public String showVerifyReset(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verify-reset";
    }

    @PostMapping("/verify-reset")
    public String processVerifyReset(@RequestParam String email, @RequestParam String code, RedirectAttributes ra) {
        int result = accountService.verify(email, code);
        if (result == 1) {
            String token = accountService.createResetToken(email);
            return "redirect:/reset-password?token=" + token;
        }
        ra.addFlashAttribute("error", "Mã xác thực không đúng!");
        return "redirect:/verify-reset?email=" + email;
    }

    @GetMapping("/reset-password")
    public String showResetPassword(@RequestParam String token, Model model) {
        Account account = accountService.validateResetToken(token);
        if (account == null) return "redirect:/login?error=invalid_token";
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       RedirectAttributes ra) {
        Account account = accountService.validateResetToken(token);
        if (account == null) return "redirect:/login?error=invalid_token";

        if (!password.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu không khớp!");
            return "redirect:/reset-password?token=" + token;
        }

        accountService.updatePasswordWithToken(account, password);
        ra.addFlashAttribute("msg", "Đổi mật khẩu thành công!");
        return "redirect:/login";
    }

    // --- HÀM BỔ TRỢ: Xây dựng link redirect kèm thông báo lỗi ---
    private String buildErrorRedirect(RedirectAttributes ra, String msg) {
        try {
            String encodedMsg = URLEncoder.encode(msg, StandardCharsets.UTF_8.toString());
            return "redirect:/login?error=custom&msg=" + encodedMsg + "&openRegister=true";
        } catch (Exception e) {
            return "redirect:/login?error=true&openRegister=true";
        }
    }
    @PostMapping("/login")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpServletRequest request,   // Thêm cái này
                               HttpServletResponse response, // Thêm cái này
                               jakarta.servlet.http.HttpSession session,
                               RedirectAttributes ra) {

        Account account = accountRepo.findByUsername(username);

        // Kiểm tra pass ( plain text để test )
        if (account == null || !account.getPassword_hash().equals(password)) {
            return "redirect:/login?error=true&msg=" + URLEncoder.encode("Sai tài khoản hoặc mật khẩu!", StandardCharsets.UTF_8);
        }

        if (!account.isVerified()) {
            return "redirect:/verify?email=" + account.getEmail() + "&error=not_verified";
        }

        // --- ĐOẠN "CẤP THẺ BÀI" CHUẨN CỦA PHONG ---
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Lưu vào Context tạm thời
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // LƯU CHÍNH THỨC VÀO SESSION (Để không bị đá ra trang login)
        securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

        // Lưu user vào session để dùng ở UI
        session.setAttribute("loggedInUser", account);

        return "redirect:/";
    }
}