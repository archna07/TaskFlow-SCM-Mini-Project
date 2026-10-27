package com.taskflow.model;

import java.awt.Color;

/**
 * Defines current execution status for tasks in TaskFlow.
 */
public enum TaskStatus {
    PENDING("Pending", "#F59E0B"),
    COMPLETED("Completed", "#10B981");

    private final String displayName;
    private final String hexColor;

    TaskStatus(String displayName, String hexColor) {
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

    public static TaskStatus fromString(String text) {
        if (text == null) {
            return PENDING;
        }
        for (TaskStatus s : TaskStatus.values()) {
            if (s.name().equalsIgnoreCase(text.trim()) || s.displayName.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return PENDING;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
