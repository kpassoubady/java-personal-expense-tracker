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
            new Expense("Online course fee", new BigDecimal("45.00"), today.minusDays(7), categories.get(6))
        );
    }
}
