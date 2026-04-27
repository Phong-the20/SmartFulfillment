package vn.edu.fpt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.entity.Warehouse;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
}
