
package org.example.repository.jpa;

import jakarta.persistence.EntityManager;
import org.example.model.Car;
import org.example.model.Client;
import org.example.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PostgreSqlPersistenceTest {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RepairOrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldSaveAndLoadOrderFromPostgreSql() {
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

        entityManager.clear();

        Order foundOrder = orderRepository.findWithJobsById(orderId)
                .orElseThrow();

        assertEquals(orderId, foundOrder.getId());
        assertEquals(vin, foundOrder.getCar().getVin());
    }
}
