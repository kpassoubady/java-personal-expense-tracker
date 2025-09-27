package com.expensetracker.app;

import com.expensetracker.app.entity.Category;
import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.repository.CategoryRepository;
import com.expensetracker.app.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Seeds initial data into the database when the application starts.
 * Disabled during testing to avoid conflicts with test data.
 */
@Component
@Profile("!test")  // Don't run this component when test profile is active
public class DataSeeder {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void seedData() {
        if (categoryRepository.count() > 0 && expenseRepository.count() > 0) {
            logger.info("Sample data already exists. Skipping seeding.");
            return;
        }
        logger.info("Seeding sample categories...");
        List<Category> categories = createSampleCategories();
        List<Category> savedCategories = categoryRepository.saveAll(categories);
        logger.info("Seeded categories: {}", savedCategories.size());

        logger.info("Seeding sample expenses...");
        List<Expense> expenses = createSampleExpenses(savedCategories);
        List<Expense> savedExpenses = expenseRepository.saveAll(expenses);
        logger.info("Seeded expenses: {}", savedExpenses.size());
    }

    private List<Category> createSampleCategories() {
        return Arrays.asList(
            new Category("Food", "Groceries, restaurants, snacks", "#28a745", "fas fa-utensils"),
            new Category("Transport", "Public transport, fuel, taxi", "#007bff", "fas fa-car"),
            new Category("Entertainment", "Movies, games, events", "#e83e8c", "fas fa-film"),
            new Category("Utilities", "Electricity, water, internet", "#fd7e14", "fas fa-bolt"),
            new Category("Health", "Medicine, doctor, gym", "#dc3545", "fas fa-heart"),
            new Category("Shopping", "Clothes, electronics, gifts", "#6f42c1", "fas fa-shopping-bag"),
            new Category("Education", "Books, courses, tuition", "#20c997", "fas fa-graduation-cap")
        );
    }

    private List<Expense> createSampleExpenses(List<Category> categories) {
        if (categories == null || categories.size() < 7) {
            return Arrays.asList(); // Return empty list if not enough categories
        }
        LocalDate today = LocalDate.now();
        return Arrays.asList(
            new Expense("Lunch at cafe", new BigDecimal("25.50"), today.minusDays(1), categories.get(0)),
            new Expense("Taxi ride", new BigDecimal("60.00"), today.minusDays(2), categories.get(1)),
            new Expense("Movie ticket", new BigDecimal("15.00"), today.minusDays(3), categories.get(2)),
            new Expense("Internet bill", new BigDecimal("80.00"), today.minusDays(4), categories.get(3)),
            new Expense("Pharmacy", new BigDecimal("30.00"), today.minusDays(5), categories.get(4)),
            new Expense("New headphones", new BigDecimal("120.00"), today.minusDays(6), categories.get(5)),
            new Expense("Online course fee", new BigDecimal("45.00"), today.minusDays(7), categories.get(6)),
            new Expense("Grocery shopping", new BigDecimal("90.00"), today.minusDays(8), categories.get(0)),
            new Expense("Fuel refill", new BigDecimal("50.00"), today.minusDays(9), categories.get(1)),
            new Expense("Concert ticket", new BigDecimal("75.00"), today.minusDays(10), categories.get(2)),
            new Expense("Electricity bill", new BigDecimal("100.00"), today.minusDays(11), categories.get(3)),
            new Expense("Dinner at restaurant", new BigDecimal("45.00"), today.minusDays(12), categories.get(0)),
            new Expense("Bus pass", new BigDecimal("20.00"), today.minusDays(13), categories.get(1)),
            new Expense("Video game", new BigDecimal("60.00"), today.minusDays(14), categories.get(2)),
            new Expense("Water bill", new BigDecimal("40.00"), today.minusDays(15), categories.get(3)),
            new Expense("Gym membership", new BigDecimal("55.00"), today.minusDays(16), categories.get(4)),
            new Expense("Birthday gift", new BigDecimal("70.00"), today.minusDays(17), categories.get(5)),
            new Expense("Textbooks", new BigDecimal("150.00"), today.minusDays(18), categories.get(6)),
            new Expense("Snack", new BigDecimal("5.00"), today.minusDays(19), categories.get(0)),
            new Expense("Coffee", new BigDecimal("3.50"), today.minusDays(20), categories.get(0))  ,
            new Expense("Train ticket", new BigDecimal("25.00"), today.minusDays(21), categories.get(1)),  
            new Expense("Streaming subscription", new BigDecimal("12.00"), today.minusDays(22), categories.get(2)),
            new Expense("Doctor visit", new BigDecimal("100.00"), today.minusDays(23), categories.get(4)),
            new Expense("Clothes", new BigDecimal("80.00"), today.minusDays(24), categories.get(5)),
            new Expense("Course materials", new BigDecimal("40.00"), today.minusDays(25), categories.get(6)),
            new Expense("Lunch special", new BigDecimal("20.00"), today.minusDays(26), categories.get(0)),
            new Expense("Gasoline", new BigDecimal("45.00"), today.minusDays(27), categories.get(1)),
            new Expense("Theater play", new BigDecimal("50.00"), today.minusDays(28), categories.get(2)),
            new Expense("Mobile bill", new BigDecimal("60.00"), today.minusDays(29), categories.get(3)),
            new Expense("Vitamins", new BigDecimal("25.00"), today.minusDays(30), categories.get(4)),
            new Expense("Online shopping", new BigDecimal("90.00"), today.minusDays(31), categories.get(5)),
            new Expense("Workshop fee", new BigDecimal("75.00"), today.minusDays(32), categories.get(6)),
            new Expense("Dessert", new BigDecimal("8.00"), today.minusDays(33), categories.get(0)),
            new Expense("Airport taxi", new BigDecimal("70.00"), today.minusDays(34), categories.get(1)),
            new Expense("Museum visit", new BigDecimal("18.00"), today.minusDays(35), categories.get(2)),
            new Expense("Cable TV", new BigDecimal("55.00"), today.minusDays(36), categories.get(3)),
            new Expense("Dental checkup", new BigDecimal("120.00"), today.minusDays(37), categories.get(4)),
            new Expense("New shoes", new BigDecimal("110.00"), today.minusDays(38), categories.get(5)),
            new Expense("Language course", new BigDecimal("200.00"), today.minusDays(39), categories.get(6)),
            new Expense("Breakfast", new BigDecimal("15.00"), today.minusDays(40), categories.get(0)) 
        );
    }
}
