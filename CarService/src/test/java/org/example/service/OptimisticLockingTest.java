
package org.example.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.RollbackException;

import org.example.model.Car;
import org.example.model.Client;
import org.example.model.Order;
import org.example.repository.jpa.CarRepository;
import org.example.repository.jpa.ClientRepository;
import org.example.repository.jpa.RepairOrderRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OptimisticLockingTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RepairOrderRepository orderRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void shouldDetectConcurrentModification() {

        Client client = clientRepository.save(
                new Client("Тест блокування", "+380501234567")
        );

        String vin = "T" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 16);

        Car car = carRepository.save(
                new Car(vin, "Toyota", "Camry", 2021, client)
        );

        Order order = orderRepository.save(new Order(car));
        String orderId = order.getId();

        EntityManager em1 = entityManagerFactory.createEntityManager();
        EntityManager em2 = entityManagerFactory.createEntityManager();

        EntityTransaction tx1 = em1.getTransaction();
        EntityTransaction tx2 = em2.getTransaction();

        try {
            tx1.begin();
            tx2.begin();

            Order firstOrder = em1.find(Order.class, orderId);
            Order secondOrder = em2.find(Order.class, orderId);

            firstOrder.diagnose();
            secondOrder.diagnose();

            tx1.commit();

            assertThrows(
                    RollbackException.class,
                    tx2::commit
            );

        } finally {
            if (tx1.isActive()) {
                tx1.rollback();
            }

            if (tx2.isActive()) {
                tx2.rollback();
            }

            em1.close();
            em2.close();
        }
    }
}
