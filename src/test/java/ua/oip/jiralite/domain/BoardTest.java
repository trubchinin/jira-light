package ua.oip.jiralite.domain;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

class BoardTest {
    
    private Board board;
    
    @BeforeEach
    void setUp() {
        // Board має ініціалізувати 3 стандартні колонки
        board = new Board();
    }

    @Test
    @DisplayName("Пошук колонки за іменем повертає існуючу колонку")
    void findColumn_existingName_returnsColumn() {
        // Шукаємо колонку "To Do"
        BoardColumn column = board.findColumn("To Do");
        
        // Перевіряємо, що колонка знайдена та має правильну назву
        assertThat(column).isNotNull();
        assertThat(column.getName()).isEqualTo("To Do");
    }
    
    @Test
    @DisplayName("При створенні дошка має стандартні колонки")
    void boardInitialization_hasDefaultColumns() {
        // Перевіряємо що є колонки і їх кількість
        assertThat(board.getColumns()).isNotNull();
        assertThat(board.getColumns()).hasSize(3);
        
        // Перевіряємо назви колонок
        assertThat(board.getColumns()).extracting("name")
            .containsExactlyInAnyOrder("To Do", "In Progress", "Done");
    }
} 