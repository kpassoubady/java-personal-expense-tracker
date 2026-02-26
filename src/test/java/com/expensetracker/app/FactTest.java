package com.expensetracker.app;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;


class FactTest {

    @Test
    void testFibonacciZero() {
        assertThat(Fact.fibonacci(0)).isEqualTo(0);
    }

    @Test
    void testFibonacciOne() {
        assertThat(Fact.fibonacci(1)).isEqualTo(1);
    }

    @Test
    void testFibonacciSmallNumbers() {
        assertThat(Fact.fibonacci(2)).isEqualTo(1);
        assertThat(Fact.fibonacci(3)).isEqualTo(2);
        assertThat(Fact.fibonacci(4)).isEqualTo(3);
        assertThat(Fact.fibonacci(5)).isEqualTo(5);
        assertThat(Fact.fibonacci(6)).isEqualTo(8);
        assertThat(Fact.fibonacci(7)).isEqualTo(13);
        assertThat(Fact.fibonacci(10)).isEqualTo(55);
    }

    @Test
    void testFibonacciNegativeThrowsException() {
        assertThatThrownBy(() -> Fact.fibonacci(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Negative numbers are not allowed");
    }

    @Test
    void testFactorialZero() {
        assertThat(Fact.factorial(0)).isEqualTo(1);
    }

    @Test
    void testFactorialOne() {
        assertThat(Fact.factorial(1)).isEqualTo(1);
    }

    @Test
    void testFactorialSmallNumbers() {
        assertThat(Fact.factorial(2)).isEqualTo(2);
        assertThat(Fact.factorial(3)).isEqualTo(6);
        assertThat(Fact.factorial(4)).isEqualTo(24);
        assertThat(Fact.factorial(5)).isEqualTo(120);
        assertThat(Fact.factorial(6)).isEqualTo(720);
        assertThat(Fact.factorial(10)).isEqualTo(3628800);
    }

    @Test
    void testFactorialNegativeThrowsException() {
        assertThatThrownBy(() -> Fact.factorial(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Negative numbers do not have factorials");
    }
}