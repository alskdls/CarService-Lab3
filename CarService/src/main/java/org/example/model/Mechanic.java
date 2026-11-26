
package org.example.model;

import jakarta.persistence.*;

@Entity
@Table(name = "mechanics")
public class Mechanic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String specialization;

    private boolean busy;

    protected Mechanic() {
    }

    public Mechanic(String name, String specialization) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ім'я механіка не може бути порожнім");
        }
        this.name = name;
        this.specialization = specialization;
        this.busy = false;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    @Override
    public String toString() {
        return "Mechanic{id=" + id + ", name='" + name + "', specialization='" + specialization + "', busy=" + busy + "}";
    }
}
