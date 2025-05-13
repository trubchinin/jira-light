package ua.oip.jiralite.domain;

import java.time.LocalDateTime;

/**
 * Базовий абстрактний клас для всіх сутностей.
 * Інкапсулює технічні поля: ідентифікатор та часові мітки.
 */
public abstract class BaseEntity {

    /** Унікальний ідентифікатор сутності. */
    protected Long id;

    /** Дата-час створення. */
    protected final LocalDateTime createdAt;

    /** Дата-час останнього оновлення. */
    protected LocalDateTime updatedAt;

    protected BaseEntity() {
        this.id = null; // ID будет присвоен при сохранении в базу
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    // ── Гетери / Сетери ────────────────────────────────────────────────────────
    public Long getId()                 { return id; }
    public void setId(Long id)          { this.id = id; }
    
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public LocalDateTime getUpdatedAt()   { return updatedAt; }

    /** Оновити часову мітку зміни. */
    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }
} 