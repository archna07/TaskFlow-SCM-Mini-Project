package com.taskflow.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern pill-shaped badge component for categories, priorities, and status indicators.
 */
public class Badge extends JComponent {

    private String text;
    private Color badgeColor;
    private boolean showDot = true;

    public Badge(String text, Color badgeColor) {
        this(text, badgeColor, true);
    }

    public Badge(String text, Color badgeColor, boolean showDot) {
        this.text = text != null ? text : "";
        this.badgeColor = badgeColor != null ? badgeColor : Theme.TEXT_MUTED;
        this.showDot = showDot;
        setFont(Theme.fontSmallBold());
        updatePreferredSize();
    }

    public void setText(String text) {
        this.text = text != null ? text : "";
        updatePreferredSize();
        repaint();
    }

    public void setBadgeColor(Color badgeColor) {
        this.badgeColor = badgeColor;
        repaint();
    }

    private void updatePreferredSize() {
        FontMetrics fm = getFontMetrics(Theme.fontSmallBold());
        int textW = fm.stringWidth(text);
        int pad = showDot ? 26 : 18;
        setPreferredSize(new Dimension(textW + pad, 22));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int arc = h;

        // Background with subtle alpha
        g2.setColor(Theme.withAlpha(badgeColor, 28));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Border with subtle alpha
        g2.setColor(Theme.withAlpha(badgeColor, 85));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

        int startX = 10;
        // Dot indicator
        if (showDot) {
            g2.setColor(badgeColor);
            int dotSize = 6;
            int dotY = (h - dotSize) / 2;
            g2.fillOval(startX, dotY, dotSize, dotSize);
            startX += 11;
        }

        // Text
        g2.setFont(Theme.fontSmallBold());
        g2.setColor(badgeColor);
        FontMetrics fm = g2.getFontMetrics();
        int textY = (h + fm.getAscent()) / 2 - 2;
        g2.drawString(text, startX, textY);

        g2.dispose();
    }
}
