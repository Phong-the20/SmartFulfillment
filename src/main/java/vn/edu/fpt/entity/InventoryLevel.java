package vn.edu.fpt.entity;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Inventory_Level")
@Data
public class InventoryLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer inventoryId;

    @Column(name = "sku_code", nullable = false, length = 50)
    private String skuCode;

    @Column(name = "warehouse_id", nullable = false, length = 50)
    private String warehouseId;

    @Column(name = "location_zone", nullable = false, length = 50)
    private String locationZone;

    @Column(name = "physical_qty", nullable = false)
    private Integer physicalQty;

    @Column(name = "available_qty", nullable = false)
    private Integer availableQty;

    @Column(name = "last_updated", insertable = false, updatable = false)
    private LocalDateTime lastUpdated;
}