package org.example.repository.jpa;

import org.example.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, String> {
    boolean existsByVinIgnoreCase(String vin);
}