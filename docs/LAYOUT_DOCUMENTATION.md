# Layout Template Documentation

This document explains how to use the main layout template (`layout/main.html`) in the Personal Expense Tracker application.

## Overview

The `layout/main.html` template provides a comprehensive, reusable layout foundation for all pages in the application. It includes:

✅ **All Required Features:**
- Proper `<!DOCTYPE html>` with responsive meta tags
- Bootstrap 5.3.2 CSS and JS via WebJars and CDN fallback
- jQuery 3.7.1 for AJAX functionality
- Navigation bar with Dashboard, Expenses, Categories links
- Main content area with Thymeleaf fragments
- Footer with application info and useful links
- Font Awesome icons for enhanced UI
- Fully responsive design for mobile devices

## Template Structure

### Features Included

1. **Responsive Design**: Mobile-first approach with Bootstrap 5.3.2
2. **Navigation**: Sticky navigation bar with active link highlighting
3. **Flash Messages**: Support for success, error, info, and warning alerts
4. **AJAX Support**: jQuery 3.7.1 with common AJAX utilities
5. **Icons**: Font Awesome 6.5.1 for consistent iconography
6. **Footer**: Informative footer with copyright and useful links
7. **Loading States**: Built-in loading spinner functionality
8. **Accessibility**: Screen reader support and proper ARIA labels

### Dependencies

- **Bootstrap 5.3.2**: CSS framework for responsive design
- **jQuery 3.7.1**: JavaScript library for DOM manipulation and AJAX
- **Font Awesome 6.5.1**: Icon library
- **Chart.js**: For data visualization (included in page-specific scripts)

## Usage Examples

### Basic Page Template

```html
<!DOCTYPE html>
<html lang="en" 
      xmlns:th="http://www.thymeleaf.org" 
      th:replace="~{layout/main :: layout(~{::title}, ~{::content})}">
<head>
    <title>Your Page Title</title>
</head>
<body>
    <div th:fragment="content">
        <h1>Your Page Content</h1>
        <p>This content will be inserted into the main layout.</p>
    </div>
</body>
</html>
```

### Page with Custom Scripts

```html
<!DOCTYPE html>
<html lang="en" 
      xmlns:th="http://www.thymeleaf.org" 
      th:replace="~{layout/main :: layout(~{::title}, ~{::content})}">
<head>
    <title>Dashboard</title>
</head>
<body>
    <div th:fragment="content">
        <!-- Your page content here -->
        <div class="row">
            <div class="col-md-6">
                <canvas id="myChart"></canvas>
            </div>
        </div>
    </div>

    <!-- Page-specific Scripts -->
    <th:block th:fragment="scripts">
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script>
            // Your custom JavaScript here
            $(document).ready(function() {
                // Chart initialization
                const ctx = document.getElementById('myChart').getContext('2d');
                // ... chart configuration
            });
        </script>
    </th:block>
</body>
</html>
```

## Flash Message Support

The layout automatically displays flash messages. Set them in your controller:

```java
@Controller
public class MyController {
    
    @GetMapping("/example")
    public String example(Model model, RedirectAttributes redirectAttributes) {
        // Success message
        redirectAttributes.addFlashAttribute("success", "Operation completed successfully!");
        
        // Error message
        redirectAttributes.addFlashAttribute("error", "Something went wrong!");
        
        // Info message
        redirectAttributes.addFlashAttribute("info", "Here's some information.");
        
        // Warning message
        redirectAttributes.addFlashAttribute("warning", "Please be careful!");
        
        return "redirect:/somewhere";
    }
}
```

## Navigation

The navigation bar automatically highlights the active page based on the current URL:

- `/` - Dashboard (active when on home page)
- `/expenses` - Expenses (active when URL starts with `/expenses`)
- `/categories` - Categories (active when URL starts with `/categories`)

Additional navigation items in the Settings dropdown:
- About page
- Database Console (H2 console)
- API Analytics

## Utility Functions

The layout includes several JavaScript utility functions:

### Show Alert
```javascript
showAlert('Your message here', 'success'); // Types: success, danger, warning, info
```

### Format Currency
```javascript
const formatted = formatCurrency(123.45); // Returns "$123.45"
```

### Format Date
```javascript
const formatted = formatDate('2023-12-25'); // Returns "Dec 25, 2023"
```

## Styling

The layout includes comprehensive CSS with:

- **Card Styles**: Enhanced cards with hover effects
- **Button Styles**: Consistent button styling
- **Table Styles**: Professional table appearance
- **Responsive Utilities**: Mobile-friendly responsive design
- **Custom Components**: Statistics cards, loading spinners, chart containers

## Mobile Responsiveness

The layout is fully responsive with:
- Collapsible navigation on mobile devices
- Responsive grid system
- Touch-friendly interface elements
- Optimized font sizes for different screen sizes

## Browser Support

- Modern browsers (Chrome, Firefox, Safari, Edge)
- Mobile browsers (iOS Safari, Chrome Mobile)
- Responsive design works on all screen sizes

## File Structure

```
src/main/resources/templates/
├── layout/
│   └── main.html                 # Main layout template
├── home/
│   ├── dashboard.html           # Original dashboard
│   ├── dashboard-with-layout.html # Dashboard using new layout
│   └── about.html               # About page
├── fragments/
│   └── layout.html              # Original layout fragment (deprecated)
└── ...
```

## Migration from Old Layout

To convert existing templates to use the new layout:

1. Replace the existing HTML structure with the layout template reference
2. Move page content into a `th:fragment="content"` block
3. Move page-specific scripts to `th:fragment="scripts"` block
4. Update page titles to use the layout parameter
5. Remove duplicate HTML, CSS, and JavaScript includes

## Examples in Codebase

- `home/dashboard-with-layout.html` - Full dashboard example
- `home/about.html` - Standalone page with complete HTML
- Check the demo at `/demo-layout` endpoint

## Best Practices

1. **Use the layout template** for all new pages to maintain consistency
2. **Keep page-specific content minimal** - let the layout handle common elements
3. **Use flash messages** for user feedback instead of custom alerts
4. **Leverage utility functions** for common operations like currency formatting
5. **Follow responsive design patterns** provided by the layout
6. **Test on multiple screen sizes** to ensure proper responsive behavior

## Performance Considerations

- CSS and JS files are loaded from CDN with WebJars fallback
- Bootstrap and jQuery are cached by browsers
- Minimal custom CSS for fast loading
- Optimized for mobile performance

This layout template provides a solid foundation for building consistent, professional, and responsive web pages in the Personal Expense Tracker application.