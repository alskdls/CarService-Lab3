
package org.example.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.Version;
import java.time.LocalDateTime;

@Entity
@Table(name = "repair_orders")
public class Order {

    @Column(name = "final_price", precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Version
    private Long version;

    @Id
    @Column(nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_vin", nullable = false)
    private Car car;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Job> jobs = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mechanic_id")
    private Mechanic mechanic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    protected Order() {
    }

    public BigDecimal getFinalPrice() {
        return finalPrice;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Order(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Замовлення повинно належати автомобілю");
        }
        this.car = car;
        this.jobs = new ArrayList<>();
        this.status = OrderStatus.CREATED;
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public List<Job> getJobs() {
        return Collections.unmodifiableList(jobs);
    }

    public Mechanic getMechanic() {
        return mechanic;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void addJob(Job job) {
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Неможливо змінити завершене або скасоване замовлення");
        }
        if (job == null) {
            throw new IllegalArgumentException("Робота не може бути нульовою");
        }
        jobs.add(job);
        job.setOrder(this);
    }

    public void diagnose() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Діагностику можна виконати лише для замовлення зі статусом CREATED");
        }
        this.status = OrderStatus.DIAGNOSED;
    }

    public void approve() {
        if (status != OrderStatus.CREATED && status != OrderStatus.DIAGNOSED) {
            throw new IllegalStateException("Погодити можна лише замовлення в статусі CREATED або DIAGNOSED");
        }
        if (jobs.isEmpty()) {
            throw new IllegalStateException("Неможливо погодити порожнє замовлення (немає жодної роботи)");
        }
        this.status = OrderStatus.APPROVED;
    }

    public void assignMechanic(Mechanic mechanic) {
        if (status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Неможливо призначити механіка на завершене або скасоване замовлення");
        }
        if (mechanic == null) {
            throw new IllegalArgumentException("Механік не може бути нульовим");
        }
        if (mechanic.isBusy()) {
            throw new IllegalStateException("Цей механік уже зайнятий іншим активним замовленням");
        }

        if (this.mechanic != null) {
            this.mechanic.setBusy(false);
        }

        this.mechanic = mechanic;
        mechanic.setBusy(true);
    }

    public void startWork() {
        if (status != OrderStatus.APPROVED) {
            throw new IllegalStateException("Розпочати виконання можна лише для погодженого замовлення (APPROVED)");
        }
        if (mechanic == null) {
            throw new IllegalStateException("Неможливо розпочати замовлення без призначеного механіка");
        }
        this.status = OrderStatus.IN_PROGRESS;
    }

    public void completeJob(int index) {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Змінювати статус робіт можна лише під час виконання замовлення (IN_PROGRESS)");
        }
        if (index < 0 || index >= jobs.size()) {
            throw new IllegalArgumentException("Некоректний індекс роботи");
        }
        jobs.get(index).setCompleted(true);
    }

    public void completeOrder() {
        completeOrder(getTotalCost());
    }

    public void completeOrder(BigDecimal finalPrice) {
        if (status != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Завершить можно лишь заказ, который выполняется");
        }

        if (!jobs.stream().allMatch(Job::isCompleted)) {
            throw new IllegalStateException("Неможливо завершити замовлення: не всі роботи позначені виконаними");
        }

        if (finalPrice == null || finalPrice.signum() < 0) {
            throw new IllegalArgumentException("Некоректна підсумкова вартість");
        }

        this.finalPrice = finalPrice;
        this.completedAt = LocalDateTime.now();
        this.status = OrderStatus.COMPLETED;

        if (mechanic != null) {
            mechanic.setBusy(false);
        }
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("Неможливо скасувати вже завершене замовлення");
        }
        this.status = OrderStatus.CANCELLED;
        if (mechanic != null) {
            mechanic.setBusy(false);
        }
    }

    public BigDecimal getTotalCost() {
        return jobs.stream()
                .map(Job::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String toString() {
        return "Order{car=" + car.getVin() + ", status=" + status + ", mechanic=" + (mechanic != null ? mechanic.getName() : "None") + ", totalCost=" + getTotalCost() + "}";
    }
}
