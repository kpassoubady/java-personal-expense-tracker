package com.expensetracker.app;

public class Fact {
    // implement a fact class

    // add facvtorial method
    /**
     * Calculates the factorial of a non-negative integer using recursion.
     *
     * @param n the non-negative integer whose factorial is to be computed
     * @return the factorial of the given integer n
     * @throws IllegalArgumentException if n is negative
     */
    public static long factorial(int n) {
        // implement using recursion
        if (n < 0) {
            throw new IllegalArgumentException("Negative numbers do not have factorials.");
        }
        if (n == 0) {
            return 1;
        }
        return n * factorial(n - 1);
    }

    // Implement a method to calculate the nth Fibonacci number
    /**
     * Calculates the n-th Fibonacci number using recursion.
     *
     * @param n the position in the Fibonacci sequence (must be non-negative)
     * @return the n-th Fibonacci number
     * @throws IllegalArgumentException if {@code n} is negative
     */
    public static long fibonacci(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Negative numbers are not allowed.");
        }
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
}