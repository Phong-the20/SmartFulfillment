package vn.edu.fpt.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.ProductSKU;

import java.util.List;

@Repository
public interface ProductSKURepository extends JpaRepository<ProductSKU, String> {

    List<ProductSKU> findByProductNameContainingIgnoreCase(String productName);

    List<ProductSKU> findByCategory(String category);

    // ✅ CHỈ GIỮ LẠI HÀM NÀY ĐỂ PHÂN TRANG
    Page<ProductSKU> findByProductNameContainingIgnoreCaseAndBasePriceBetweenOrderByBasePriceAsc(
            String keyword, Double min, Double max, Pageable pageable
    );
}