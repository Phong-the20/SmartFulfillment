package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByEmail(String email); // Kiểm tra email đã tồn tại chưa
    Customer findByEmail(String email); // Dùng cho phần Đăng nhập sau này
}