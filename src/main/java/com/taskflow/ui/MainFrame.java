package com.taskflow.ui;

import com.taskflow.model.Task;
import com.taskflow.service.TaskManager;
import com.taskflow.ui.components.CustomButton;
import com.taskflow.ui.components.ModernTextField;
import com.taskflow.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Main application window for TaskFlow.
 * Hosts the Left Sidebar, Top Header, and CardLayout View Switcher (Dashboard & Task views).
 */
public class MainFrame extends JFrame {

    private final TaskManager taskManager;

    private CardLayout cardLayout;
    private JPanel viewsContainer;

    private DashboardPanel dashboardPanel;
    private TaskPanel taskPanel;

    // Sidebar navigation buttons
    private final Map<String, CustomButton> navButtons = new HashMap<>();
    private String currentNavKey = "DASHBOARD";

    // Top Header Search Field
    private ModernTextField globalSearchField;

    public MainFrame(TaskManager taskManager) {
        super("TaskFlow — Smart Task Management System");
        this.taskManager = taskManager;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1240, 820);
        setMinimumSize(new Dimension(1050, 700));
        setLocationRelativeTo(null);

        // Dark background
        getContentPane().setBackground(Theme.BG_MAIN);
        setLayout(new BorderLayout());

        initComponents();
    }

    private void initComponents() {
        // 1. Left Sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // 2. Right Workspace Panel (Top Header + Center Content Views)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // Top Header
        JPanel topHeader = createTopHeader();
        rightPanel.add(topHeader, BorderLayout.NORTH);

        // Center Views Container with CardLayout
        cardLayout = new CardLayout();
        viewsContainer = new JPanel(cardLayout);
        viewsContainer.setOpaque(false);

        // Views
        dashboardPanel = new DashboardPanel(
                taskManager,
                this::navigateToView,
                this::openNewTaskDialog,
                this::openEditTaskDialog
        );

        taskPanel = new TaskPanel(
                taskManager,
                this::openNewTaskDialog,
                this::openEditTaskDialog
        );

        viewsContainer.add(dashboardPanel, "DASHBOARD");
        viewsContainer.add(taskPanel, "TASKS");

        rightPanel.add(viewsContainer, BorderLayout.CENTER);

        add(rightPanel, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);
                g2.setColor(Theme.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Right border line
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setPreferredSize(new Dimension(240, Integer.MAX_VALUE));
        sidebar.setBorder(Theme.emptyPadding(24, 18, 20, 18));

        // Top: Logo + Brand
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // Brand Logo Row
        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Custom drawn Aurora gradient logo icon
        JPanel logoIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);
                int size = 36;
                // Aurora gradient rounded rect
                GradientPaint gp = new GradientPaint(
                        0, 0, Theme.ACCENT_VIOLET,
                        size, size, Theme.ACCENT_CYAN
                );
                g2.setPaint(gp);
                g2.fill(new RoundRectangle2D.Float(0, 0, size, size, 10, 10));

                // Glowing check / lightning icon inside
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(9, 18, 15, 24);
                g2.drawLine(15, 24, 27, 12);

                g2.dispose();
            }
        };
        logoIcon.setOpaque(false);
        logoIcon.setPreferredSize(new Dimension(36, 36));
        logoRow.add(logoIcon);

        // Brand Title
        JPanel brandText = new JPanel();
        brandText.setLayout(new BoxLayout(brandText, BoxLayout.Y_AXIS));
        brandText.setOpaque(false);

        JLabel brandName = new JLabel("TaskFlow");
        brandName.setFont(Theme.fontHero());
        brandName.setForeground(Theme.TEXT_WHITE);
        brandText.add(brandName);

        JLabel brandTagline = new JLabel("Smart Task System");
        brandTagline.setFont(Theme.fontSmall());
        brandTagline.setForeground(Theme.ACCENT_CYAN);
        brandText.add(brandTagline);

        logoRow.add(brandText);
        topSection.add(logoRow);

        topSection.add(Box.createVerticalStrut(24));

        // Prominent "+ New Task" Button
        CustomButton newBtn = new CustomButton("+ New Task", CustomButton.ButtonStyle.PRIMARY);
        newBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        newBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        newBtn.addActionListener(e -> openNewTaskDialog());
        topSection.add(newBtn);

        topSection.add(Box.createVerticalStrut(28));

        // Navigation Menu label
        JLabel navHeader = new JLabel("MENU");
        navHeader.setFont(Theme.fontSmallBold());
        navHeader.setForeground(Theme.TEXT_MUTED);
        navHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        navHeader.setBorder(Theme.emptyPadding(0, 8, 8, 0));
        topSection.add(navHeader);

        // Navigation Buttons
        addNavButton(topSection, "DASHBOARD", "📊   Dashboard", () -> navigateToView("DASHBOARD"));
        addNavButton(topSection, "ALL", "📋   All Tasks", () -> navigateToView("ALL"));
        addNavButton(topSection, "TODAY", "📅   Today", () -> navigateToView("TODAY"));
        addNavButton(topSection, "UPCOMING", "⏳   Upcoming", () -> navigateToView("UPCOMING"));
        addNavButton(topSection, "COMPLETED", "✅   Completed", () -> navigateToView("COMPLETED"));

        sidebar.add(topSection, BorderLayout.NORTH);

        // Bottom Footer: SCM Mini Project & Java 17
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setOpaque(false);
        footer.setBorder(Theme.emptyPadding(12, 6, 4, 6));

        // Divider line
        JPanel divider = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Theme.BORDER_SUBTLE);
                g.drawLine(0, 0, getWidth(), 0);
            }
        };
        divider.setOpaque(false);
        divider.setPreferredSize(new Dimension(180, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        footer.add(divider);

        footer.add(Box.createVerticalStrut(12));

        JLabel footerTitle = new JLabel("SCM Mini Project | Java 17");
        footerTitle.setFont(Theme.fontSmallBold());
        footerTitle.setForeground(Theme.TEXT_SECONDARY);
        footer.add(footerTitle);

        JLabel footerSub = new JLabel("Maven • JUnit 5 • Jenkins CI");
        footerSub.setFont(Theme.fontSmall());
        footerSub.setForeground(Theme.TEXT_MUTED);
        footer.add(footerSub);

        sidebar.add(footer, BorderLayout.SOUTH);

        // Set initial selected state
        updateNavSelection("DASHBOARD");

        return sidebar;
    }

    private void addNavButton(JPanel container, String key, String text, Runnable action) {
        CustomButton btn = new CustomButton(text, CustomButton.ButtonStyle.NAV_ITEM);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(200, 40));
        btn.addActionListener(e -> action.run());

        container.add(btn);
        container.add(Box.createVerticalStrut(4));
        navButtons.put(key, btn);
    }

    private void updateNavSelection(String key) {
        this.currentNavKey = key;
        for (Map.Entry<String, CustomButton> entry : navButtons.entrySet()) {
            entry.getValue().setSelectedNav(entry.getKey().equals(key));
        }
    }

    private JPanel createTopHeader() {
        JPanel header = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);
                g2.setColor(Theme.BG_HEADER);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Bottom border
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(Integer.MAX_VALUE, 65));
        header.setBorder(Theme.emptyPadding(12, 28, 12, 28));

        // Left: Greeting & Current Date
        JPanel leftHeader = new JPanel();
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        leftHeader.setOpaque(false);

        JLabel titleLbl = new JLabel("Let's make today productive.");
        titleLbl.setFont(Theme.fontHeader());
        titleLbl.setForeground(Theme.TEXT_PRIMARY);
        leftHeader.add(titleLbl);

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy"));
        JLabel dateLbl = new JLabel(dateStr);
        dateLbl.setFont(Theme.fontSmall());
        dateLbl.setForeground(Theme.TEXT_SECONDARY);
        leftHeader.add(dateLbl);

        header.add(leftHeader, BorderLayout.WEST);

        // Center / Right: Global Search Field + "+ New Task" + User Avatar Profile
        JPanel rightHeader = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightHeader.setOpaque(false);

        globalSearchField = new ModernTextField("Search tasks...", 16);
        globalSearchField.setPreferredSize(new Dimension(240, 38));
        globalSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String q = globalSearchField.getText().trim();
                navigateToView("ALL");
                taskPanel.applySearchQuery(q);
            }
        });
        rightHeader.add(globalSearchField);

        // Quick "+ New Task" header button
        CustomButton headerNewBtn = new CustomButton("+ New Task", CustomButton.ButtonStyle.PRIMARY);
        headerNewBtn.setPreferredSize(new Dimension(120, 38));
        headerNewBtn.addActionListener(e -> openNewTaskDialog());
        rightHeader.add(headerNewBtn);

        // Profile-style circular icon with initials "TF"
        JPanel avatarIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);
                int size = 36;
                // Circular gradient
                GradientPaint gp = new GradientPaint(
                        0, 0, Theme.ACCENT_VIOLET,
                        size, size, Theme.ACCENT_CYAN
                );
                g2.setPaint(gp);
                g2.fillOval(0, 0, size, size);

                // Initials "TF"
                g2.setFont(Theme.fontBodyBold());
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (size - fm.stringWidth("TF")) / 2;
                int ty = (size + fm.getAscent()) / 2 - 2;
                g2.drawString("TF", tx, ty);

                g2.dispose();
            }
        };
        avatarIcon.setOpaque(false);
        avatarIcon.setPreferredSize(new Dimension(36, 36));
        avatarIcon.setToolTipText("TaskFlow User Profile (TF)");
        rightHeader.add(avatarIcon);

        header.add(rightHeader, BorderLayout.EAST);

        return header;
    }

    public void navigateToView(String key) {
        updateNavSelection(key);
        switch (key) {
            case "DASHBOARD" -> {
                cardLayout.show(viewsContainer, "DASHBOARD");
                dashboardPanel.refreshData();
            }
            case "ALL" -> {
                cardLayout.show(viewsContainer, "TASKS");
                taskPanel.setFilterMode(TaskPanel.FilterMode.ALL);
            }
            case "TODAY" -> {
                cardLayout.show(viewsContainer, "TASKS");
                taskPanel.setFilterMode(TaskPanel.FilterMode.TODAY);
            }
            case "UPCOMING" -> {
                cardLayout.show(viewsContainer, "TASKS");
                taskPanel.setFilterMode(TaskPanel.FilterMode.UPCOMING);
            }
            case "COMPLETED" -> {
                cardLayout.show(viewsContainer, "TASKS");
                taskPanel.setFilterMode(TaskPanel.FilterMode.COMPLETED);
            }
        }
    }

    private void openNewTaskDialog() {
        TaskFormDialog dialog = new TaskFormDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isSaved() && dialog.getResultTask() != null) {
            taskManager.addTask(dialog.getResultTask());
        }
    }

    private void openEditTaskDialog(Task task) {
        TaskFormDialog dialog = new TaskFormDialog(this, task);
        dialog.setVisible(true);

        if (dialog.isSaved() && dialog.getResultTask() != null) {
            taskManager.updateTask(dialog.getResultTask());
        }
    }
}
