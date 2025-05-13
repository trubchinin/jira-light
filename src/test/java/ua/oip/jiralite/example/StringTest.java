package ua.oip.jiralite.example;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тести для рядків
 */
public class StringTest {
    
    @Test
    @DisplayName("Перевірка конкатенації рядків")
    void testStringConcatenation() {
        String result = "Hello" + " " + "World";
        assertThat(result).isEqualTo("Hello World");
    }
    
    @Test
    @DisplayName("Перевірка довжини рядка")
    void testStringLength() {
        String text = "Jira Lite";
        assertThat(text.length()).isEqualTo(9);
    }
    
    @Test
    @DisplayName("Перевірка методу equals")
    void testStringEquals() {
        String a = "Jira";
        String b = "Jira";
        String c = new String("Jira");
        
        assertThat(a).isEqualTo(b);
        assertThat(a).isEqualTo(c);
    }
    
    @Test
    @DisplayName("Перевірка методу trim")
    void testStringTrim() {
        String text = "  spaces  ";
        assertThat(text.trim()).isEqualTo("spaces");
    }
} 