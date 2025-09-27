package com.expensetracker.app;

import com.expensetracker.app.entity.Category;

public class CategoryBuilder {
    private Long id = 1L;
    private String name = "Food";

    public CategoryBuilder withId(Long id) { this.id = id; return this; }
    public CategoryBuilder withName(String name) { this.name = name; return this; }

    public Category build() {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }
}

