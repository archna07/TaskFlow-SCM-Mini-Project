package com.taskflow.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern custom-painted button with Midnight Aurora aesthetics, rounded corners,
 * smooth hover states, and gradient/accent support.
 */
public class CustomButton extends JButton {

    public enum ButtonStyle {
        PRIMARY,      // Electric Violet to Cyan gradient / vibrant primary
        SECONDARY,    // Dark panel surface with subtle border
        ACCENT_CYAN,  // Cyan accent
        DANGER,       // Red action / delete
        SUCCESS,      // Green complete action
        NAV_ITEM      // Sidebar navigation button with active indicator
    }

    private final ButtonStyle style;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private boolean isSelectedNav = false;
    private int cornerRadius = 12;
    private Icon leadingIcon;

    public CustomButton(String text, ButtonStyle style) {
        super(text);
        this.style = style;
        init();
    }

    public CustomButton(String text, ButtonStyle style, Icon icon) {
        super(text);
        this.style = style;
        this.leadingIcon = icon;
        init();
    }

    private void init() {
        setFont(Theme.fontBodyBold());
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(10, 18, 10, 18));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    public void setSelectedNav(boolean selected) {
        this.isSelectedNav = selected;
        repaint();
    }

    public boolean isSelectedNav() {
        return isSelectedNav;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyAntialiasing(g2);

        int width = getWidth();
        int height = getHeight();

        Shape shape = new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius);

        switch (style) {
            case PRIMARY -> paintPrimary(g2, width, height, shape);
            case SECONDARY -> paintSecondary(g2, width, height, shape);
            case ACCENT_CYAN -> paintAccentCyan(g2, width, height, shape);
            case DANGER -> paintDanger(g2, width, height, shape);
            case SUCCESS -> paintSuccess(g2, width, height, shape);
            case NAV_ITEM -> paintNavItem(g2, width, height, shape);
        }

        // Paint Text and optional Icon
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(getText());
        int textHeight = fm.getAscent();

        int contentWidth = textWidth + (leadingIcon != null ? leadingIcon.getIconWidth() + 10 : 0);
        int startX = (style == ButtonStyle.NAV_ITEM) ? 20 : (width - contentWidth) / 2;
        int centerY = (height + textHeight) / 2 - 2;

        if (leadingIcon != null) {
            int iconY = (height - leadingIcon.getIconHeight()) / 2;
            leadingIcon.paintIcon(this, g2, startX, iconY);
            startX += leadingIcon.getIconWidth() + 10;
        }

        g2.drawString(getText(), startX, centerY);

        g2.dispose();
    }

    private void paintPrimary(Graphics2D g2, int w, int h, Shape shape) {
        Color c1 = isPressed ? Theme.ACCENT_VIOLET_HOVER.darker() :
                (isHovered ? Theme.ACCENT_VIOLET_HOVER : Theme.ACCENT_VIOLET);
        Color c2 = isPressed ? Theme.ACCENT_CYAN.darker() :
                (isHovered ? Theme.ACCENT_CYAN_HOVER : new Color(0x63, 0x66, 0xF1));

        GradientPaint gradient = new GradientPaint(0, 0, c1, w, h, c2);
        g2.setPaint(gradient);
        g2.fill(shape);

        // Subtle glow border
        g2.setColor(new Color(255, 255, 255, isHovered ? 60 : 30));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, cornerRadius, cornerRadius));

        g2.setColor(Theme.TEXT_WHITE);
    }

    private void paintSecondary(Graphics2D g2, int w, int h, Shape shape) {
        Color bg = isPressed ? Theme.BG_CARD.darker() :
                (isHovered ? Theme.BG_CARD_HOVER : Theme.BG_CARD);
        g2.setColor(bg);
        g2.fill(shape);

        // Border
        g2.setColor(isHovered ? Theme.BORDER_LIGHT : Theme.BORDER_SUBTLE);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, cornerRadius, cornerRadius));

        g2.setColor(isHovered ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
    }

    private void paintAccentCyan(Graphics2D g2, int w, int h, Shape shape) {
        Color bg = isPressed ? Theme.ACCENT_CYAN.darker() :
                (isHovered ? Theme.ACCENT_CYAN_HOVER : Theme.ACCENT_CYAN);
        g2.setColor(bg);
        g2.fill(shape);
        g2.setColor(new Color(0x0A, 0x0E, 0x1A)); // Dark text for contrast on bright cyan
    }

    private void paintDanger(Graphics2D g2, int w, int h, Shape shape) {
        Color bg = isPressed ? new Color(0x45, 0x1A, 0x22) :
                (isHovered ? new Color(0x3B, 0x16, 0x20) : new Color(0x28, 0x14, 0x1E));
        g2.setColor(bg);
        g2.fill(shape);

        g2.setColor(isHovered ? Theme.STATUS_RED : new Color(Theme.STATUS_RED.getRed(), Theme.STATUS_RED.getGreen(), Theme.STATUS_RED.getBlue(), 140));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, cornerRadius, cornerRadius));

        g2.setColor(Theme.STATUS_RED);
    }

    private void paintSuccess(Graphics2D g2, int w, int h, Shape shape) {
        Color bg = isPressed ? new Color(0x06, 0x38, 0x26) :
                (isHovered ? new Color(0x0B, 0x48, 0x32) : new Color(0x0D, 0x36, 0x26));
        g2.setColor(bg);
        g2.fill(shape);

        g2.setColor(isHovered ? Theme.STATUS_GREEN : new Color(Theme.STATUS_GREEN.getRed(), Theme.STATUS_GREEN.getGreen(), Theme.STATUS_GREEN.getBlue(), 160));
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, cornerRadius, cornerRadius));

        g2.setColor(Theme.STATUS_GREEN);
    }

    private void paintNavItem(Graphics2D g2, int w, int h, Shape shape) {
        if (isSelectedNav) {
            // Selected background
            g2.setColor(new Color(0x8B, 0x5C, 0xF6, 45));
            g2.fill(shape);

            // Left vertical highlight indicator bar
            g2.setColor(Theme.ACCENT_CYAN);
            g2.fillRoundRect(0, (h - 22) / 2, 4, 22, 3, 3);

            // Subtle border
            g2.setColor(new Color(0x8B, 0x5C, 0xF6, 80));
            g2.setStroke(new BasicStroke(1.0f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, cornerRadius, cornerRadius));

            g2.setColor(Theme.TEXT_WHITE);
        } else if (isHovered) {
            g2.setColor(new Color(255, 255, 255, 12));
            g2.fill(shape);
            g2.setColor(Theme.TEXT_PRIMARY);
        } else {
            g2.setColor(Theme.TEXT_SECONDARY);
        }
    }
}
