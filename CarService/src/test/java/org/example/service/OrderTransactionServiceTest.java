
package org.example.service;

import org.example.model.Car;
import org.example.model.Client;
import org.example.model.Job;
import org.example.model.Order;
import org.example.repository.jpa.CarRepository;
import org.example.repository.jpa.ClientRepository;
import org.example.repository.jpa.RepairOrderRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderTransactionServiceTest {

    @Autowired
    private OrderTransactionService transactionService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RepairOrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void shouldRollbackAllJobsWhenOneJobIsInvalid() {
        Client client = clientRepository.save(
                new Client("Тестовий клієнт", "+380501234567")
        );

        String vin = "T" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16);

        Car car = carRepository.save(
                new Car(vin, "Toyota", "Camry", 2021, client)
        );

        Order order = orderRepository.save(new Order(car));

        entityManager.flush();

        String orderId = order.getId();

        Job validJob = new Job(
                "Діагностика",
                new BigDecimal("200.00"),
                new BigDecimal("800.00")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.addJobsBatch(
                        orderId,
                        Arrays.asList(validJob, null)
                )
        );

        entityManager.clear();

        Order savedOrder = orderRepository.findWithJobsById(orderId)
                .orElseThrow();

        assertTrue(savedOrder.getJobs().isEmpty());
    }
}
