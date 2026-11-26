
package org.example;

import org.example.model.Car;
import org.example.model.Client;
import org.example.model.Job;
import org.example.model.Mechanic;
import org.example.model.Order;
import org.example.service.CarService;
import org.example.service.OrderTransactionService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class CarServiceRunner implements CommandLineRunner {

    private static final String PREVIOUS_ORDER_ID =
            "724d668d-5c67-4d42-85a2-fc8b0a880384";

    private final CarService carService;
    private final OrderTransactionService transactionService;

    public CarServiceRunner(
            CarService carService,
            OrderTransactionService transactionService
    ) {
        this.carService = carService;
        this.transactionService = transactionService;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n=== ДЕМОНСТРАЦІЯ РОБОТИ АВТОСЕРВІСУ ===\n");

        try {
            System.out.println("--- 1. Перевірка даних із попереднього запуску ---");

            Order previousOrder = carService.getOrderById(PREVIOUS_ORDER_ID)
                    .orElse(null);

            if (previousOrder != null) {
                System.out.println("Попереднє замовлення знайдено в базі даних.");
                System.out.println("Ідентифікатор: " + previousOrder.getId());
                System.out.println("Статус: " + previousOrder.getStatus());
                System.out.println("Кількість робіт: " + previousOrder.getJobs().size());
                System.out.println("Підсумкова вартість: " + previousOrder.getFinalPrice() + " грн");
                System.out.println("Дата завершення: " + previousOrder.getCompletedAt());
            } else {
                System.out.println("Замовлення з попереднього запуску не знайдено.");
            }

            System.out.println("\n--- 2. Реєстрація клієнта ---");

            Client client = new Client(
                    "Іван Петренко",
                    "+380501234567"
            );

            carService.registerClient(client);

            System.out.println("Клієнта успішно зареєстровано.");

            System.out.println("\n--- 3. Реєстрація автомобіля ---");

            Car car = new Car(
                    "1HGCM82633A123456",
                    "Toyota",
                    "Camry",
                    2021,
                    client
            );

            try {
                carService.registerCar(car);
                System.out.println("Автомобіль успішно зареєстровано.");
            } catch (IllegalArgumentException e) {
                System.out.println("Автомобіль із таким VIN уже зареєстровано.");
                System.out.println("Використовуємо наявний запис із бази даних.");
            }

            System.out.println("\n--- 4. Реєстрація механіка ---");

            Mechanic mechanic = new Mechanic(
                    "Олег Майстер",
                    "Ходова частина"
            );

            carService.registerMechanic(mechanic);

            System.out.println("Механіка успішно зареєстровано.");

            System.out.println("\n--- 5. Створення замовлення ---");

            Order order = carService.createOrder(car);

            String orderId = order.getId();

            System.out.println("Створено замовлення: " + orderId);
            System.out.println("Поточний статус: " + order.getStatus());

            System.out.println("\n--- 6. Додавання робіт ---");

            List<Job> jobs = List.of(
                    new Job(
                            "Заміна гальмівних колодок",
                            new BigDecimal("1500.00"),
                            new BigDecimal("800.00")
                    ),
                    new Job(
                            "Заміна мастила",
                            new BigDecimal("1000.00"),
                            new BigDecimal("400.00")
                    )
            );

            transactionService.addJobsBatch(orderId, jobs);

            Order orderWithJobs = carService.getOrderById(orderId)
                    .orElseThrow();

            System.out.println("Кількість доданих робіт: " + orderWithJobs.getJobs().size());
            System.out.println("Базова вартість: " + orderWithJobs.getTotalCost() + " грн");

            System.out.println("\n--- 7. Підготовка замовлення до завершення ---");

            transactionService.prepareOrderForCompletion(
                    orderId,
                    mechanic.getId()
            );

            System.out.println("Діагностику виконано.");
            System.out.println("Замовлення погоджено.");
            System.out.println("Механіка призначено.");
            System.out.println("Усі роботи виконано.");

            System.out.println("\n--- 8. Завершення замовлення ---");

            Order completedOrder = transactionService.completeOrder(orderId);

            System.out.println("Замовлення успішно завершено.");
            System.out.println("Фінальний статус: " + completedOrder.getStatus());
            System.out.println("Підсумкова вартість: " + completedOrder.getFinalPrice() + " грн");
            System.out.println("Час завершення: " + completedOrder.getCompletedAt());

            System.out.println("\n--- 9. Перевірка негативного сценарію ---");

            try {
                completedOrder.addJob(
                        new Job(
                                "Миття кузова",
                                BigDecimal.ZERO,
                                new BigDecimal("300.00")
                        )
                );
            } catch (IllegalStateException e) {
                System.out.println("Помилку успішно перехоплено: " + e.getMessage());
            }

            System.out.println("\n--- 10. Перевірка збереження нового замовлення ---");

            Order savedOrder = carService.getOrderById(orderId)
                    .orElseThrow();

            System.out.println("Замовлення знайдено в базі даних.");
            System.out.println("Ідентифікатор: " + savedOrder.getId());
            System.out.println("Статус: " + savedOrder.getStatus());
            System.out.println("Кількість робіт: " + savedOrder.getJobs().size());
            System.out.println("Підсумкова вартість: " + savedOrder.getFinalPrice() + " грн");
            System.out.println("Дата завершення: " + savedOrder.getCompletedAt());

        } catch (Exception e) {
            System.out.println("\nПід час демонстрації виникла помилка: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== ДЕМОНСТРАЦІЮ ЗАВЕРШЕНО ===");
    }
}
