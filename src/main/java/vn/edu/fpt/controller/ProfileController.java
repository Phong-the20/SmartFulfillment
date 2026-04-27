package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.Account;
import vn.edu.fpt.repository.AccountRepository;
import vn.edu.fpt.service.AccountService;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepo;

    @GetMapping
    public String showProfile() {
        // KHÔNG CẦN CODE GÌ Ở ĐÂY NỮA!
        // 1. Nếu chưa đăng nhập: SecurityConfig tự đá về /login
        // 2. Nếu đã đăng nhập: GlobalControllerAdvice đã tự đẩy 'user' ra ngoài HTML rồi
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String phone,
                                @RequestParam String address,
                                RedirectAttributes ra) {

        if (!phone.matches("^0\\d{9}$")) {
            ra.addFlashAttribute("errorMsg", "Số điện thoại không hợp lệ!");
            return "redirect:/profile";
        }

        // Lấy thông tin user đang đăng nhập trực tiếp từ Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = "";

        if (auth.getPrincipal() instanceof OAuth2User) { // Khách đăng nhập bằng Google
            username = ((OAuth2User) auth.getPrincipal()).getAttribute("email");
            Account user = accountRepo.findByEmail(username);
            accountService.updateProfile(user.getAccountId(), fullName, phone, address);
        } else { // Khách đăng nhập bằng tài khoản web
            username = auth.getName();
            Account user = accountRepo.findByUsername(username);
            accountService.updateProfile(user.getAccountId(), fullName, phone, address);
        }

        ra.addFlashAttribute("successMsg", "Cập nhật hồ sơ thành công!");
        return "redirect:/profile";
    }
}