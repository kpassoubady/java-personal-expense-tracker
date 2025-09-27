package com.expensetracker.app;

import com.expensetracker.app.entity.Expense;
import com.expensetracker.app.entity.Category;

public class ExpenseBuilder {
    private Long id = 1L;
    private String description = "Lunch";
    private java.math.BigDecimal amount = new java.math.BigDecimal("10.0");
    private Category category = new CategoryBuilder().build();

    public ExpenseBuilder withId(Long id) { this.id = id; return this; }
    public ExpenseBuilder withDescription(String desc) { this.description = desc; return this; }
    public ExpenseBuilder withAmount(java.math.BigDecimal amt) { this.amount = amt; return this; }
    public ExpenseBuilder withCategory(Category cat) { this.category = cat; return this; }

    public Expense build() {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);
        return expense;
    }
}
