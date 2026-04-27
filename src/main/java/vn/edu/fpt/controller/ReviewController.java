package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.entity.Review;
import vn.edu.fpt.repository.AccountRepository;
import vn.edu.fpt.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/review")
public class ReviewController {

    @Autowired private ReviewRepository reviewRepo;
    @Autowired private AccountRepository accountRepo;

    // Bộ lọc từ ngữ
    private static final List<String> BAD_WORDS = Arrays.asList(
            "đm", "dm", "vcl", "vl", "vcl", "đmm", "cl", "đệch", "đệt",
            "ngu", "óc chó", "đần", "vô học", "cút", "khốn nạn", "chó đẻ",
            "mịa", "đéo", "đíu", "mẹ kiếp", "đậu xanh", "lừa đảo"
    );

    @PostMapping("/submit")
    public String submitReview(
            @RequestParam String skuCode,
            @RequestParam int rating,
            @RequestParam String comment,
            Authentication auth,
            RedirectAttributes ra) {

        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        // 1. VALIDATION ĐỘ DÀI (1 - 200 từ)
        String trimmedComment = comment.trim();
        if (trimmedComment.isEmpty()) {
            ra.addFlashAttribute("errorMsg", "Vui lòng nhập nội dung đánh giá!");
            return "redirect:/products/detail/" + skuCode;
        }

        String[] words = trimmedComment.split("\\s+");
        if (words.length < 1 || words.length > 200) {
            ra.addFlashAttribute("errorMsg", "Nội dung đánh giá phải từ 1 đến 200 từ. (Ông đang viết " + words.length + " từ)");
            return "redirect:/products/detail/" + skuCode;
        }

        // 2. LỌC TỪ NGỮ XÚC PHẠM
        String censoredComment = trimmedComment;
        for (String badWord : BAD_WORDS) {
            censoredComment = censoredComment.replaceAll("(?i)(?U)\\b" + badWord + "\\b", "***");
        }

        // 3. LẤY EMAIL TỪ SECURITY / DATABASE
        String email = "";
        if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
            email = ((org.springframework.security.oauth2.core.user.OAuth2User) auth.getPrincipal()).getAttribute("email");
        } else {
            vn.edu.fpt.entity.Account acc = accountRepo.findByUsername(auth.getName());
            if (acc != null) email = acc.getEmail();
        }

        // 4. LƯU DATABASE
        Review review = new Review();
        review.setEmail(email);
        review.setUsername(auth.getName());
        review.setSkuCode(skuCode);
        review.setRating(rating);
        review.setComment(censoredComment); // Nhớ truyền censoredComment đã được lọc
        review.setCreatedAt(LocalDateTime.now());

        reviewRepo.save(review);

        ra.addFlashAttribute("successMsg", "Cảm ơn ông đã đánh giá!");
        return "redirect:/products/detail/" + skuCode;
    }
}