package vn.edu.fpt.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reviews")
@Data
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    private String email;
    private String username; // Lưu người đánh giá
    private String skuCode;  // Lưu mã sản phẩm được đánh giá
    private int rating;      // Số sao (1-5)

    // Ép kiểu nvarchar để lưu tiếng Việt có dấu, độ dài 1000 cho thoải mái
    @Column(columnDefinition = "nvarchar(1000)")
    private String comment;

    private LocalDateTime createdAt;
}