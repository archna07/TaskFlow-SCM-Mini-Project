package com.taskflow.ui;

import com.taskflow.model.Task;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.service.TaskManager;
import com.taskflow.ui.components.CustomButton;
import com.taskflow.ui.components.ModernProgressBar;
import com.taskflow.ui.components.StatCard;
import com.taskflow.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Modern productivity dashboard with Midnight Aurora statistics cards,
 * progress analytics, motivational quotes, and high-priority task alerts.
 */
public class DashboardPanel extends JPanel implements TaskManager.TaskChangeListener {

    private final TaskManager taskManager;
    private final Consumer<String> navigationHandler;
    private final Runnable onNewTaskRequested;
    private final Consumer<Task> onEditRequested;

    // Stat Cards
    private StatCard totalCard;
    private StatCard completedCard;
    private StatCard pendingCard;
    private StatCard highPriorityCard;

    // Progress Section
    private ModernProgressBar progressBar;
    private JLabel progressPercentLabel;
    private JLabel progressStatusLabel;
    private JLabel progressRatioLabel;

    // Recent / Urgent tasks container
    private JPanel urgentTasksList;

    public DashboardPanel(TaskManager taskManager, Consumer<String> navigationHandler, Runnable onNewTaskRequested, Consumer<Task> onEditRequested) {
        this.taskManager = taskManager;
        this.navigationHandler = navigationHandler;
        this.onNewTaskRequested = onNewTaskRequested;
        this.onEditRequested = onEditRequested;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(Theme.emptyPadding(24, 28, 24, 28));

        taskManager.addChangeListener(this);

        initComponents();
        refreshData();
    }

    private void initComponents() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        // 1. Welcome Banner
        scrollContent.add(createWelcomeBanner());
        scrollContent.add(Box.createVerticalStrut(24));

        // 2. 4 Stat Cards
        scrollContent.add(createStatCardsRow());
        scrollContent.add(Box.createVerticalStrut(24));

        // 3. Progress Section Card
        scrollContent.add(createProgressSection());
        scrollContent.add(Box.createVerticalStrut(24));

        // 4. Urgent / High Priority Tasks Section
        scrollContent.add(createUrgentTasksSection());

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createWelcomeBanner() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);
                int w = getWidth();
                int h = getHeight();
                int arc = 18;

                // Gradient background from dark indigo to midnight purple
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0x1B, 0x22, 0x42),
                        w, h, new Color(0x23, 0x1A, 0x44)
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

                // Border
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

                // Corner decorative aurora glow
                g2.setColor(new Color(0x8B, 0x5C, 0xF6, 35));
                g2.fillOval(w - 180, -60, 240, 240);
                g2.setColor(new Color(0x43, 0xD9, 0xFF, 20));
                g2.fillOval(w - 90, 20, 160, 160);

                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(Theme.emptyPadding(22, 26, 22, 26));

        // Left info
        JPanel leftInfo = new JPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        leftInfo.setOpaque(false);

        JLabel greetingLabel = new JLabel("Let's make today productive.");
        greetingLabel.setFont(Theme.fontHero());
        greetingLabel.setForeground(Theme.TEXT_PRIMARY);
        leftInfo.add(greetingLabel);

        leftInfo.add(Box.createVerticalStrut(6));

        String dateFormatted = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy"));
        JLabel dateLabel = new JLabel("📅 " + dateFormatted + "  •  Midnight Aurora Workspace");
        dateLabel.setFont(Theme.fontBody());
        dateLabel.setForeground(Theme.ACCENT_CYAN);
        leftInfo.add(dateLabel);

        banner.add(leftInfo, BorderLayout.WEST);

        // Right Action: Quick "+ New Task"
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);

        CustomButton newBtn = new CustomButton("+ Create Task", CustomButton.ButtonStyle.PRIMARY);
        newBtn.setPreferredSize(new Dimension(145, 42));
        newBtn.addActionListener(e -> {
            if (onNewTaskRequested != null) onNewTaskRequested.run();
        });
        rightActions.add(newBtn);

        banner.add(rightActions, BorderLayout.EAST);

        return banner;
    }

    private JPanel createStatCardsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        totalCard = new StatCard("Total Tasks", 0, "All items in backlog", Theme.ACCENT_VIOLET, "📋");
        completedCard = new StatCard("Completed Tasks", 0, "Finished successfully", Theme.STATUS_GREEN, "✓");
        pendingCard = new StatCard("Pending Tasks", 0, "In progress & queued", Theme.STATUS_AMBER, "⏳");
        highPriorityCard = new StatCard("High Priority", 0, "Urgent focus needed", Theme.STATUS_RED, "⚡");

        row.add(totalCard);
        row.add(completedCard);
        row.add(pendingCard);
        row.add(highPriorityCard);

        return row;
    }

    private JPanel createProgressSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);
                int w = getWidth();
                int h = getHeight();
                int arc = 16;

                g2.setColor(Theme.BG_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

                g2.setColor(Theme.BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1.0f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(Theme.emptyPadding(20, 24, 20, 24));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Header line: "Task Progress" & Percentage label
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Task Completion Progress");
        title.setFont(Theme.fontHeader());
        title.setForeground(Theme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        progressPercentLabel = new JLabel("0.0%");
        progressPercentLabel.setFont(Theme.fontHeader());
        progressPercentLabel.setForeground(Theme.ACCENT_CYAN);
        header.add(progressPercentLabel, BorderLayout.EAST);

        panel.add(header, BorderLayout.NORTH);

        // Center: Progress Bar
        progressBar = new ModernProgressBar();
        progressBar.setPreferredSize(new Dimension(200, 16));
        panel.add(progressBar, BorderLayout.CENTER);

        // Footer: Status Message & Ratio
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        progressStatusLabel = new JLabel("Initializing tasks...");
        progressStatusLabel.setFont(Theme.fontBody());
        progressStatusLabel.setForeground(Theme.TEXT_SECONDARY);
        footer.add(progressStatusLabel, BorderLayout.WEST);

        progressRatioLabel = new JLabel("0 of 0 completed");
        progressRatioLabel.setFont(Theme.fontSmall());
        progressRatioLabel.setForeground(Theme.TEXT_MUTED);
        footer.add(progressRatioLabel, BorderLayout.EAST);

        panel.add(footer, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createUrgentTasksSection() {
        JPanel section = new JPanel(new BorderLayout(0, 12));
        section.setOpaque(false);

        // Section header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("High Priority & Active Tasks");
        title.setFont(Theme.fontHeader());
        title.setForeground(Theme.TEXT_PRIMARY);
        header.add(title, BorderLayout.WEST);

        CustomButton viewAllBtn = new CustomButton("View All Tasks →", CustomButton.ButtonStyle.SECONDARY);
        viewAllBtn.setMargin(new Insets(6, 12, 6, 12));
        viewAllBtn.setFont(Theme.fontSmallBold());
        viewAllBtn.addActionListener(e -> {
            if (navigationHandler != null) navigationHandler.accept("ALL");
        });
        header.add(viewAllBtn, BorderLayout.EAST);

        section.add(header, BorderLayout.NORTH);

        // Urgent tasks card list
        urgentTasksList = new JPanel();
        urgentTasksList.setLayout(new BoxLayout(urgentTasksList, BoxLayout.Y_AXIS));
        urgentTasksList.setOpaque(false);
        section.add(urgentTasksList, BorderLayout.CENTER);

        return section;
    }

    public void refreshData() {
        // Update 4 stat cards
        int total = taskManager.getTotalCount();
        int completed = taskManager.getCompletedCount();
        int pending = taskManager.getPendingCount();
        int highPri = taskManager.getHighPriorityCount();
        double percent = taskManager.getCompletionPercentage();

        if (totalCard != null) totalCard.setValue(total);
        if (completedCard != null) completedCard.setValue(completed);
        if (pendingCard != null) pendingCard.setValue(pending);
        if (highPriorityCard != null) highPriorityCard.setValue(highPri);

        // Update progress bar
        if (progressBar != null) {
            progressBar.setProgressPercentage(percent);
            progressPercentLabel.setText(String.format("%.1f%%", percent));
            progressRatioLabel.setText(completed + " of " + total + " tasks completed");

            // Dynamic encouraging status message
            if (total == 0) {
                progressStatusLabel.setText("No tasks created yet. Plan your goals today!");
            } else if (percent == 100.0) {
                progressStatusLabel.setText("🎉 Outstanding! All tasks are completed. You're on fire!");
            } else if (percent >= 75.0) {
                progressStatusLabel.setText("Almost there! Finish the final few tasks strong.");
            } else if (percent >= 50.0) {
                progressStatusLabel.setText("Halfway milestone passed! Outstanding focus.");
            } else if (percent > 0.0) {
                progressStatusLabel.setText("Good start! Keep up the momentum.");
            } else {
                progressStatusLabel.setText("Ready to conquer today? Begin with your highest priority task.");
            }
        }

        // Populate urgent / high-priority pending tasks preview
        if (urgentTasksList != null) {
            urgentTasksList.removeAll();
            List<Task> urgent = taskManager.getAllTasks().stream()
                    .filter(t -> t.getStatus() == TaskStatus.PENDING)
                    .sorted((a, b) -> {
                        // High priority first
                        int pA = a.getPriority() == TaskPriority.HIGH ? 2 : (a.getPriority() == TaskPriority.MEDIUM ? 1 : 0);
                        int pB = b.getPriority() == TaskPriority.HIGH ? 2 : (b.getPriority() == TaskPriority.MEDIUM ? 1 : 0);
                        return Integer.compare(pB, pA);
                    })
                    .limit(4)
                    .toList();

            if (urgent.isEmpty()) {
                JPanel empty = new JPanel(new FlowLayout(FlowLayout.CENTER));
                empty.setOpaque(false);
                JLabel lbl = new JLabel("✨ All pending tasks are cleared! Great job.");
                lbl.setFont(Theme.fontBody());
                lbl.setForeground(Theme.STATUS_GREEN);
                empty.add(lbl);
                urgentTasksList.add(empty);
            } else {
                for (Task t : urgent) {
                    TaskCard card = new TaskCard(
                            t,
                            task -> taskManager.toggleTaskStatus(task.getId()),
                            onEditRequested,
                            task -> taskManager.deleteTask(task.getId())
                    );
                    urgentTasksList.add(card);
                    urgentTasksList.add(Box.createVerticalStrut(10));
                }
            }
            urgentTasksList.revalidate();
            urgentTasksList.repaint();
        }
    }

    @Override
    public void onTasksChanged() {
        SwingUtilities.invokeLater(this::refreshData);
    }
}
