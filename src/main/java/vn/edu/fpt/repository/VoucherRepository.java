package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.entity.Voucher;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    // Kiểm tra xem mã này đã có trong đơn hàng nào chưa
    @Query("SELECT COUNT(o) > 0 FROM Order o WHERE o.voucher.voucherCode = :code")
    boolean isVoucherUsedInOrder(@Param("code") String code);
}