package com.taskflow.model;

import java.awt.Color;

/**
 * Supported task categories in TaskFlow with signature Midnight Aurora accent colors.
 */
public enum TaskCategory {
    STUDY("Study", "#8B5CF6"),      // Electric Violet
    WORK("Work", "#38BDF8"),        // Sky Blue
    PROJECT("Project", "#43D9FF"),  // Cyan Accent
    PERSONAL("Personal", "#EC4899"),// Rose Pink
    OTHER("Other", "#94A3B8");      // Slate Gray

    private final String displayName;
    private final String hexColor;

    TaskCategory(String displayName, String hexColor) {
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

    public static TaskCategory fromString(String text) {
        if (text == null) {
            return OTHER;
        }
        for (TaskCategory c : TaskCategory.values()) {
            if (c.name().equalsIgnoreCase(text.trim()) || c.displayName.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return OTHER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
