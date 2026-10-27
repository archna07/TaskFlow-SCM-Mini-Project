package com.taskflow.ui.components;

import java.awt.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * Midnight Aurora design system tokens and graphics utilities for TaskFlow.
 */
public final class Theme {

    private Theme() {}

    // --- Color Palette: "Midnight Aurora" ---
    public static final Color BG_MAIN          = new Color(0x10, 0x14, 0x26); // Deep Midnight Navy #101426
    public static final Color BG_SIDEBAR       = new Color(0x13, 0x18, 0x32); // Slightly lighter sidebar #131832
    public static final Color BG_HEADER        = new Color(0x14, 0x1A, 0x35); // Top header #141A35
    public static final Color BG_PANEL         = new Color(0x19, 0x1F, 0x38); // Dark Indigo Panel #191F38
    public static final Color BG_CARD          = new Color(0x1E, 0x26, 0x48); // Card surface #1E2648
    public static final Color BG_CARD_HOVER    = new Color(0x25, 0x30, 0x5C); // Card hover state #25305C
    public static final Color BG_INPUT         = new Color(0x15, 0x1B, 0x33); // Input fields #151B33

    public static final Color BORDER_SUBTLE    = new Color(0x27, 0x32, 0x5C); // Subtle borders #27325C
    public static final Color BORDER_LIGHT     = new Color(0x3B, 0x4B, 0x85); // Active / hover borders
    public static final Color BORDER_FOCUS     = new Color(0x8B, 0x5C, 0xF6); // Violet focus ring

    // Accents
    public static final Color ACCENT_VIOLET    = new Color(0x8B, 0x5C, 0xF6); // Electric Violet #8B5CF6
    public static final Color ACCENT_VIOLET_HOVER = new Color(0x7C, 0x3A, 0xED); // #7C3AED
    public static final Color ACCENT_CYAN      = new Color(0x43, 0xD9, 0xFF); // Vibrant Cyan #43D9FF
    public static final Color ACCENT_CYAN_HOVER= new Color(0x22, 0xD3, 0xEE);

    // Text & Content
    public static final Color TEXT_PRIMARY     = new Color(0xF8, 0xFA, 0xFC); // Crisp off-white #F8FAFC
    public static final Color TEXT_SECONDARY   = new Color(0x94, 0xA3, 0xB8); // Slate silver #94A3B8
    public static final Color TEXT_MUTED       = new Color(0x64, 0x74, 0x8B); // Muted slate #64748B
    public static final Color TEXT_WHITE       = Color.WHITE;

    // Status Indicators
    public static final Color STATUS_GREEN     = new Color(0x10, 0xB9, 0x81); // Emerald #10B981 (Completed)
    public static final Color STATUS_AMBER     = new Color(0xF5, 0x9E, 0x0B); // Amber #F59E0B (Pending / Warning)
    public static final Color STATUS_RED       = new Color(0xEF, 0x44, 0x44); // Rose Coral #EF4444 (High / Overdue)
    public static final Color STATUS_BLUE      = new Color(0x38, 0xBD, 0xF8); // Sky #38BDF8

    // --- Typography ---
    private static final String FONT_FAMILY = resolveFontFamily();

    private static String resolveFontFamily() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fonts = ge.getAvailableFontFamilyNames();
        for (String candidate : new String[]{"Segoe UI", "Inter", "SF Pro Display", "Roboto", "Helvetica Neue", "Arial"}) {
            for (String f : fonts) {
                if (f.equalsIgnoreCase(candidate)) {
                    return candidate;
                }
            }
        }
        return Font.SANS_SERIF;
    }

    public static Font fontHero() {
        return new Font(FONT_FAMILY, Font.BOLD, 22);
    }

    public static Font fontTitle() {
        return new Font(FONT_FAMILY, Font.BOLD, 17);
    }

    public static Font fontHeader() {
        return new Font(FONT_FAMILY, Font.BOLD, 15);
    }

    public static Font fontSubheader() {
        return new Font(FONT_FAMILY, Font.BOLD, 13);
    }

    public static Font fontBody() {
        return new Font(FONT_FAMILY, Font.PLAIN, 13);
    }

    public static Font fontBodyBold() {
        return new Font(FONT_FAMILY, Font.BOLD, 13);
    }

    public static Font fontSmall() {
        return new Font(FONT_FAMILY, Font.PLAIN, 11);
    }

    public static Font fontSmallBold() {
        return new Font(FONT_FAMILY, Font.BOLD, 11);
    }

    public static Font fontStatValue() {
        return new Font(FONT_FAMILY, Font.BOLD, 28);
    }

    // --- Utilities ---
    public static void applyAntialiasing(Graphics g) {
        if (g instanceof Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }

    public static Border emptyPadding(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    /**
     * Creates a semi-transparent version of a color.
     */
    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(255, Math.max(0, alpha)));
    }
}
