package org.example.service;

import org.example.model.Job;
import org.example.model.Mechanic;
import org.example.model.Order;
import org.example.repository.jpa.MechanicRepository;
import org.example.repository.jpa.RepairOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class OrderTransactionService {

    private final RepairOrderRepository orderRepository;
    private final MechanicRepository mechanicRepository;
    private final PriceCalculator priceCalculator;

    public OrderTransactionService(
            RepairOrderRepository orderRepository,
            MechanicRepository mechanicRepository,
            PriceCalculator priceCalculator
    ) {
        this.orderRepository = orderRepository;
        this.mechanicRepository = mechanicRepository;
        this.priceCalculator = priceCalculator;
    }

    @Transactional
    public Order prepareOrderForCompletion(String orderId, Long mechanicId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Замовлення не знайдено: " + orderId
                ));

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Механіка не знайдено: " + mechanicId
                ));

        order.diagnose();
        order.approve();
        order.assignMechanic(mechanic);
        order.startWork();

        IntStream.range(0, order.getJobs().size())
                .forEach(order::completeJob);

        return order;
    }

    @Transactional
    public Order completeOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Замовлення не знайдено: " + orderId
                ));

        BigDecimal finalPrice = priceCalculator.calculateCommercialPrice(order);

        order.completeOrder(finalPrice);

        return orderRepository.save(order);
    }

    @Transactional
    public Order addJobsBatch(String orderId, List<Job> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException("Список робіт не може бути порожнім");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Замовлення не знайдено: " + orderId
                ));

        jobs.stream().forEach(order::addJob);

        return orderRepository.save(order);
    }
}