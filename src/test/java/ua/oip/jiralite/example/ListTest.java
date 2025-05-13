package ua.oip.jiralite.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тести для колекцій списків
 */
public class ListTest {
    
    @Test
    @DisplayName("Перевірка додавання елементів у список")
    void testListAdd() {
        List<String> list = new ArrayList<>();
        list.add("Item 1");
        list.add("Item 2");
        
        assertThat(list).hasSize(2);
        assertThat(list).contains("Item 1", "Item 2");
    }
    
    @Test
    @DisplayName("Перевірка видалення елементів зі списку")
    void testListRemove() {
        List<String> list = new ArrayList<>(Arrays.asList("A", "B", "C"));
        list.remove("B");
        
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly("A", "C");
    }
    
    @Test
    @DisplayName("Перевірка перетину двох списків")
    void testListIntersection() {
        List<Integer> list1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> list2 = Arrays.asList(3, 4, 5, 6);
        
        List<Integer> intersection = new ArrayList<>(list1);
        intersection.retainAll(list2);
        
        assertThat(intersection).hasSize(2);
        assertThat(intersection).containsExactly(3, 4);
    }
} 