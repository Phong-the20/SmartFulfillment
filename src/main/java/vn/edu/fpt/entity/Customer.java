package vn.edu.fpt.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Customers")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullname;

    private String phone;
    private String address;
}