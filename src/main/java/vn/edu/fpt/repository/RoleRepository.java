package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.entity.Role;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    // Tìm theo tên (chính là Primary Key của ông)
    Optional<Role> findByName(String name);
}