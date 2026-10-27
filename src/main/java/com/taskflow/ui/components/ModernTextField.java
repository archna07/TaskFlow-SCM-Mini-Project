package com.taskflow.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.RoundRectangle2D;

/**
 * Styled text input field with placeholder text and focus glow border.
 */
public class ModernTextField extends JTextField {

    private String placeholder = "";
    private boolean isFocused = false;
    private int cornerRadius = 10;

    public ModernTextField(String placeholder) {
        this.placeholder = placeholder;
        init();
    }

    public ModernTextField(String placeholder, int columns) {
        super(columns);
        this.placeholder = placeholder;
        init();
    }

    private void init() {
        setFont(Theme.fontBody());
        setForeground(Theme.TEXT_PRIMARY);
        setCaretColor(Theme.ACCENT_CYAN);
        setSelectionColor(new Color(0x8B, 0x5C, 0xF6, 120));
        setSelectedTextColor(Theme.TEXT_WHITE);
        setOpaque(false);
        setBorder(Theme.emptyPadding(8, 14, 8, 14));

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                isFocused = true;
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                isFocused = false;
                repaint();
            }
        });
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();

        // Background
        g2.setColor(Theme.BG_INPUT);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));

        // Border: Focused violet glow or subtle border
        if (isFocused) {
            g2.setColor(Theme.BORDER_FOCUS);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Float(1, 1, w - 2, h - 2, cornerRadius, cornerRadius));
        } else {
            g2.setColor(Theme.BORDER_SUBTLE);
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, cornerRadius, cornerRadius));
        }

        g2.dispose();
        super.paintComponent(g);

        // Placeholder text if empty and not focused
        if (getText().isEmpty() && !placeholder.isEmpty()) {
            Graphics2D gPlaceholder = (Graphics2D) g.create();
            Theme.applyAntialiasing(gPlaceholder);
            gPlaceholder.setFont(getFont());
            gPlaceholder.setColor(Theme.TEXT_MUTED);
            Insets insets = getInsets();
            FontMetrics fm = gPlaceholder.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            gPlaceholder.drawString(placeholder, insets.left, y);
            gPlaceholder.dispose();
        }
    }
}
