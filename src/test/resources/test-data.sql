-- Insert test data without specifying IDs, let them be auto-generated
INSERT INTO categories (name, description, color, icon, created_at, updated_at) VALUES ('Food', 'Food and dining', '#28a745', 'fas fa-utensils', NOW(), NOW());
INSERT INTO expenses (description, amount, expense_date, category_id, created_at, updated_at) VALUES ('Lunch', 10.0, CURRENT_DATE, 1, NOW(), NOW());

