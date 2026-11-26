package org.example.service;

import org.example.model.Car;
import org.example.model.Client;
import org.example.model.Mechanic;
import org.example.model.Order;
import org.example.repository.jpa.CarRepository;
import org.example.repository.jpa.ClientRepository;
import org.example.repository.jpa.MechanicRepository;
import org.example.repository.jpa.RepairOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CarService {

    private final ClientRepository clientRepository;
    private final CarRepository carRepository;
    private final MechanicRepository mechanicRepository;
    private final RepairOrderRepository orderRepository;
    private final PriceCalculator priceCalculator;

    public CarService(
            ClientRepository clientRepository,
            CarRepository carRepository,
            MechanicRepository mechanicRepository,
            RepairOrderRepository orderRepository,
            PriceCalculator priceCalculator
    ) {
        this.clientRepository = clientRepository;
        this.carRepository = carRepository;
        this.mechanicRepository = mechanicRepository;
        this.orderRepository = orderRepository;
        this.priceCalculator = priceCalculator;
    }

    public void registerClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Клієнт не може бути нульовим.");
        }
        clientRepository.save(client);
    }

    public void registerCar(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Автомобіль не може бути нульовим.");
        }
        if (carRepository.existsByVinIgnoreCase(car.getVin())) {
            throw new IllegalArgumentException("Автомобіль із VIN-кодом '" + car.getVin() + "' вже зареєстрований в автосервісі.");
        }
        carRepository.save(car);
    }

    public void registerMechanic(Mechanic mechanic) {
        if (mechanic == null) {
            throw new IllegalArgumentException("Механік не може бути нульовим.");
        }
        mechanicRepository.save(mechanic);
    }

    public Order createOrder(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Неможливо створити замовлення без автомобіля.");
        }

        Car savedCar = carRepository.findById(car.getVin())
                .orElseThrow(() -> new IllegalArgumentException("Цей автомобіль не зареєстрований у сервісі."));

        Order order = new Order(savedCar);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(String id) {
        return orderRepository.findWithJobsById(id);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateOrderPrice(Order order) {
        return priceCalculator.calculateCommercialPrice(order);
    }

    @Transactional(readOnly = true)
    public List<Client> getClients() {
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Car> getCars() {
        return carRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Mechanic> getMechanics() {
        return mechanicRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders() {
        return orderRepository.findAll();
    }
}