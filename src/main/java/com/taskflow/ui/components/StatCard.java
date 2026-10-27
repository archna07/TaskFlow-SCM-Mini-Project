package com.taskflow.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * StatCard component displaying dynamic dashboard metrics with Midnight Aurora accents.
 */
public class StatCard extends JPanel {

    private final String title;
    private int value;
    private final String subtext;
    private final Color accentColor;
    private final String iconSymbol;

    private JLabel valueLabel;
    private JLabel titleLabel;
    private JLabel subtextLabel;

    public StatCard(String title, int initialValue, String subtext, Color accentColor, String iconSymbol) {
        this.title = title;
        this.value = initialValue;
        this.subtext = subtext;
        this.accentColor = accentColor;
        this.iconSymbol = iconSymbol;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(Theme.emptyPadding(16, 20, 16, 20));
        setPreferredSize(new Dimension(220, 125));

        initComponents();
    }

    private void initComponents() {
        // Top row: Title and Icon Badge
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(Theme.fontSmallBold());
        titleLabel.setForeground(Theme.TEXT_MUTED);
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Circular icon badge
        JPanel iconBadge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);
                int size = Math.min(getWidth(), getHeight());
                // Circle bg with alpha
                g2.setColor(Theme.withAlpha(accentColor, 35));
                g2.fillOval(0, 0, size, size);
                // Border
                g2.setColor(Theme.withAlpha(accentColor, 100));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawOval(0, 0, size - 1, size - 1);

                // Symbol
                g2.setFont(Theme.fontSubheader());
                g2.setColor(accentColor);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (size - fm.stringWidth(iconSymbol)) / 2;
                int ty = (size + fm.getAscent()) / 2 - 2;
                g2.drawString(iconSymbol, tx, ty);
                g2.dispose();
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setPreferredSize(new Dimension(30, 30));
        topPanel.add(iconBadge, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Center: Big Value
        valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setFont(Theme.fontStatValue());
        valueLabel.setForeground(Theme.TEXT_PRIMARY);
        add(valueLabel, BorderLayout.CENTER);

        // Bottom: Subtext
        subtextLabel = new JLabel(subtext);
        subtextLabel.setFont(Theme.fontSmall());
        subtextLabel.setForeground(Theme.TEXT_SECONDARY);
        add(subtextLabel, BorderLayout.SOUTH);
    }

    public void setValue(int newValue) {
        this.value = newValue;
        if (valueLabel != null) {
            valueLabel.setText(String.valueOf(newValue));
        }
        repaint();
    }

    public int getValue() {
        return value;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int arc = 16;

        // Card surface background
        g2.setColor(Theme.BG_CARD);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Subtle border
        g2.setColor(Theme.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

        // Top accent line glow
        GradientPaint topAccent = new GradientPaint(
                0, 0, accentColor,
                w / 2f, 0, Theme.withAlpha(accentColor, 40)
        );
        g2.setPaint(topAccent);
        g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, 4, 3, 3));

        g2.dispose();
        super.paintComponent(g);
    }
}
