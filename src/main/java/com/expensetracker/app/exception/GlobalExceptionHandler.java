package com.expensetracker.app.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Global exception handler for the expense tracker application.
 * Provides centralized exception handling with user-friendly error pages
 * and proper error logging.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle EntityNotFoundException - when a requested entity is not found.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(EntityNotFoundException ex, 
                                     Model model, 
                                     HttpServletRequest request) {
        logger.warn("Entity not found: {}", ex.getMessage());
        
        model.addAttribute("errorTitle", "Not Found");
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorCode", "404");
        model.addAttribute("requestUrl", request.getRequestURL());
        
        return "error/404";
    }

    /**
     * Handle ValidationException - custom validation errors.
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ValidationException ex, 
                                          Model model, 
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        logger.warn("Validation error: {}", ex.getMessage());
        
        // For AJAX requests or API calls, return error page
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            model.addAttribute("errorTitle", "Validation Error");
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("errorCode", "400");
            return "error/400";
        }
        
        // For regular form submissions, add flash message and redirect
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Handle MethodArgumentNotValidException - Bean validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationErrors(MethodArgumentNotValidException ex, 
                                       Model model, 
                                       HttpServletRequest request) {
        logger.warn("Validation errors in form submission: {}", ex.getBindingResult().getErrorCount());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        model.addAttribute("errorTitle", "Form Validation Error");
        model.addAttribute("errorMessage", "Please correct the errors in the form and try again.");
        model.addAttribute("validationErrors", errors);
        model.addAttribute("errorCode", "400");
        model.addAttribute("requestUrl", request.getRequestURL());
        
        return "error/validation";
    }

    /**
     * Handle ConstraintViolationException - Database constraint violations.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolation(ConstraintViolationException ex, 
                                           Model model, 
                                           HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) {
        logger.warn("Constraint violation: {}", ex.getMessage());
        
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        StringBuilder message = new StringBuilder("Validation failed: ");
        
        for (ConstraintViolation<?> violation : violations) {
            message.append(violation.getMessage()).append("; ");
        }
        
        // For form submissions, add flash message and redirect
        redirectAttributes.addFlashAttribute("error", message.toString());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Handle DataIntegrityViolationException - Database constraint violations.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDataIntegrityViolation(DataIntegrityViolationException ex, 
                                              Model model, 
                                              HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {
        logger.error("Data integrity violation: {}", ex.getMessage());
        
        String userMessage = "This action cannot be completed due to data constraints. " +
                           "The item may be referenced by other records.";
        
        // Check for specific constraint violations
        String rootCause = ex.getRootCause() != null ? ex.getRootCause().getMessage() : "";
        if (rootCause.contains("category")) {
            userMessage = "Cannot delete this category because it has associated expenses. " +
                         "Please delete or reassign the expenses first.";
        } else if (rootCause.contains("duplicate") || rootCause.contains("unique")) {
            userMessage = "This name is already in use. Please choose a different name.";
        }
        
        redirectAttributes.addFlashAttribute("error", userMessage);
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Handle IllegalArgumentException - Invalid arguments.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgument(IllegalArgumentException ex, 
                                       Model model, 
                                       HttpServletRequest request,
                                       RedirectAttributes redirectAttributes) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    /**
     * Handle all other exceptions - Generic error handling.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, 
                                        Model model, 
                                        HttpServletRequest request) {
        logger.error("Unexpected error occurred", ex);
        
        model.addAttribute("errorTitle", "Internal Server Error");
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        model.addAttribute("errorCode", "500");
        model.addAttribute("requestUrl", request.getRequestURL());
        model.addAttribute("errorDetails", ex.getMessage());
        
        return "error/500";
    }
}