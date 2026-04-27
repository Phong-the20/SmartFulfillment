package vn.edu.fpt.controller;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.entity.ProductSKU;
import vn.edu.fpt.repository.InventoryRepository;
import vn.edu.fpt.repository.ProductSKURepository;
import vn.edu.fpt.service.CloudinaryService;

import java.io.IOException;
import java.math.BigDecimal; // Import thêm cái này
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProductSKURepository productRepo;

    @Autowired
    private InventoryRepository inventoryRepo;

    // Đã sửa: Trả về List<ProductSKU> thay vì List<Repository>
    @GetMapping
    public List<ProductSKU> getAllProducts() {
        return productRepo.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            @RequestParam String skuCode,
            @RequestParam String productName,
            @RequestParam BigDecimal basePrice, // Đã sửa: Dùng luôn BigDecimal để đồng bộ
            @RequestParam String category,
            @RequestParam MultipartFile imageFile) throws IOException {

        // Bước 1: Đẩy ảnh lên Cloudinary
        String imageUrl = cloudinaryService.uploadImage(imageFile);

        // Bước 2: Lưu vào Database
        ProductSKU product = new ProductSKU(); // Đã sửa: Viết hoa chữ SKU
        product.setSkuCode(skuCode);
        product.setProductName(productName);
        product.setBasePrice(basePrice); // Giờ thì mượt mà không lỗi nữa
        product.setCategory(category);
        product.setImageUrl(imageUrl); // Hết lỗi báo đỏ

        // Mẹo chống lỗi: Form không có cân nặng, nhưng DB bắt buộc (nullable = false)
        // Nên mình set tạm bằng 0 để không bị văng lỗi 500
        product.setWeightGram(0);

        productRepo.save(product);

        return ResponseEntity.ok("Đã thêm sản phẩm " + productName + " thành công!");
    }

    @DeleteMapping("/{skuCode}")
    @Transactional // Thêm cái này để đảm bảo xóa hết hoặc không xóa gì (tránh lỗi nửa chừng)
    public ResponseEntity<?> deleteProduct(@PathVariable String skuCode) throws IOException {
        // 1. Tìm sản phẩm
        Optional<ProductSKU> productOpt = productRepo.findById(skuCode);
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ProductSKU product = productOpt.get();

        // 2. Xóa các bản ghi liên quan trong bảng Inventory trước (Quan trọng nhất nè)
        // Giả sử ông có hàm deleteBySkuCode trong InventoryRepository
        // Nếu chưa có, ông dùng findBySku rồi delete hết list đó
        inventoryRepo.deleteByProduct(product);

        // 3. Xóa ảnh trên Cloudinary
        if (product.getImageUrl() != null) {
            cloudinaryService.deleteImage(product.getImageUrl());
        }

        // 4. Cuối cùng mới xóa sản phẩm trong Product_SKU
        productRepo.delete(product);

        return ResponseEntity.ok("Đã dọn dẹp sạch sẽ sản phẩm " + skuCode);
    }


}