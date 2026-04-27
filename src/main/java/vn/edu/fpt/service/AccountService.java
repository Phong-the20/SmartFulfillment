package vn.edu.fpt.service;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.fpt.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AccountService {
    @Autowired private AccountRepository accountRepo;
    @Autowired private EmailService emailService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private PasswordEncoder passwor;

    @Transactional(rollbackFor = Exception.class)
    public void register(Account account) throws MessagingException {
        account.setAccountId(UUID.randomUUID());
        // Mã hóa pass
        account.setPassword_hash(passwordEncoder.encode(account.getPassword_hash()));
        // Tạo mã OTP ngẫu nhiên 6 số
        String code = String.valueOf((int)((Math.random() * 899999) + 100000));
        account.setVerificationCode(code);
        account.setVerified(false);
        account.setFailedAttempts(0);

        accountRepo.saveAndFlush(account);

        emailService.sendVerificationCode(account.getEmail(), code);
    }

    public boolean canResendOtp(String email) {
        Account account = accountRepo.findByEmail(email);
        if (account == null || account.getOtpRequestedTime() == null) return true;

        // Kiểm tra xem đã qua 60 giây chưa
        return account.getOtpRequestedTime().plusMinutes(1).isBefore(LocalDateTime.now());
    }

    public int verify(String email, String code) {
        Account account = accountRepo.findByEmail(email);
        if (account == null) return 0; // Lỗi 0: Không thấy user

        // A. Kiểm tra xem có ĐANG bị khóa không?
        if (account.getLockoutTime() != null) {
            if (account.getLockoutTime().isAfter(LocalDateTime.now())) {
                return -1; // Vẫn đang khóa
            } else {
                // Đã hết khóa -> Reset
                account.setLockoutTime(null);
                account.setFailedAttempts(0);
                // NHỚ SAVE LẠI TRẠNG THÁI RESET NÀY NẾU KHÔNG NÓ LẠI QUÊN:
                accountRepo.save(account);
            }
        }

        // B. Hết hạn OTP
        if (account.getOtpRequestedTime() != null && account.getOtpRequestedTime().plusMinutes(5).isBefore(LocalDateTime.now())) {
            return 3;
        }

        // C. Xử lý đúng / sai
        if (account.getVerificationCode() != null && account.getVerificationCode().equals(code)) {
            // Đúng mã
            account.setVerified(true);
            account.setVerificationCode(null);
            account.setFailedAttempts(0);
            account.setResendAttempts(0);
            account.setLockoutTime(null);
            accountRepo.save(account);
            return 1;
        } else {
            // SAI MÃ
            int currentFails = account.getFailedAttempts() == null ? 0 : account.getFailedAttempts();
            account.setFailedAttempts(currentFails + 1);

            // Kiểm tra xem đã đủ 5 lần chưa
            if (account.getFailedAttempts() >= 5) {
                account.setLockoutTime(LocalDateTime.now().plusMinutes(5));
                accountRepo.save(account); // Lưu khóa
                return -1;
            }

            // NẾU CHƯA ĐỦ 5 LẦN THÌ CŨNG PHẢI SAVE SỐ LẦN SAI LÊN DB:
            accountRepo.save(account); // <-- DÒNG NÀY QUAN TRỌNG NHẤT
            return 2;
        }
    }

    // 2. HÀM XỬ LÝ GỬI LẠI MÃ (CHỐNG SPAM)
    @Transactional(rollbackFor = Exception.class)
    public String resendOtp(String email) throws MessagingException {
        Account account = accountRepo.findByEmail(email);
        if (account == null) return "error";

        // Mở khóa nếu đã qua 5 phút
        if (account.getLockoutTime() != null && account.getLockoutTime().isBefore(LocalDateTime.now())) {
            account.setLockoutTime(null);
            account.setResendAttempts(0);
        }

        // Nếu đang bị khóa -> Không cho gửi
        if (account.getLockoutTime() != null && account.getLockoutTime().isAfter(LocalDateTime.now())) {
            return "locked";
        }

        // Nếu gửi quá 3 lần -> Khóa 5 phút
        if (account.getResendAttempts() >= 3) {
            account.setLockoutTime(LocalDateTime.now().plusMinutes(5));
            accountRepo.save(account);
            return "locked";
        }

        // Nếu hợp lệ -> Gửi mã mới
        String code = String.valueOf((int)((Math.random() * 899999) + 100000));
        account.setVerificationCode(code);
        account.setOtpRequestedTime(LocalDateTime.now());
        account.setResendAttempts(account.getResendAttempts() + 1); // Tăng số lần gửi lại
        account.setFailedAttempts(0);
        accountRepo.save(account);
        emailService.sendVerificationCode(account.getEmail(), code);
        return "success";
    }

    public boolean isEmailTaken(String email) {
        return accountRepo.existsByEmail(email);
    }

    public boolean isUsernameTaken(String username) {
        return accountRepo.existsByUsername(username);
    }
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String email, String newPassword) {
        Account account = accountRepo.findByEmail(email);
        if (account != null) {
            // Mã hóa mật khẩu mới trước khi lưu
            account.setPassword_hash(passwordEncoder.encode(newPassword));
            // Xóa mã OTP cũ đi cho an toàn
            account.setVerificationCode(null);
            accountRepo.save(account);
        }
    }


    // 1. Tạo token ngẫu nhiên và lưu vào DB
    public String createResetToken(String email) {
        Account account = accountRepo.findByEmail(email);
        String token = UUID.randomUUID().toString(); // Tạo chuỗi ngẫu nhiên duy nhất
        account.setResetToken(token);
        accountRepo.save(account);
        return token;
    }

    // 2. Kiểm tra token có hợp lệ không
    public Account validateResetToken(String token) {
        return accountRepo.findByResetToken(token).orElse(null);
    }

    // 3. Cập nhật pass và xóa token luôn (để không dùng lại được lần 2)
    public void updatePasswordWithToken(Account account, String newPassword) {
        // QUAN TRỌNG: Phải mã hóa mật khẩu trước khi lưu!
        String encodedPassword = passwor.encode(newPassword);

        account.setPassword_hash(encodedPassword);
        account.setResetToken(null);
        accountRepo.save(account);
    }

    @Transactional
    public Account updateProfile(UUID accountId, String fullName, String phone, String address) {
        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));

        // Cập nhật các trường thông tin cá nhân
        account.setFullName(fullName);
        account.setPhone(phone);
        account.setAddress(address);

        return accountRepo.save(account);
    }
}
