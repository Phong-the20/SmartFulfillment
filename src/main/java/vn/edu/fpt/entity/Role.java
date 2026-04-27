package vn.edu.fpt.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Roles")
@Data
public class Role {
    @Id
    @Column(name = "role_id") // Giữ nguyên tên cột trong DB là role_id
    private String name;      // Đổi tên biến thành name (chứa: ADMIN, STAFF, USER...)

    @Column(name = "role_name")
    private String displayName; // Chứa tên hiển thị tiếng Việt (Quản trị viên, Nhân viên...)

    private String description;
}