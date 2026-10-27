package com.taskflow.ui;

import com.taskflow.model.Task;
import com.taskflow.model.TaskStatus;
import com.taskflow.ui.components.Badge;
import com.taskflow.ui.components.CustomButton;
import com.taskflow.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * Modern card component representing an individual task with status toggle,
 * metadata badges, and edit/delete actions.
 */
public class TaskCard extends JPanel {

    private final Task task;
    private final Consumer<Task> onToggleComplete;
    private final Consumer<Task> onEdit;
    private final Consumer<Task> onDelete;

    private boolean isHovered = false;

    public TaskCard(Task task, Consumer<Task> onToggleComplete, Consumer<Task> onEdit, Consumer<Task> onDelete) {
        this.task = task;
        this.onToggleComplete = onToggleComplete;
        this.onEdit = onEdit;
        this.onDelete = onDelete;

        setLayout(new BorderLayout(14, 0));
        setOpaque(false);
        setBorder(Theme.emptyPadding(14, 18, 14, 18));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        setPreferredSize(new Dimension(800, 95));

        initComponents();

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
        });
    }

    private void initComponents() {
        // Left: Custom Circle Checkbox
        JPanel checkPanel = new JPanel(new GridBagLayout());
        checkPanel.setOpaque(false);

        JButton checkButton = new JButton() {
            private boolean cbHover = false;
            {
                setPreferredSize(new Dimension(28, 28));
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setToolTipText(task.getStatus() == TaskStatus.COMPLETED ? "Mark as Pending" : "Mark as Completed");

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) { cbHover = true; repaint(); }
                    @Override
                    public void mouseExited(MouseEvent e) { cbHover = false; repaint(); }
                });

                addActionListener(e -> {
                    if (onToggleComplete != null) {
                        onToggleComplete.accept(task);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);

                int size = 22;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                boolean isCompleted = task.getStatus() == TaskStatus.COMPLETED;

                if (isCompleted) {
                    // Filled Emerald circle
                    g2.setColor(Theme.STATUS_GREEN);
                    g2.fillOval(x, y, size, size);

                    // Checkmark
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 6, y + 11, x + 10, y + 15);
                    g2.drawLine(x + 10, y + 15, x + 16, y + 7);
                } else {
                    // Empty circle with border
                    g2.setColor(cbHover ? Theme.ACCENT_CYAN : Theme.BORDER_LIGHT);
                    g2.setStroke(new BasicStroke(1.8f));
                    g2.drawOval(x, y, size, size);

                    if (cbHover) {
                        g2.setColor(new Color(Theme.ACCENT_CYAN.getRed(), Theme.ACCENT_CYAN.getGreen(), Theme.ACCENT_CYAN.getBlue(), 40));
                        g2.fillOval(x + 3, y + 3, size - 6, size - 6);
                    }
                }

                g2.dispose();
            }
        };

        checkPanel.add(checkButton);
        add(checkPanel, BorderLayout.WEST);

        // Center: Task Information
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Top line: Task ID + Title + Overdue warning
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);
        titleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Task ID label
        JLabel idLabel = new JLabel(task.getId());
        idLabel.setFont(Theme.fontSmallBold());
        idLabel.setForeground(Theme.TEXT_MUTED);
        titleRow.add(idLabel);

        // Title label
        JLabel titleLabel = new JLabel(task.getTitle());
        boolean isCompleted = task.getStatus() == TaskStatus.COMPLETED;
        if (isCompleted) {
            // Apply strikethrough and muted color
            Font font = Theme.fontHeader();
            font = font.deriveFont(Collections.singletonMap(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));
            titleLabel.setFont(font);
            titleLabel.setForeground(Theme.TEXT_MUTED);
        } else {
            titleLabel.setFont(Theme.fontHeader());
            titleLabel.setForeground(Theme.TEXT_PRIMARY);
        }
        titleRow.add(titleLabel);

        // Overdue alert badge if overdue and pending
        if (task.isOverdue()) {
            Badge overdueBadge = new Badge("OVERDUE", Theme.STATUS_RED, false);
            titleRow.add(overdueBadge);
        }

        centerPanel.add(titleRow);

        // Middle line: Description (if not empty)
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            centerPanel.add(Box.createVerticalStrut(3));
            JLabel descLabel = new JLabel(task.getDescription());
            descLabel.setFont(Theme.fontBody());
            descLabel.setForeground(Theme.TEXT_SECONDARY);
            descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerPanel.add(descLabel);
        }

        centerPanel.add(Box.createVerticalStrut(6));

        // Bottom line: Metadata Badges
        JPanel badgesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgesRow.setOpaque(false);
        badgesRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Category Badge
        Badge catBadge = new Badge(task.getCategory().getDisplayName(), task.getCategory().getColor());
        badgesRow.add(catBadge);

        // Priority Badge
        Badge priorityBadge = new Badge(task.getPriority().getDisplayName() + " Priority", task.getPriority().getColor());
        badgesRow.add(priorityBadge);

        // Due Date Badge
        if (task.getDueDate() != null) {
            String dateText = formatDueDate(task.getDueDate());
            Color dateColor = task.isOverdue() ? Theme.STATUS_RED :
                    (task.isDueToday() ? Theme.STATUS_AMBER : Theme.TEXT_SECONDARY);
            Badge dateBadge = new Badge("Due " + dateText, dateColor, false);
            badgesRow.add(dateBadge);
        }

        // Status Badge
        Badge statusBadge = new Badge(task.getStatus().getDisplayName(), task.getStatus().getColor(), true);
        badgesRow.add(statusBadge);

        centerPanel.add(badgesRow);

        add(centerPanel, BorderLayout.CENTER);

        // Right: Action Buttons (Edit & Delete)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actionsPanel.setOpaque(false);

        // Quick Toggle button
        CustomButton toggleBtn = new CustomButton(
                task.getStatus() == TaskStatus.COMPLETED ? "Reopen" : "Complete",
                task.getStatus() == TaskStatus.COMPLETED ? CustomButton.ButtonStyle.SECONDARY : CustomButton.ButtonStyle.SUCCESS
        );
        toggleBtn.setMargin(new Insets(6, 12, 6, 12));
        toggleBtn.setFont(Theme.fontSmallBold());
        toggleBtn.addActionListener(e -> {
            if (onToggleComplete != null) onToggleComplete.accept(task);
        });
        actionsPanel.add(toggleBtn);

        // Edit button
        CustomButton editBtn = new CustomButton("Edit", CustomButton.ButtonStyle.SECONDARY);
        editBtn.setMargin(new Insets(6, 12, 6, 12));
        editBtn.setFont(Theme.fontSmallBold());
        editBtn.addActionListener(e -> {
            if (onEdit != null) onEdit.accept(task);
        });
        actionsPanel.add(editBtn);

        // Delete button
        CustomButton deleteBtn = new CustomButton("Delete", CustomButton.ButtonStyle.DANGER);
        deleteBtn.setMargin(new Insets(6, 12, 6, 12));
        deleteBtn.setFont(Theme.fontSmallBold());
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete task:\n\"" + task.getTitle() + "\"?",
                    "Confirm Task Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION && onDelete != null) {
                onDelete.accept(task);
            }
        });
        actionsPanel.add(deleteBtn);

        JPanel rightAlign = new JPanel(new GridBagLayout());
        rightAlign.setOpaque(false);
        rightAlign.add(actionsPanel);
        add(rightAlign, BorderLayout.EAST);
    }

    private String formatDueDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isEqual(today)) {
            return "Today";
        } else if (date.isEqual(today.plusDays(1))) {
            return "Tomorrow";
        } else if (date.isEqual(today.minusDays(1))) {
            return "Yesterday";
        } else {
            return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.applyAntialiasing(g2);

        int w = getWidth();
        int h = getHeight();
        int arc = 14;

        // Card Surface
        Color bg = isHovered ? Theme.BG_CARD_HOVER : Theme.BG_CARD;
        g2.setColor(bg);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

        // Border: highlight on hover or subtle border
        Color border = isHovered ? Theme.BORDER_LIGHT : Theme.BORDER_SUBTLE;
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.0f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

        // Left accent indicator if high priority
        if (task.getPriority() == com.taskflow.model.TaskPriority.HIGH && task.getStatus() == TaskStatus.PENDING) {
            g2.setColor(Theme.STATUS_RED);
            g2.fillRoundRect(2, 8, 4, h - 16, 2, 2);
        } else if (task.getStatus() == TaskStatus.COMPLETED) {
            g2.setColor(Theme.STATUS_GREEN);
            g2.fillRoundRect(2, 8, 4, h - 16, 2, 2);
        }

        g2.dispose();
        super.paintComponent(g);
    }
}
