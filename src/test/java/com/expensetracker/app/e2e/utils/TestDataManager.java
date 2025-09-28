package com.expensetracker.app.e2e.utils;

public class TestDataManager {
    public static TestDataBuilder.ExpenseBuilder expense(String desc, String amt, String cat, String date) {
        return new TestDataBuilder.ExpenseBuilder()
            .withDescription(desc)
            .withAmount(amt)
            .withCategoryName(cat)
            .withExpenseDate(date);
    }
    // Add similar methods for categories, users, etc.
}
