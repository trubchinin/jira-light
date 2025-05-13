package ua.oip.jiralite.domain.enums;

public enum Priority {
    LOWEST(1, "Lowest"),
    LOW(2, "Low"),
    MEDIUM(3, "Medium"),
    HIGH(4, "High"),
    HIGHEST(5, "Highest");
    
    private final int level;
    private final String displayName;
    
    Priority(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
} 