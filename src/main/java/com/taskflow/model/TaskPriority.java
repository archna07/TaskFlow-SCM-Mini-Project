package com.taskflow.model;

import java.awt.Color;

/**
 * Defines priority levels for tasks in the TaskFlow system.
 */
public enum TaskPriority {
    LOW("Low", "#10B981"),      // Emerald Green
    MEDIUM("Medium", "#F59E0B"), // Amber / Warm Orange
    HIGH("High", "#EF4444");    // Rose / Coral Red

    private final String displayName;
    private final String hexColor;

    TaskPriority(String displayName, String hexColor) {
        this.displayName = displayName;
        this.hexColor = hexColor;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getHexColor() {
        return hexColor;
    }

    public Color getColor() {
        return Color.decode(hexColor);
    }

    public static TaskPriority fromString(String text) {
        if (text == null) {
            return MEDIUM;
        }
        for (TaskPriority p : TaskPriority.values()) {
            if (p.name().equalsIgnoreCase(text.trim()) || p.displayName.equalsIgnoreCase(text.trim())) {
                return p;
            }
        }
        return MEDIUM;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
