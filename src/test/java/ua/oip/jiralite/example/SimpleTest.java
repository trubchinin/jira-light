package ua.oip.jiralite.example;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Простий тест для перевірки роботи Maven Surefire
 */
public class SimpleTest {
    
    @Test
    @DisplayName("Перевірка додавання")
    void testAddition() {
        int result = 2 + 2;
        assertThat(result).isEqualTo(4);
    }
    
    @Test
    @DisplayName("Перевірка множення")
    void testMultiplication() {
        int result = 2 * 3;
        assertThat(result).isEqualTo(6);
    }
    
    @Test
    @DisplayName("Перевірка ділення")
    void testDivision() {
        double result = 10.0 / 2.0;
        assertThat(result).isEqualTo(5.0);
    }
} 