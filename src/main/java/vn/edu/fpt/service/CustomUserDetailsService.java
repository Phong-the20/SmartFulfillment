package vn.edu.fpt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.fpt.entity.Account;
import vn.edu.fpt.repository.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.stream.Collectors;

import java.time.LocalDateTime;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepo.findByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng!");
        }

        // --- HÀNG RÀO BẢO MẬT ---
        // Nếu chưa xác thực (isVerified = false), Spring Security sẽ ném DisabledException
        boolean enabled = account.isVerified();

        // Kiểm tra xem có đang bị khóa không (nếu muốn chặn cả ở trang Login)
        boolean accountNonLocked = true;
        if (account.getLockoutTime() != null && account.getLockoutTime().isAfter(LocalDateTime.now())) {
            accountNonLocked = false;
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(account.getUsername())
                .password(account.getPassword_hash())
                .disabled(!enabled)
                .accountLocked(!accountNonLocked)
                // --- SỬA DÒNG NÀY ---
                .authorities(account.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName())) // Dùng getName() nhé!
                        .collect(Collectors.toList()))
                .build();
    }
}