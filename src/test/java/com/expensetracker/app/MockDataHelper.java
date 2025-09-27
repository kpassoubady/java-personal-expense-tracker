package com.expensetracker.app;

import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.entity.Category;
import java.util.List;

public class MockDataHelper {
    public static Expense createExpense() {
        return new ExpenseBuilder().build();
    }

    public static Category createCategory() {
        return new CategoryBuilder().build();
    }

    public static List<Expense> createExpenseList() {
        return List.of(
            createExpense(),
            new ExpenseBuilder().withId(2L).withDescription("Dinner").build()
        );
    }
}

