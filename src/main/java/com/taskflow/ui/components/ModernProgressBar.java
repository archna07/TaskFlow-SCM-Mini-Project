package com.taskflow.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * ModernProgressBar with Midnight Aurora violet-to-cyan gradient progress fill.
 */
public class ModernProgressBar extends JComponent {

    private double progressPercentage = 0.0; // 0.0 to 100.0

    public ModernProgressBar() {
        setPreferredSize(new Dimension(200, 14));
        setMinimumSize(new Dimension(100, 10));
    }

    public void setProgressPercentage(double percentage) {
        this.progressPercentage = Math.max(0.0, Math.min(100.0, percentage));
        repaint();
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int arc = h;

        // Background Track
        g2.setColor(Theme.BG_INPUT);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Border around track
        g2.setColor(Theme.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

        // Filled Progress Bar with Gradient
        if (progressPercentage > 0) {
            float fillWidth = (float) (w * (progressPercentage / 100.0));
            fillWidth = Math.max(fillWidth, (float) h); // Ensure cap visibility

            GradientPaint gradient = new GradientPaint(
                    0, 0, Theme.ACCENT_VIOLET,
                    fillWidth, 0, (progressPercentage >= 100.0) ? Theme.STATUS_GREEN : Theme.ACCENT_CYAN
            );
            g2.setPaint(gradient);
            g2.fill(new RoundRectangle2D.Float(1, 1, Math.min(fillWidth - 2, w - 2), h - 2, arc, arc));

            // Subtle gloss highlight line on top half
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fill(new RoundRectangle2D.Float(2, 2, Math.min(fillWidth - 4, w - 4), (h - 4) / 2.0f, arc / 2, arc / 2));
        }

        g2.dispose();
    }
}
