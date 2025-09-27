package com.expensetracker.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main Spring Boot application class for the Personal Expense Tracker.
 * 
 * This application provides functionality to:
 * - Track personal expenses with categories
 * - Manage expense categories
 * - View expense summaries and reports
 * - Provide a web interface for expense management
 *
 * @author GitHub Copilot Training
 * @version 1.0.0
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.expensetracker.app.repository")
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }
}