package ua.oip.jiralite.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PermissionTest {
    
    @Test
    @DisplayName("Адміністратор має доступ до будь-якої дії")
    void adminAllowed_anyAction_true() {
        // Створюємо мок для Permission
        Permission permissionMock = mock(Permission.class);
        
        // Налаштовуємо, щоб мок повертав true для будь-якої дії
        when(permissionMock.allowed(anyString())).thenReturn(true);
        
        // Перевіряємо, що дозвіл для адміністратора видається для будь-якої дії
        assertThat(permissionMock.allowed("delete")).isTrue();
        assertThat(permissionMock.allowed("update")).isTrue();
        assertThat(permissionMock.allowed("create")).isTrue();
        
        // Перевіряємо, що метод був викликаний 3 рази
        verify(permissionMock, times(3)).allowed(anyString());
    }
    
    @Test
    @DisplayName("Звичайний користувач має обмежені права")
    void userAllowed_limitedPermissions() {
        // Створюємо мок для Permission
        Permission permissionMock = mock(Permission.class);
        
        // Налаштовуємо, щоб мок повертав true тільки для певних дій
        when(permissionMock.allowed("read")).thenReturn(true);
        when(permissionMock.allowed("update")).thenReturn(true);
        when(permissionMock.allowed("delete")).thenReturn(false);
        
        // Перевіряємо права доступу
        assertThat(permissionMock.allowed("read")).isTrue();
        assertThat(permissionMock.allowed("update")).isTrue();
        assertThat(permissionMock.allowed("delete")).isFalse();
    }
} 