package com.taskflow.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Modern empty state placeholder when no tasks match current filter/search.
 */
public class EmptyStatePanel extends JPanel {

    private final JLabel titleLabel;
    private final JLabel messageLabel;
    private final CustomButton actionButton;

    public EmptyStatePanel(String title, String message, Runnable onActionClick) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);
        setBorder(Theme.emptyPadding(40, 20, 40, 20));

        // Visual Icon placeholder
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                int r = 32;

                // Outer circle glow
                g2.setColor(new Color(0x8B, 0x5C, 0xF6, 25));
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(new Color(0x8B, 0x5C, 0xF6, 70));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);

                // Inner notebook / check emblem
                g2.setColor(Theme.ACCENT_CYAN);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Document shape
                g2.drawRoundRect(cx - 14, cy - 18, 28, 36, 6, 6);
                // Lines
                g2.drawLine(cx - 8, cy - 8, cx + 8, cy - 8);
                g2.drawLine(cx - 8, cy - 1, cx + 8, cy - 1);
                g2.drawLine(cx - 8, cy + 6, cx + 3, cy + 6);

                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(80, 80));
        iconPanel.setMaximumSize(new Dimension(80, 80));
        iconPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(iconPanel);

        add(Box.createVerticalStrut(16));

        titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.fontTitle());
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(titleLabel);

        add(Box.createVerticalStrut(8));

        messageLabel = new JLabel(message);
        messageLabel.setFont(Theme.fontBody());
        messageLabel.setForeground(Theme.TEXT_MUTED);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(messageLabel);

        if (onActionClick != null) {
            add(Box.createVerticalStrut(20));
            actionButton = new CustomButton("+ Create Task", CustomButton.ButtonStyle.PRIMARY);
            actionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            actionButton.setMaximumSize(new Dimension(160, 38));
            actionButton.addActionListener(e -> onActionClick.run());
            add(actionButton);
        } else {
            actionButton = null;
        }
    }

    public void setMessage(String title, String message) {
        titleLabel.setText(title);
        messageLabel.setText(message);
        repaint();
    }
}
