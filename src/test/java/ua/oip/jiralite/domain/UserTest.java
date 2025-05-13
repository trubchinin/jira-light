package ua.oip.jiralite.domain;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ua.oip.jiralite.domain.user.User;

class UserTest {

    @Test
    @DisplayName("Перевірка успішної валідації пароля")
    void setPassword_validatesSuccessfully() {
        User user = new User("kate");
        user.setPassword("secret");

        assertThat(user.checkPassword("secret".toCharArray()))
                .isTrue();
    }

    @Test
    @DisplayName("Перевірка хибної валідації пароля")
    void checkPassword_wrongPassword_fails() {
        User user = new User("kate");
        user.setPassword("secret");

        assertThat(user.checkPassword("hack".toCharArray()))
                .isFalse();
    }
} 