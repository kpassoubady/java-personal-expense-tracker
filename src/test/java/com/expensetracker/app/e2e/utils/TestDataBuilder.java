package com.expensetracker.app.e2e.utils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Builder pattern utility for creating consistent test data
 * Provides fluent API for building test objects
 */
public class TestDataBuilder {
    
    private static final Random random = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Builder for Expense test data
     */
    public static class ExpenseBuilder {
        private String description;
        private BigDecimal amount;
        private String expenseDate;
        private Long categoryId;
        private String categoryName;
        
        public ExpenseBuilder() {
            // Set default values
            this.description = "Test Expense " + System.currentTimeMillis();
            this.amount = new BigDecimal("50.00");
            this.expenseDate = LocalDate.now().format(DATE_FORMATTER);
            this.categoryId = 1L;
        }
        
        public ExpenseBuilder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public ExpenseBuilder withAmount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public ExpenseBuilder withAmount(String amount) {
            this.amount = new BigDecimal(amount);
            return this;
        }
        
        public ExpenseBuilder withAmount(double amount) {
            this.amount = BigDecimal.valueOf(amount);
            return this;
        }
        
        public ExpenseBuilder withExpenseDate(String expenseDate) {
            this.expenseDate = expenseDate;
            return this;
        }
        
        public ExpenseBuilder withExpenseDate(LocalDate expenseDate) {
            this.expenseDate = expenseDate.format(DATE_FORMATTER);
            return this;
        }
        
        public ExpenseBuilder withCategoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }
        
        public ExpenseBuilder withCategoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }
        
        public ExpenseBuilder withRandomAmount() {
            double randomAmount = 10.00 + (random.nextDouble() * 500.00);
            this.amount = BigDecimal.valueOf(Math.round(randomAmount * 100.0) / 100.0);
            return this;
        }
        
        public ExpenseBuilder withRandomDate() {
            LocalDate startDate = LocalDate.now().minusMonths(3);
            LocalDate endDate = LocalDate.now();
            long daysBetween = endDate.toEpochDay() - startDate.toEpochDay();
            long randomDays = random.nextLong() % daysBetween;
            LocalDate randomDate = startDate.plusDays(Math.abs(randomDays));
            this.expenseDate = randomDate.format(DATE_FORMATTER);
            return this;
        }
        
        public Map<String, Object> build() {
            Map<String, Object> expense = new HashMap<>();
            expense.put("description", description);
            expense.put("amount", amount);
            expense.put("expenseDate", expenseDate);
            if (categoryId != null) {
                expense.put("categoryId", categoryId);
            }
            if (categoryName != null) {
                expense.put("categoryName", categoryName);
            }
            return expense;
        }
        
        public Map<String, String> buildForForm() {
            Map<String, String> formData = new HashMap<>();
            formData.put("description", description);
            formData.put("amount", amount.toString());
            formData.put("expenseDate", expenseDate);
            if (categoryId != null) {
                formData.put("category.id", categoryId.toString());
            }
            return formData;
        }
    }
    
    /**
     * Builder for Category test data
     */
    public static class CategoryBuilder {
        private String name;
        private String description;
        private String color;
        private String icon;
        
        public CategoryBuilder() {
            // Set default values
            this.name = "Test Category " + System.currentTimeMillis();
            this.description = "Test category description";
            this.color = "#" + String.format("%06X", random.nextInt(0xFFFFFF));
            this.icon = getRandomIcon();
        }
        
        public CategoryBuilder withName(String name) {
            this.name = name;
            return this;
        }
        
        public CategoryBuilder withDescription(String description) {
            this.description = description;
            return this;
        }
        
        public CategoryBuilder withColor(String color) {
            this.color = color;
            return this;
        }
        
        public CategoryBuilder withIcon(String icon) {
            this.icon = icon;
            return this;
        }
        
        public CategoryBuilder withRandomColor() {
            this.color = "#" + String.format("%06X", random.nextInt(0xFFFFFF));
            return this;
        }
        
        public CategoryBuilder withRandomIcon() {
            this.icon = getRandomIcon();
            return this;
        }
        
        private String getRandomIcon() {
            String[] icons = {"🍔", "🚗", "🎬", "🏠", "💰", "🎯", "📚", "⚽", "🎵", "💻"};
            return icons[random.nextInt(icons.length)];
        }
        
        public Map<String, Object> build() {
            Map<String, Object> category = new HashMap<>();
            category.put("name", name);
            category.put("description", description);
            category.put("color", color);
            category.put("icon", icon);
            return category;
        }
        
        public Map<String, String> buildForForm() {
            Map<String, String> formData = new HashMap<>();
            formData.put("name", name);
            formData.put("description", description);
            formData.put("color", color);
            formData.put("icon", icon);
            return formData;
        }
    }
    
    /**
     * Predefined test data sets
     */
    public static class PredefinedData {
        
        public static ExpenseBuilder groceryExpense() {
            return new ExpenseBuilder()
                    .withDescription("Grocery shopping")
                    .withAmount("85.50")
                    .withCategoryName("Food");
        }
        
        public static ExpenseBuilder transportExpense() {
            return new ExpenseBuilder()
                    .withDescription("Gas station")
                    .withAmount("65.00")
                    .withCategoryName("Transport");
        }
        
        public static ExpenseBuilder entertainmentExpense() {
            return new ExpenseBuilder()
                    .withDescription("Movie tickets")
                    .withAmount("24.99")
                    .withCategoryName("Entertainment");
        }
        
        public static ExpenseBuilder largeExpense() {
            return new ExpenseBuilder()
                    .withDescription("Large purchase")
                    .withAmount("999.99")
                    .withCategoryName("Shopping");
        }
        
        public static ExpenseBuilder smallExpense() {
            return new ExpenseBuilder()
                    .withDescription("Coffee")
                    .withAmount("4.50")
                    .withCategoryName("Food");
        }
        
        public static CategoryBuilder foodCategory() {
            return new CategoryBuilder()
                    .withName("Food")
                    .withDescription("Food and beverages")
                    .withColor("#FF5722")
                    .withIcon("🍔");
        }
        
        public static CategoryBuilder transportCategory() {
            return new CategoryBuilder()
                    .withName("Transport")
                    .withDescription("Transportation expenses")
                    .withColor("#2196F3")
                    .withIcon("🚗");
        }
        
        public static CategoryBuilder entertainmentCategory() {
            return new CategoryBuilder()
                    .withName("Entertainment")
                    .withDescription("Entertainment and leisure")
                    .withColor("#9C27B0")
                    .withIcon("🎬");
        }
    }
    
    /**
     * Validation test data for negative testing
     */
    public static class InvalidData {
        
        public static ExpenseBuilder invalidExpense() {
            return new ExpenseBuilder()
                    .withDescription("")  // Invalid: empty description
                    .withAmount(-10.00);  // Invalid: negative amount
        }
        
        public static ExpenseBuilder expenseWithoutCategory() {
            return new ExpenseBuilder()
                    .withDescription("Test expense")
                    .withAmount("50.00")
                    .withCategoryId(null);
        }
        
        public static ExpenseBuilder expenseWithLargeAmount() {
            return new ExpenseBuilder()
                    .withDescription("Expensive item")
                    .withAmount("15000.00");  // Exceeds $10,000 limit
        }
        
        public static CategoryBuilder invalidCategory() {
            return new CategoryBuilder()
                    .withName("")  // Invalid: empty name
                    .withColor("invalid-color");  // Invalid: not hex color
        }
        
        public static CategoryBuilder duplicateCategory() {
            return new CategoryBuilder()
                    .withName("Food");  // Duplicate of existing category
        }
    }
    
    /**
     * Factory methods for quick data creation
     */
    public static ExpenseBuilder expense() {
        return new ExpenseBuilder();
    }
    
    public static CategoryBuilder category() {
        return new CategoryBuilder();
    }
    
    /**
     * Generate multiple expenses for bulk testing
     */
    public static ExpenseBuilder[] multipleExpenses(int count) {
        ExpenseBuilder[] expenses = new ExpenseBuilder[count];
        for (int i = 0; i < count; i++) {
            expenses[i] = new ExpenseBuilder()
                    .withDescription("Test Expense " + (i + 1))
                    .withRandomAmount()
                    .withRandomDate()
                    .withCategoryId((long) (1 + random.nextInt(3)));
        }
        return expenses;
    }
    
    /**
     * Generate multiple categories for bulk testing
     */
    public static CategoryBuilder[] multipleCategories(int count) {
        CategoryBuilder[] categories = new CategoryBuilder[count];
        String[] baseNames = {"Shopping", "Healthcare", "Education", "Utilities", "Travel", "Sports"};
        
        for (int i = 0; i < count; i++) {
            String name = i < baseNames.length ? baseNames[i] : "Category " + (i + 1);
            categories[i] = new CategoryBuilder()
                    .withName(name)
                    .withRandomColor()
                    .withRandomIcon();
        }
        return categories;
    }
}