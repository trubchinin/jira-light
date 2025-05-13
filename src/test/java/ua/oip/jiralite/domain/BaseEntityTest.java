package ua.oip.jiralite.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class BaseEntityTest {

    @Test
    @DisplayName("Перевірка get/set методів для id")
    void setId_thenGetId_returnsSame() {
        // Створюємо анонімну реалізацію абстрактного класу
        BaseEntity entity = new BaseEntity() { };
        
        // Встановлюємо ID
        entity.setId(42L);
        
        // Перевіряємо, що getId повертає те саме значення
        assertThat(entity.getId()).isEqualTo(42L);
    }
} 