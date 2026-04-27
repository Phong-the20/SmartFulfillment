package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Account findByEmail(String email);
    Account findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<Account> findByResetToken(String resetToken);

    // ✅ CHỈ GIỮ LẠI CÂU NÀY (Vì mình tự viết SQL nên Spring không bắt bẻ thuộc tính)
    @Query("SELECT DISTINCT a FROM Account a LEFT JOIN a.roles r " +
            "WHERE r.name IS NULL OR r.name != 'ADMIN'")
    List<Account> findNonAdminAccounts();
}