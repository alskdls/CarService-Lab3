package org.example.repository.jpa;

import org.example.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepairOrderRepository extends JpaRepository<Order, String> {

    @EntityGraph(attributePaths = {"jobs"})
    Optional<Order> findWithJobsById(String id);
}