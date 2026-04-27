package vn.edu.fpt.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "Accounts")
@Data
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Dùng UUID chính thống cho SQL Server
    @Column(name = "account_id")
    private UUID accountId;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 5, max = 20, message = "Tên đăng nhập phải từ 5-20 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Tên đăng nhập chỉ chứa chữ cái và số")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password_hash;

    @NotBlank(message = "Họ tên không được để trống")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "Họ tên chỉ được chứa chữ cái và khoảng trắng")
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải bắt đầu bằng số 0 và có đúng 10 chữ số")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    // --- LOGIC BẢO MẬT & OTP ---
    private boolean isVerified = false;
    private String verificationCode;
    private Integer failedAttempts = 0;
    private Integer resendAttempts = 0;
    private LocalDateTime otpRequestedTime;
    private LocalDateTime lockoutTime;
    private String resetToken;

    @Transient
    private String confirmPassword;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "Account_Roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}