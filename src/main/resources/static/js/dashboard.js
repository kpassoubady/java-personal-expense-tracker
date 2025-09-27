/**
 * Dashboard JavaScript Module
 * Features:
 * - AJAX expense data loading
 * - Pie chart for expenses by category (Chart.js)
 * - Bar chart for monthly expense trends
 * - Real-time updates every 30 seconds
 * - Responsive chart configurations
 * - Error handling for API failures
 * - Loading indicators
 */

class ExpenseDashboard {
    constructor() {
        this.pieChart = null;
        this.barChart = null;
        this.updateInterval = null;
        this.isLoading = false;
        this.chartColors = [
            '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', 
            '#9966FF', '#FF9F40', '#FF6384', '#C9CBCF',
            '#4BC0C0', '#FF6384', '#36A2EB', '#FFCE56'
        ];
        
        this.init();
    }

    /**
     * Initialize the dashboard
     */
    async init() {
        try {
            console.log('Initializing Expense Dashboard...');
            
            // Wait for DOM to be ready
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', () => this.setup());
            } else {
                await this.setup();
            }
        } catch (error) {
            console.error('Failed to initialize dashboard:', error);
            this.showError('Failed to initialize dashboard');
        }
    }

    /**
     * Setup charts and start data loading
     */
    async setup() {
        try {
            // Check if Chart.js is loaded
            if (typeof Chart === 'undefined') {
                console.error('Chart.js is not loaded');
                this.showError('Chart.js library is not loaded');
                return;
            }

            // Initialize chart containers
            this.initializeChartContainers();
            
            // Load initial data
            await this.loadDashboardData();
            
            // Start auto-refresh
            this.startAutoRefresh();
            
            // Setup responsive handlers
            this.setupResponsiveHandlers();
            
            console.log('Dashboard initialized successfully');
        } catch (error) {
            console.error('Failed to setup dashboard:', error);
            this.showError('Failed to setup dashboard charts');
        }
    }

    /**
     * Initialize chart containers in the DOM
     */
    initializeChartContainers() {
        // Create pie chart container if it doesn't exist
        if (!document.getElementById('expensePieChart')) {
            const pieContainer = this.createChartContainer('expensePieChart', 'Expenses by Category');
            const targetElement = document.querySelector('.col-lg-4 .card:last-child .card-body') || 
                                 document.querySelector('.row .col-lg-4:last-child .card .card-body');
            if (targetElement) {
                targetElement.innerHTML = '';
                targetElement.appendChild(pieContainer);
            }
        }

        // Create bar chart container if it doesn't exist
        if (!document.getElementById('monthlyTrendChart')) {
            const barContainer = this.createChartContainer('monthlyTrendChart', 'Monthly Expense Trends');
            const targetElement = document.querySelector('.col-lg-8 .card:last-child .card-body') ||
                                 document.querySelector('.row .col-lg-8:last-child .card .card-body');
            if (targetElement) {
                targetElement.innerHTML = '';
                targetElement.appendChild(barContainer);
            }
        }
    }

    /**
     * Create a chart container element
     */
    createChartContainer(id, title) {
        const container = document.createElement('div');
        container.className = 'chart-container position-relative';
        container.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h6 class="mb-0"><i class="fas fa-chart-pie me-2"></i>${title}</h6>
                <div class="chart-loading" id="${id}Loading" style="display: none;">
                    <div class="spinner-border spinner-border-sm text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                </div>
            </div>
            <div class="chart-wrapper" style="position: relative; height: 300px;">
                <canvas id="${id}" style="max-height: 300px;"></canvas>
            </div>
            <div class="chart-error alert alert-danger mt-3" id="${id}Error" style="display: none;">
                <i class="fas fa-exclamation-triangle me-2"></i>
                <span class="error-message">Failed to load chart data</span>
            </div>
            <div class="chart-empty alert alert-info mt-3" id="${id}Empty" style="display: none;">
                <i class="fas fa-info-circle me-2"></i>
                <span>No data available for this chart</span>
            </div>
        `;
        return container;
    }

    /**
     * Load dashboard data via AJAX
     */
    async loadDashboardData() {
        if (this.isLoading) return;
        
        this.isLoading = true;
        this.showLoading(true);

        try {
            console.log('Loading dashboard data...');
            
            // Load data in parallel
            const [categoryData, monthlyData] = await Promise.all([
                this.fetchCategoryData(),
                this.fetchMonthlyData()
            ]);

            // Update charts with new data
            this.updatePieChart(categoryData);
            this.updateBarChart(monthlyData);
            
            // Hide loading indicators
            this.hideError();
            
            console.log('Dashboard data loaded successfully');
        } catch (error) {
            console.error('Failed to load dashboard data:', error);
            this.showError('Failed to load dashboard data. Please try refreshing the page.');
        } finally {
            this.isLoading = false;
            this.showLoading(false);
        }
    }

    /**
     * Fetch category expense data
     */
    async fetchCategoryData() {
        try {
            const response = await fetch('/api/expenses/by-category', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                timeout: 10000
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Failed to fetch category data:', error);
            // Return mock data as fallback
            return this.getMockCategoryData();
        }
    }

    /**
     * Fetch monthly trend data
     */
    async fetchMonthlyData() {
        try {
            const response = await fetch('/api/expenses/monthly-trends', {
                method: 'GET',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                timeout: 10000
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            return await response.json();
        } catch (error) {
            console.error('Failed to fetch monthly data:', error);
            // Return mock data as fallback
            return this.getMockMonthlyData();
        }
    }

    /**
     * Update pie chart with category data
     */
    updatePieChart(data) {
        const canvas = document.getElementById('expensePieChart');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');

        // Destroy existing chart
        if (this.pieChart) {
            this.pieChart.destroy();
        }

        // Check for empty data
        if (!data || !data.length) {
            this.showEmptyState('expensePieChart');
            return;
        }

        // Prepare chart data
        const chartData = {
            labels: data.map(item => item.category),
            datasets: [{
                data: data.map(item => item.amount),
                backgroundColor: this.chartColors.slice(0, data.length),
                borderColor: '#fff',
                borderWidth: 2,
                hoverBorderWidth: 3
            }]
        };

        // Create pie chart
        this.pieChart = new Chart(ctx, {
            type: 'pie',
            data: chartData,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: false
                    },
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 20,
                            usePointStyle: true,
                            font: {
                                size: 12
                            }
                        }
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const label = context.label || '';
                                const value = new Intl.NumberFormat('en-US', {
                                    style: 'currency',
                                    currency: 'USD'
                                }).format(context.parsed);
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((context.parsed / total) * 100).toFixed(1);
                                return `${label}: ${value} (${percentage}%)`;
                            }
                        }
                    }
                },
                animation: {
                    animateRotate: true,
                    duration: 1500
                }
            }
        });

        this.hideEmptyState('expensePieChart');
    }

    /**
     * Update bar chart with monthly data
     */
    updateBarChart(data) {
        const canvas = document.getElementById('monthlyTrendChart');
        if (!canvas) return;

        const ctx = canvas.getContext('2d');

        // Destroy existing chart
        if (this.barChart) {
            this.barChart.destroy();
        }

        // Check for empty data
        if (!data || !data.length) {
            this.showEmptyState('monthlyTrendChart');
            return;
        }

        // Prepare chart data
        const chartData = {
            labels: data.map(item => item.month),
            datasets: [{
                label: 'Monthly Expenses',
                data: data.map(item => item.amount),
                backgroundColor: 'rgba(54, 162, 235, 0.2)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 2,
                borderRadius: 4,
                borderSkipped: false,
                hoverBackgroundColor: 'rgba(54, 162, 235, 0.4)',
                hoverBorderColor: 'rgba(54, 162, 235, 1)',
                hoverBorderWidth: 3
            }]
        };

        // Create bar chart
        this.barChart = new Chart(ctx, {
            type: 'bar',
            data: chartData,
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    title: {
                        display: false
                    },
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return `Expenses: ${new Intl.NumberFormat('en-US', {
                                    style: 'currency',
                                    currency: 'USD'
                                }).format(context.parsed.y)}`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        display: true,
                        title: {
                            display: true,
                            text: 'Month',
                            font: {
                                weight: 'bold'
                            }
                        },
                        grid: {
                            display: false
                        }
                    },
                    y: {
                        display: true,
                        title: {
                            display: true,
                            text: 'Amount ($)',
                            font: {
                                weight: 'bold'
                            }
                        },
                        beginAtZero: true,
                        ticks: {
                            callback: function(value) {
                                return '$' + value.toLocaleString();
                            }
                        }
                    }
                },
                animation: {
                    duration: 1500,
                    easing: 'easeInOutQuart'
                }
            }
        });

        this.hideEmptyState('monthlyTrendChart');
    }

    /**
     * Start auto-refresh every 30 seconds
     */
    startAutoRefresh() {
        // Clear existing interval
        if (this.updateInterval) {
            clearInterval(this.updateInterval);
        }

        // Start new interval
        this.updateInterval = setInterval(() => {
            console.log('Auto-refreshing dashboard data...');
            this.loadDashboardData();
        }, 30000); // 30 seconds

        console.log('Auto-refresh started (30 second intervals)');
    }

    /**
     * Stop auto-refresh
     */
    stopAutoRefresh() {
        if (this.updateInterval) {
            clearInterval(this.updateInterval);
            this.updateInterval = null;
            console.log('Auto-refresh stopped');
        }
    }

    /**
     * Setup responsive handlers
     */
    setupResponsiveHandlers() {
        let resizeTimeout;
        
        window.addEventListener('resize', () => {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(() => {
                if (this.pieChart) {
                    this.pieChart.resize();
                }
                if (this.barChart) {
                    this.barChart.resize();
                }
            }, 250);
        });

        // Handle visibility change (pause updates when tab is hidden)
        document.addEventListener('visibilitychange', () => {
            if (document.hidden) {
                this.stopAutoRefresh();
            } else {
                this.startAutoRefresh();
                // Refresh data when tab becomes visible
                setTimeout(() => this.loadDashboardData(), 1000);
            }
        });
    }

    /**
     * Show loading indicators
     */
    showLoading(show) {
        const loadingElements = document.querySelectorAll('.chart-loading');
        loadingElements.forEach(element => {
            element.style.display = show ? 'block' : 'none';
        });
    }

    /**
     * Show error message
     */
    showError(message) {
        const errorElements = document.querySelectorAll('.chart-error');
        errorElements.forEach(element => {
            const messageElement = element.querySelector('.error-message');
            if (messageElement) {
                messageElement.textContent = message;
            }
            element.style.display = 'block';
        });
    }

    /**
     * Hide error message
     */
    hideError() {
        const errorElements = document.querySelectorAll('.chart-error');
        errorElements.forEach(element => {
            element.style.display = 'none';
        });
    }

    /**
     * Show empty state for specific chart
     */
    showEmptyState(chartId) {
        const emptyElement = document.getElementById(chartId + 'Empty');
        if (emptyElement) {
            emptyElement.style.display = 'block';
        }
    }

    /**
     * Hide empty state for specific chart
     */
    hideEmptyState(chartId) {
        const emptyElement = document.getElementById(chartId + 'Empty');
        if (emptyElement) {
            emptyElement.style.display = 'none';
        }
    }

    /**
     * Get mock category data (fallback)
     */
    getMockCategoryData() {
        const currentDate = new Date();
        const categories = ['Food', 'Transportation', 'Entertainment', 'Shopping', 'Utilities', 'Healthcare', 'Education'];
        
        return categories.map((category, index) => ({
            category: category,
            amount: Math.floor(Math.random() * 500) + 50,
            count: Math.floor(Math.random() * 20) + 1
        })).filter(item => item.amount > 0);
    }

    /**
     * Get mock monthly data (fallback)
     */
    getMockMonthlyData() {
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        const currentMonth = new Date().getMonth();
        const displayMonths = [];
        
        // Show last 6 months
        for (let i = 5; i >= 0; i--) {
            const monthIndex = (currentMonth - i + 12) % 12;
            displayMonths.push(months[monthIndex]);
        }

        return displayMonths.map(month => ({
            month: month,
            amount: Math.floor(Math.random() * 1000) + 200,
            count: Math.floor(Math.random() * 50) + 5
        }));
    }

    /**
     * Destroy dashboard instance
     */
    destroy() {
        // Stop auto-refresh
        this.stopAutoRefresh();

        // Destroy charts
        if (this.pieChart) {
            this.pieChart.destroy();
            this.pieChart = null;
        }

        if (this.barChart) {
            this.barChart.destroy();
            this.barChart = null;
        }

        console.log('Dashboard destroyed');
    }

    /**
     * Refresh dashboard manually
     */
    async refresh() {
        console.log('Manual dashboard refresh triggered');
        await this.loadDashboardData();
    }
}

// Global dashboard instance
let expenseDashboard = null;

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Only initialize if we're on the dashboard page
    if (window.location.pathname === '/' || window.location.pathname === '/dashboard') {
        expenseDashboard = new ExpenseDashboard();
        
        // Make dashboard available globally for debugging
        window.expenseDashboard = expenseDashboard;
        
        console.log('Dashboard module loaded and initialized');
    }
});

// Handle page unload
window.addEventListener('beforeunload', function() {
    if (expenseDashboard) {
        expenseDashboard.destroy();
    }
});

// Export for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ExpenseDashboard;
}