package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    protected Client() {
    }

    public Client(String name, String phone) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ім'я клієнта не може бути порожнім");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Телефон клієнта не може бути порожнім");
        }
        this.name = name;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return "Client{id=" + id + ", name='" + name + "', phone='" + phone + "'}";
    }
}
