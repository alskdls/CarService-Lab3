
package org.example.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal partsCost;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal laborCost;

    private boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    protected Job() {
    }

    public Job(String description, BigDecimal partsCost, BigDecimal laborCost) {
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Опис роботи не може бути порожнім");
        }
        if (partsCost == null || partsCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Вартість запчастин не може бути від'ємною");
        }
        if (laborCost == null || laborCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Вартість роботи не може бути від'ємною");
        }
        this.description = description;
        this.partsCost = partsCost;
        this.laborCost = laborCost;
        this.completed = false;
    }

    void setOrder(Order order) {
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPartsCost() {
        return partsCost;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public BigDecimal getTotalCost() {
        return partsCost.add(laborCost);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public Order getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return "Job{id=" + id + ", description='" + description + "', totalCost=" + getTotalCost() + ", completed=" + completed + "}";
    }
}
