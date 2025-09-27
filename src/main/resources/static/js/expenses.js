/**
 * Expenses JavaScript Module
 * Features:
 * - AJAX form submission for add/edit expense forms
 * - Dynamic category filtering without page reload
 * - Real-time search functionality with debouncing
 * - Delete confirmation modals
 * - Success/error toast notifications
 * - Form validation with real-time feedback
 * - Auto-save draft functionality for forms
 */

class ExpenseManager {
    constructor() {
        this.searchTimeout = null;
        this.autoSaveTimeout = null;
        this.autoSaveInterval = 5000; // 5 seconds
        this.searchDebounceTime = 500; // 500ms debounce
        this.currentFormData = {};
        this.validationRules = {};
        this.toastContainer = null;
        
        this.init();
    }

    /**
     * Initialize the expense manager
     */
    init() {
        console.log('Initializing Expense Manager...');
        
        // Wait for DOM to be ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setup());
        } else {
            this.setup();
        }
    }

    /**
     * Setup all expense management functionality
     */
    setup() {
        try {
            // Initialize components
            this.createToastContainer();
            this.setupFormSubmission();
            this.setupCategoryFiltering();
            this.setupRealTimeSearch();
            this.setupDeleteConfirmation();
            this.setupFormValidation();
            this.setupAutoSave();
            this.setupEventListeners();
            this.loadDraftData();
            
            console.log('Expense Manager initialized successfully');
            this.showToast('Expense Manager loaded', 'success');
        } catch (error) {
            console.error('Failed to setup Expense Manager:', error);
        }
    }

    /**
     * Create toast notification container
     */
    createToastContainer() {
        if (!document.getElementById('toastContainer')) {
            const container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-container position-fixed top-0 end-0 p-3';
            container.style.zIndex = '9999';
            document.body.appendChild(container);
        }
        this.toastContainer = document.getElementById('toastContainer');
    }

    /**
     * Setup AJAX form submission
     */
    setupFormSubmission() {
        const expenseForms = document.querySelectorAll('form[id*="expense"], form[action*="/expenses"]');
        
        expenseForms.forEach(form => {
            form.addEventListener('submit', (e) => this.handleFormSubmission(e));
        });

        console.log(`Set up AJAX form submission for ${expenseForms.length} forms`);
    }

    /**
     * Handle form submission via AJAX
     */
    async handleFormSubmission(event) {
        event.preventDefault();
        
        const form = event.target;
        const submitButton = form.querySelector('button[type="submit"]');
        const originalButtonText = submitButton?.textContent;
        
        try {
            // Show loading state
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Saving...';
            }
            
            // Validate form
            if (!this.validateForm(form)) {
                this.showToast('Please fix validation errors', 'error');
                return;
            }

            // Prepare form data
            const formData = new FormData(form);
            const method = form.method || 'POST';
            const url = form.action;

            // Submit form
            const response = await fetch(url, {
                method: method,
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (response.ok) {
                // Handle success
                const result = await this.handleSuccessResponse(response, form);
                this.clearDraft(form);
                
                // Redirect if specified or stay on page
                if (result.redirect) {
                    window.location.href = result.redirect;
                } else if (response.redirected) {
                    window.location.href = response.url;
                } else {
                    this.showToast('Expense saved successfully!', 'success');
                    this.resetForm(form);
                    this.refreshExpenseList();
                }
            } else {
                // Handle error response
                await this.handleErrorResponse(response, form);
            }

        } catch (error) {
            console.error('Form submission error:', error);
            this.showToast('Network error. Please try again.', 'error');
        } finally {
            // Restore button state
            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = originalButtonText;
            }
        }
    }

    /**
     * Handle successful form response
     */
    async handleSuccessResponse(response, form) {
        try {
            const contentType = response.headers.get('content-type');
            
            if (contentType && contentType.includes('application/json')) {
                const result = await response.json();
                return result;
            } else {
                // HTML response - likely a redirect
                return { redirect: response.url };
            }
        } catch (error) {
            console.log('Response was not JSON, likely a redirect');
            return { redirect: response.url };
        }
    }

    /**
     * Handle error form response
     */
    async handleErrorResponse(response, form) {
        try {
            const contentType = response.headers.get('content-type');
            
            if (contentType && contentType.includes('application/json')) {
                const errorData = await response.json();
                this.displayValidationErrors(errorData.errors || {}, form);
                this.showToast(errorData.message || 'Validation failed', 'error');
            } else {
                // HTML error page
                this.showToast(`Server error (${response.status}). Please try again.`, 'error');
            }
        } catch (error) {
            this.showToast('Failed to save expense. Please try again.', 'error');
        }
    }

    /**
     * Setup dynamic category filtering
     */
    setupCategoryFiltering() {
        const categoryFilters = document.querySelectorAll('[data-category-filter]');
        const categorySelects = document.querySelectorAll('select[name="categoryId"], #categoryFilter');
        
        // Category filter buttons
        categoryFilters.forEach(filter => {
            filter.addEventListener('click', (e) => {
                e.preventDefault();
                const categoryId = filter.getAttribute('data-category-filter');
                this.filterByCategory(categoryId);
            });
        });

        // Category dropdown filters
        categorySelects.forEach(select => {
            select.addEventListener('change', (e) => {
                if (e.target.id === 'categoryFilter') {
                    const categoryId = e.target.value;
                    this.filterByCategory(categoryId);
                }
            });
        });

        console.log(`Set up category filtering for ${categoryFilters.length + categorySelects.length} elements`);
    }

    /**
     * Filter expenses by category without page reload
     */
    async filterByCategory(categoryId) {
        try {
            this.showLoadingOverlay();
            
            const currentUrl = new URL(window.location);
            
            if (categoryId && categoryId !== 'all') {
                currentUrl.searchParams.set('categoryId', categoryId);
            } else {
                currentUrl.searchParams.delete('categoryId');
            }
            
            // Preserve other search parameters
            const searchParams = currentUrl.searchParams;
            
            // Fetch filtered results
            const response = await fetch(`/expenses?${searchParams.toString()}`, {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                }
            });

            if (response.ok) {
                const html = await response.text();
                this.updateExpenseList(html);
                
                // Update URL without page reload
                window.history.pushState(null, '', currentUrl.toString());
                
                this.showToast('Expenses filtered successfully', 'info');
            } else {
                this.showToast('Failed to filter expenses', 'error');
            }

        } catch (error) {
            console.error('Category filtering error:', error);
            this.showToast('Failed to filter expenses', 'error');
        } finally {
            this.hideLoadingOverlay();
        }
    }

    /**
     * Setup real-time search with debouncing
     */
    setupRealTimeSearch() {
        const searchInputs = document.querySelectorAll('input[name="search"], #searchInput, #expenseSearch');
        
        searchInputs.forEach(input => {
            input.addEventListener('input', (e) => {
                this.debouncedSearch(e.target.value);
            });
            
            // Clear search functionality
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Escape') {
                    input.value = '';
                    this.debouncedSearch('');
                }
            });
        });

        // Quick search buttons
        const quickSearchButtons = document.querySelectorAll('[data-quick-search]');
        quickSearchButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                const searchTerm = button.getAttribute('data-quick-search');
                const searchInput = document.querySelector('input[name="search"]');
                if (searchInput) {
                    searchInput.value = searchTerm;
                    this.performSearch(searchTerm);
                }
            });
        });

        console.log(`Set up real-time search for ${searchInputs.length} inputs`);
    }

    /**
     * Debounced search function
     */
    debouncedSearch(searchTerm) {
        clearTimeout(this.searchTimeout);
        
        this.searchTimeout = setTimeout(() => {
            this.performSearch(searchTerm);
        }, this.searchDebounceTime);
    }

    /**
     * Perform search without page reload
     */
    async performSearch(searchTerm) {
        try {
            this.showSearchLoading(true);
            
            const currentUrl = new URL(window.location);
            
            if (searchTerm && searchTerm.trim()) {
                currentUrl.searchParams.set('search', searchTerm.trim());
            } else {
                currentUrl.searchParams.delete('search');
            }
            
            // Fetch search results
            const response = await fetch(`/expenses?${currentUrl.searchParams.toString()}`, {
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8'
                }
            });

            if (response.ok) {
                const html = await response.text();
                this.updateExpenseList(html);
                
                // Update URL without page reload
                window.history.pushState(null, '', currentUrl.toString());
                
                const resultCount = this.getResultCount(html);
                this.showToast(`Found ${resultCount} expenses`, 'info');
            } else {
                this.showToast('Search failed', 'error');
            }

        } catch (error) {
            console.error('Search error:', error);
            this.showToast('Search failed', 'error');
        } finally {
            this.showSearchLoading(false);
        }
    }

    /**
     * Setup delete confirmation modals
     */
    setupDeleteConfirmation() {
        // Delete buttons
        document.addEventListener('click', (e) => {
            const deleteButton = e.target.closest('[data-delete-expense]');
            if (deleteButton) {
                e.preventDefault();
                const expenseId = deleteButton.getAttribute('data-delete-expense');
                const expenseName = deleteButton.getAttribute('data-expense-name') || 'this expense';
                const expenseAmount = deleteButton.getAttribute('data-expense-amount') || '0.00';
                
                this.showDeleteConfirmation(expenseId, expenseName, expenseAmount);
            }
        });

        console.log('Set up delete confirmation handlers');
    }

    /**
     * Show delete confirmation modal
     */
    showDeleteConfirmation(expenseId, expenseName, expenseAmount) {
        // Check if modal exists, if not create it
        let modal = document.getElementById('deleteExpenseModal');
        if (!modal) {
            modal = this.createDeleteModal();
        }

        // Update modal content
        const modalBody = modal.querySelector('.modal-body');
        const deleteForm = modal.querySelector('#deleteExpenseForm');
        
        modalBody.querySelector('#deleteExpenseName').textContent = expenseName;
        modalBody.querySelector('#deleteExpenseAmount').textContent = expenseAmount;
        deleteForm.action = `/expenses/${expenseId}`;

        // Show modal
        const bsModal = new bootstrap.Modal(modal);
        bsModal.show();

        // Handle form submission
        deleteForm.onsubmit = (e) => this.handleDeleteSubmission(e, bsModal);
    }

    /**
     * Create delete confirmation modal
     */
    createDeleteModal() {
        const modalHtml = `
            <div class="modal fade" id="deleteExpenseModal" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header border-0 bg-danger text-white">
                            <h5 class="modal-title">
                                <i class="fas fa-exclamation-triangle me-2"></i>
                                Delete Expense
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="alert alert-danger border-0">
                                <div class="d-flex">
                                    <div class="flex-shrink-0">
                                        <i class="fas fa-warning fa-lg"></i>
                                    </div>
                                    <div class="flex-grow-1 ms-3">
                                        <h6 class="alert-heading">This action cannot be undone!</h6>
                                        <p class="mb-0">Are you sure you want to delete this expense?</p>
                                    </div>
                                </div>
                            </div>
                            <div class="expense-info p-3 bg-light rounded">
                                <div class="row">
                                    <div class="col-sm-4 text-muted">Description:</div>
                                    <div class="col-sm-8 fw-medium" id="deleteExpenseName">-</div>
                                </div>
                                <div class="row mt-2">
                                    <div class="col-sm-4 text-muted">Amount:</div>
                                    <div class="col-sm-8 fw-bold text-danger">$<span id="deleteExpenseAmount">0.00</span></div>
                                </div>
                            </div>
                        </div>
                        <div class="modal-footer border-0">
                            <button type="button" class="btn btn-outline-secondary" data-bs-dismiss="modal">
                                <i class="fas fa-times me-1"></i>Cancel
                            </button>
                            <form id="deleteExpenseForm" method="post" style="display: inline;">
                                <input type="hidden" name="_method" value="DELETE">
                                <button type="submit" class="btn btn-danger">
                                    <i class="fas fa-trash me-1"></i>Delete Expense
                                </button>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.body.insertAdjacentHTML('beforeend', modalHtml);
        return document.getElementById('deleteExpenseModal');
    }

    /**
     * Handle delete form submission
     */
    async handleDeleteSubmission(event, modalInstance) {
        event.preventDefault();
        
        const form = event.target;
        const submitButton = form.querySelector('button[type="submit"]');
        const originalText = submitButton.textContent;
        
        try {
            // Show loading state
            submitButton.disabled = true;
            submitButton.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>Deleting...';
            
            // Submit delete request
            const response = await fetch(form.action, {
                method: 'DELETE',
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });

            if (response.ok) {
                modalInstance.hide();
                this.showToast('Expense deleted successfully!', 'success');
                this.refreshExpenseList();
            } else {
                this.showToast('Failed to delete expense', 'error');
            }

        } catch (error) {
            console.error('Delete error:', error);
            this.showToast('Failed to delete expense', 'error');
        } finally {
            submitButton.disabled = false;
            submitButton.textContent = originalText;
        }
    }

    /**
     * Setup form validation with real-time feedback
     */
    setupFormValidation() {
        const forms = document.querySelectorAll('form[id*="expense"], form[action*="/expenses"]');
        
        forms.forEach(form => {
            this.initializeFormValidation(form);
        });

        console.log(`Set up form validation for ${forms.length} forms`);
    }

    /**
     * Initialize validation for a specific form
     */
    initializeFormValidation(form) {
        // Define validation rules
        this.validationRules[form.id || 'expenseForm'] = {
            description: {
                required: true,
                minLength: 3,
                maxLength: 500
            },
            amount: {
                required: true,
                min: 0.01,
                max: 999999.99
            },
            categoryId: {
                required: true
            },
            expenseDate: {
                required: true,
                maxDate: new Date()
            }
        };

        // Add real-time validation listeners
        const inputs = form.querySelectorAll('input, select, textarea');
        inputs.forEach(input => {
            input.addEventListener('blur', () => this.validateField(input));
            input.addEventListener('input', () => {
                if (input.classList.contains('is-invalid')) {
                    this.validateField(input);
                }
                this.updateCharacterCount(input);
                this.saveFormDraft(form);
            });
        });

        // Setup character counters
        this.setupCharacterCounters(form);
    }

    /**
     * Validate a single form field
     */
    validateField(field) {
        const formId = field.form.id || 'expenseForm';
        const rules = this.validationRules[formId];
        const fieldName = field.name;
        const rule = rules[fieldName];
        
        if (!rule) return true;

        const value = field.value.trim();
        let isValid = true;
        let errorMessage = '';

        // Required validation
        if (rule.required && !value) {
            isValid = false;
            errorMessage = `${this.getFieldDisplayName(fieldName)} is required`;
        }
        // Min/Max length validation
        else if (rule.minLength && value.length < rule.minLength) {
            isValid = false;
            errorMessage = `${this.getFieldDisplayName(fieldName)} must be at least ${rule.minLength} characters`;
        }
        else if (rule.maxLength && value.length > rule.maxLength) {
            isValid = false;
            errorMessage = `${this.getFieldDisplayName(fieldName)} must not exceed ${rule.maxLength} characters`;
        }
        // Number validation
        else if (rule.min !== undefined && parseFloat(value) < rule.min) {
            isValid = false;
            errorMessage = `${this.getFieldDisplayName(fieldName)} must be at least ${rule.min}`;
        }
        else if (rule.max !== undefined && parseFloat(value) > rule.max) {
            isValid = false;
            errorMessage = `${this.getFieldDisplayName(fieldName)} must not exceed ${rule.max}`;
        }
        // Date validation
        else if (rule.maxDate && new Date(value) > rule.maxDate) {
            isValid = false;
            errorMessage = `${this.getFieldDisplayName(fieldName)} cannot be in the future`;
        }

        // Update field styling
        this.updateFieldValidation(field, isValid, errorMessage);
        
        return isValid;
    }

    /**
     * Update field validation styling
     */
    updateFieldValidation(field, isValid, errorMessage) {
        field.classList.remove('is-valid', 'is-invalid');
        field.classList.add(isValid ? 'is-valid' : 'is-invalid');

        // Update or create feedback element
        let feedback = field.parentNode.querySelector('.invalid-feedback, .valid-feedback');
        if (!feedback) {
            feedback = document.createElement('div');
            field.parentNode.appendChild(feedback);
        }

        feedback.className = isValid ? 'valid-feedback' : 'invalid-feedback';
        feedback.textContent = isValid ? 'Looks good!' : errorMessage;
    }

    /**
     * Validate entire form
     */
    validateForm(form) {
        const inputs = form.querySelectorAll('input[required], select[required], textarea[required]');
        let isFormValid = true;

        inputs.forEach(input => {
            const fieldValid = this.validateField(input);
            if (!fieldValid) {
                isFormValid = false;
            }
        });

        return isFormValid;
    }

    /**
     * Setup auto-save draft functionality
     */
    setupAutoSave() {
        const forms = document.querySelectorAll('form[id*="expense"], form[action*="/expenses"]');
        
        forms.forEach(form => {
            // Only enable auto-save for new expense forms
            if (form.action.includes('/new') || form.action.includes('/expenses') && !form.action.match(/\/\d+$/)) {
                setInterval(() => {
                    this.saveFormDraft(form);
                }, this.autoSaveInterval);
                
                console.log(`Auto-save enabled for form: ${form.id || 'expense form'}`);
            }
        });
    }

    /**
     * Save form data as draft
     */
    saveFormDraft(form) {
        try {
            const formData = new FormData(form);
            const draftData = {};
            
            for (let [key, value] of formData.entries()) {
                if (value.trim()) {
                    draftData[key] = value;
                }
            }
            
            // Only save if there's meaningful data
            if (Object.keys(draftData).length > 0) {
                const draftKey = `expenseDraft_${form.id || 'default'}`;
                localStorage.setItem(draftKey, JSON.stringify({
                    data: draftData,
                    timestamp: Date.now()
                }));
                
                this.showAutoSaveIndicator();
            }
        } catch (error) {
            console.warn('Auto-save failed:', error);
        }
    }

    /**
     * Load draft data into form
     */
    loadDraftData() {
        const forms = document.querySelectorAll('form[id*="expense"], form[action*="/expenses"]');
        
        forms.forEach(form => {
            if (form.action.includes('/new') || form.action.includes('/expenses') && !form.action.match(/\/\d+$/)) {
                const draftKey = `expenseDraft_${form.id || 'default'}`;
                const savedDraft = localStorage.getItem(draftKey);
                
                if (savedDraft) {
                    try {
                        const draft = JSON.parse(savedDraft);
                        const ageInMinutes = (Date.now() - draft.timestamp) / (1000 * 60);
                        
                        // Only load drafts less than 1 hour old
                        if (ageInMinutes < 60) {
                            this.populateForm(form, draft.data);
                            this.showToast('Draft data restored', 'info');
                        } else {
                            localStorage.removeItem(draftKey);
                        }
                    } catch (error) {
                        console.warn('Failed to load draft:', error);
                        localStorage.removeItem(draftKey);
                    }
                }
            }
        });
    }

    /**
     * Clear form draft
     */
    clearDraft(form) {
        const draftKey = `expenseDraft_${form.id || 'default'}`;
        localStorage.removeItem(draftKey);
    }

    /**
     * Setup additional event listeners
     */
    setupEventListeners() {
        // Amount formatting
        document.addEventListener('input', (e) => {
            if (e.target.name === 'amount' || e.target.classList.contains('amount-input')) {
                this.formatAmountInput(e.target);
            }
        });

        // Category preview
        document.addEventListener('change', (e) => {
            if (e.target.name === 'categoryId') {
                this.updateCategoryPreview(e.target);
            }
        });

        // Quick date buttons
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('quick-date-btn')) {
                e.preventDefault();
                const dateField = document.querySelector('input[name="expenseDate"]');
                if (dateField) {
                    dateField.value = e.target.getAttribute('data-date');
                }
            }
        });

        console.log('Additional event listeners set up');
    }

    // Utility Methods

    /**
     * Show toast notification
     */
    showToast(message, type = 'info') {
        const toastId = 'toast-' + Date.now();
        const iconMap = {
            success: 'fas fa-check-circle',
            error: 'fas fa-exclamation-circle',
            warning: 'fas fa-exclamation-triangle',
            info: 'fas fa-info-circle'
        };
        
        const toastHtml = `
            <div id="${toastId}" class="toast align-items-center text-bg-${type} border-0" role="alert">
                <div class="d-flex">
                    <div class="toast-body">
                        <i class="${iconMap[type]} me-2"></i>
                        ${message}
                    </div>
                    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
                </div>
            </div>
        `;
        
        this.toastContainer.insertAdjacentHTML('beforeend', toastHtml);
        
        const toastElement = document.getElementById(toastId);
        const toast = new bootstrap.Toast(toastElement, { delay: 4000 });
        
        toast.show();
        
        // Remove from DOM after hiding
        toastElement.addEventListener('hidden.bs.toast', () => {
            toastElement.remove();
        });
    }

    /**
     * Show/hide loading overlay
     */
    showLoadingOverlay() {
        if (!document.getElementById('loadingOverlay')) {
            const overlay = document.createElement('div');
            overlay.id = 'loadingOverlay';
            overlay.className = 'position-fixed top-0 start-0 w-100 h-100 d-flex align-items-center justify-content-center';
            overlay.style.backgroundColor = 'rgba(0,0,0,0.5)';
            overlay.style.zIndex = '9998';
            overlay.innerHTML = `
                <div class="bg-white p-4 rounded shadow">
                    <div class="text-center">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">Loading...</span>
                        </div>
                        <div class="mt-2">Loading...</div>
                    </div>
                </div>
            `;
            document.body.appendChild(overlay);
        }
    }

    hideLoadingOverlay() {
        const overlay = document.getElementById('loadingOverlay');
        if (overlay) {
            overlay.remove();
        }
    }

    /**
     * Show/hide search loading indicator
     */
    showSearchLoading(show) {
        const searchInputs = document.querySelectorAll('input[name="search"]');
        searchInputs.forEach(input => {
            const container = input.closest('.input-group') || input.parentNode;
            let indicator = container.querySelector('.search-loading');
            
            if (show && !indicator) {
                indicator = document.createElement('div');
                indicator.className = 'search-loading position-absolute end-0 top-50 translate-middle-y me-2';
                indicator.innerHTML = '<div class="spinner-border spinner-border-sm text-primary" role="status"></div>';
                indicator.style.pointerEvents = 'none';
                container.style.position = 'relative';
                container.appendChild(indicator);
            } else if (!show && indicator) {
                indicator.remove();
            }
        });
    }

    /**
     * Update expense list without page reload
     */
    updateExpenseList(html) {
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        
        // Update main content area
        const newContent = doc.querySelector('.expense-list, .table-responsive, .expense-table');
        const currentContent = document.querySelector('.expense-list, .table-responsive, .expense-table');
        
        if (newContent && currentContent) {
            currentContent.innerHTML = newContent.innerHTML;
        }
        
        // Re-setup event listeners for new content
        this.setupDeleteConfirmation();
    }

    /**
     * Refresh expense list
     */
    async refreshExpenseList() {
        try {
            const response = await fetch(window.location.href, {
                headers: { 'X-Requested-With': 'XMLHttpRequest' }
            });
            
            if (response.ok) {
                const html = await response.text();
                this.updateExpenseList(html);
            }
        } catch (error) {
            console.error('Failed to refresh expense list:', error);
        }
    }

    /**
     * Helper methods
     */
    getFieldDisplayName(fieldName) {
        const displayNames = {
            description: 'Description',
            amount: 'Amount',
            categoryId: 'Category',
            expenseDate: 'Expense Date'
        };
        return displayNames[fieldName] || fieldName;
    }

    getResultCount(html) {
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const rows = doc.querySelectorAll('.expense-row, tbody tr');
        return rows.length;
    }

    resetForm(form) {
        form.reset();
        form.querySelectorAll('.is-valid, .is-invalid').forEach(el => {
            el.classList.remove('is-valid', 'is-invalid');
        });
    }

    populateForm(form, data) {
        Object.keys(data).forEach(key => {
            const field = form.querySelector(`[name="${key}"]`);
            if (field) {
                field.value = data[key];
            }
        });
    }

    setupCharacterCounters(form) {
        const textareas = form.querySelectorAll('textarea[maxlength]');
        textareas.forEach(textarea => {
            const counter = document.createElement('small');
            counter.className = 'form-text text-muted character-counter';
            textarea.parentNode.appendChild(counter);
            this.updateCharacterCount(textarea);
        });
    }

    updateCharacterCount(input) {
        if (input.maxLength && input.maxLength > 0) {
            const counter = input.parentNode.querySelector('.character-counter');
            if (counter) {
                const remaining = input.maxLength - input.value.length;
                counter.textContent = `${input.value.length}/${input.maxLength} characters`;
                counter.className = `form-text character-counter ${remaining < 50 ? 'text-warning' : remaining < 10 ? 'text-danger' : 'text-muted'}`;
            }
        }
    }

    formatAmountInput(input) {
        const value = parseFloat(input.value);
        if (!isNaN(value)) {
            const formatted = new Intl.NumberFormat('en-US', {
                style: 'currency',
                currency: 'USD'
            }).format(value);
            
            // Update display somewhere if needed
            const display = document.getElementById(input.id + 'Formatted');
            if (display) {
                display.textContent = formatted;
            }
        }
    }

    updateCategoryPreview(select) {
        const selectedOption = select.options[select.selectedIndex];
        const preview = document.getElementById('categoryPreview');
        
        if (preview && selectedOption.value) {
            const color = selectedOption.getAttribute('data-color') || '#6c757d';
            const icon = selectedOption.getAttribute('data-icon') || 'fas fa-tag';
            
            preview.innerHTML = `
                <span class="category-badge-preview" style="background-color: ${color}">
                    <i class="${icon} me-1"></i>
                    ${selectedOption.textContent}
                </span>
            `;
            preview.style.display = 'block';
        } else if (preview) {
            preview.style.display = 'none';
        }
    }

    showAutoSaveIndicator() {
        let indicator = document.getElementById('autoSaveIndicator');
        if (!indicator) {
            indicator = document.createElement('div');
            indicator.id = 'autoSaveIndicator';
            indicator.className = 'position-fixed bottom-0 end-0 p-3';
            indicator.style.zIndex = '9997';
            document.body.appendChild(indicator);
        }
        
        indicator.innerHTML = `
            <div class="alert alert-success alert-sm mb-0 py-2 px-3">
                <i class="fas fa-cloud-upload-alt me-1"></i>
                Draft saved
            </div>
        `;
        
        setTimeout(() => {
            if (indicator) {
                indicator.innerHTML = '';
            }
        }, 2000);
    }

    displayValidationErrors(errors, form) {
        Object.keys(errors).forEach(fieldName => {
            const field = form.querySelector(`[name="${fieldName}"]`);
            if (field) {
                this.updateFieldValidation(field, false, errors[fieldName]);
            }
        });
    }
}

// Global instance
let expenseManager = null;

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on an expenses page
    if (window.location.pathname.includes('/expenses') || 
        document.querySelector('form[action*="/expenses"]')) {
        expenseManager = new ExpenseManager();
        
        // Make available globally for debugging
        window.expenseManager = expenseManager;
        
        console.log('Expenses module loaded and initialized');
    }
});

// Handle page unload
window.addEventListener('beforeunload', function() {
    // Save any pending drafts
    if (expenseManager) {
        const forms = document.querySelectorAll('form[action*="/expenses"]');
        forms.forEach(form => expenseManager.saveFormDraft(form));
    }
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ExpenseManager;
}