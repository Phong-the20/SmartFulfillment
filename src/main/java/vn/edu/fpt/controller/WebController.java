package vn.edu.fpt.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.repository.InventoryLevelRepository;

@Controller // Lưu ý: Dùng @Controller chứ không phải @RestController
public class WebController {

    @Autowired
    private InventoryLevelRepository inventoryRepo;

    @GetMapping("/dashboard")
    public String viewDashboard(Model model) {
        // Lấy toàn bộ tồn kho để hiển thị lên bảng
        model.addAttribute("inventoryList", inventoryRepo.findAll());
        return "dashboard"; // Sẽ tìm file dashboard.html trong thư mục templates
    }
}