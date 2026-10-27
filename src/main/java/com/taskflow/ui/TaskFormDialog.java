package com.taskflow.ui;

import com.taskflow.model.Task;
import com.taskflow.model.TaskCategory;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.ui.components.CustomButton;
import com.taskflow.ui.components.ModernTextField;
import com.taskflow.ui.components.Theme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Modern modal dialog for creating and editing tasks with input validation
 * and Midnight Aurora styling.
 */
public class TaskFormDialog extends JDialog {

    private final Task initialTask;
    private Task resultTask = null;
    private boolean saved = false;

    private ModernTextField titleField;
    private JTextArea descArea;
    private JComboBox<TaskCategory> categoryCombo;
    private JComboBox<TaskPriority> priorityCombo;
    private ModernTextField dueDateField;
    private JLabel errorLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TaskFormDialog(Frame parent, Task taskToEdit) {
        super(parent, taskToEdit == null ? "Create New Task" : "Edit Task", true);
        this.initialTask = taskToEdit;

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setSize(520, 580);
        setLocationRelativeTo(parent);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        // Main container panel with custom background and border
        JPanel contentPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.applyAntialiasing(g2);
                int w = getWidth();
                int h = getHeight();
                int arc = 20;

                // Panel surface
                g2.setColor(Theme.BG_PANEL);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));

                // Border
                g2.setColor(Theme.BORDER_LIGHT);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, arc, arc));

                // Header gradient accent
                GradientPaint headerGrad = new GradientPaint(
                        0, 0, Theme.ACCENT_VIOLET,
                        w, 0, Theme.ACCENT_CYAN
                );
                g2.setPaint(headerGrad);
                g2.fill(new RoundRectangle2D.Float(1, 1, w - 2, 4, 3, 3));

                g2.dispose();
            }
        };
        contentPanel.setLayout(new BorderLayout());
        contentPanel.setBorder(Theme.emptyPadding(24, 28, 24, 28));

        // --- Dialog Header ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleHeader = new JLabel(initialTask == null ? "Create New Task" : "Edit Task");
        titleHeader.setFont(Theme.fontHero());
        titleHeader.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(titleHeader, BorderLayout.WEST);

        JLabel subtitleHeader = new JLabel(initialTask == null ? "Fill in the details to plan your task" : "Modify existing task parameters");
        subtitleHeader.setFont(Theme.fontSmall());
        subtitleHeader.setForeground(Theme.TEXT_SECONDARY);

        JPanel headerStacked = new JPanel();
        headerStacked.setLayout(new BoxLayout(headerStacked, BoxLayout.Y_AXIS));
        headerStacked.setOpaque(false);
        headerStacked.add(titleHeader);
        headerStacked.add(Box.createVerticalStrut(4));
        headerStacked.add(subtitleHeader);
        headerPanel.add(headerStacked, BorderLayout.WEST);

        // Close 'X' button
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        closeBtn.setForeground(Theme.TEXT_MUTED);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        headerPanel.add(closeBtn, BorderLayout.EAST);

        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // --- Form Fields Body ---
        JPanel formBody = new JPanel();
        formBody.setLayout(new BoxLayout(formBody, BoxLayout.Y_AXIS));
        formBody.setOpaque(false);
        formBody.setBorder(Theme.emptyPadding(18, 0, 16, 0));

        // 1. Task Title
        formBody.add(createFieldLabel("Task Title *"));
        titleField = new ModernTextField("e.g. Complete SCM Assignment");
        titleField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        if (initialTask != null) {
            titleField.setText(initialTask.getTitle());
        }
        formBody.add(titleField);

        // Error message label (initially hidden)
        errorLabel = new JLabel(" ");
        errorLabel.setFont(Theme.fontSmallBold());
        errorLabel.setForeground(Theme.STATUS_RED);
        formBody.add(errorLabel);

        formBody.add(Box.createVerticalStrut(4));

        // 2. Description
        formBody.add(createFieldLabel("Description"));
        descArea = new JTextArea(3, 20);
        descArea.setFont(Theme.fontBody());
        descArea.setForeground(Theme.TEXT_PRIMARY);
        descArea.setCaretColor(Theme.ACCENT_CYAN);
        descArea.setBackground(Theme.BG_INPUT);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(Theme.emptyPadding(8, 12, 8, 12));
        if (initialTask != null) {
            descArea.setText(initialTask.getDescription());
        }

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setOpaque(false);
        descScroll.getViewport().setOpaque(false);
        descScroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        formBody.add(descScroll);

        formBody.add(Box.createVerticalStrut(12));

        // 3. Category & Priority (Two-column row)
        JPanel metaRow = new JPanel(new GridLayout(1, 2, 16, 0));
        metaRow.setOpaque(false);
        metaRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Category Col
        JPanel catCol = new JPanel();
        catCol.setLayout(new BoxLayout(catCol, BoxLayout.Y_AXIS));
        catCol.setOpaque(false);
        catCol.add(createFieldLabel("Category"));
        categoryCombo = new JComboBox<>(TaskCategory.values());
        styleComboBox(categoryCombo);
        if (initialTask != null) {
            categoryCombo.setSelectedItem(initialTask.getCategory());
        } else {
            categoryCombo.setSelectedItem(TaskCategory.PROJECT);
        }
        catCol.add(categoryCombo);
        metaRow.add(catCol);

        // Priority Col
        JPanel priCol = new JPanel();
        priCol.setLayout(new BoxLayout(priCol, BoxLayout.Y_AXIS));
        priCol.setOpaque(false);
        priCol.add(createFieldLabel("Priority"));
        priorityCombo = new JComboBox<>(TaskPriority.values());
        styleComboBox(priorityCombo);
        if (initialTask != null) {
            priorityCombo.setSelectedItem(initialTask.getPriority());
        } else {
            priorityCombo.setSelectedItem(TaskPriority.MEDIUM);
        }
        priCol.add(priorityCombo);
        metaRow.add(priCol);

        formBody.add(metaRow);

        formBody.add(Box.createVerticalStrut(12));

        // 4. Due Date
        formBody.add(createFieldLabel("Due Date (YYYY-MM-DD)"));
        LocalDate initialDate = initialTask != null && initialTask.getDueDate() != null ?
                initialTask.getDueDate() : LocalDate.now();

        dueDateField = new ModernTextField(initialDate.format(DATE_FORMATTER));
        dueDateField.setText(initialDate.format(DATE_FORMATTER));
        dueDateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        formBody.add(dueDateField);

        // Quick date preset pills
        formBody.add(Box.createVerticalStrut(6));
        JPanel datePresets = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        datePresets.setOpaque(false);
        datePresets.setAlignmentX(Component.LEFT_ALIGNMENT);

        addDatePresetButton(datePresets, "Today", LocalDate.now());
        addDatePresetButton(datePresets, "Tomorrow", LocalDate.now().plusDays(1));
        addDatePresetButton(datePresets, "+3 Days", LocalDate.now().plusDays(3));
        addDatePresetButton(datePresets, "+1 Week", LocalDate.now().plusWeeks(1));
        formBody.add(datePresets);

        contentPanel.add(formBody, BorderLayout.CENTER);

        // --- Dialog Footer Buttons ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footerPanel.setOpaque(false);

        CustomButton cancelBtn = new CustomButton("Cancel", CustomButton.ButtonStyle.SECONDARY);
        cancelBtn.setPreferredSize(new Dimension(100, 40));
        cancelBtn.addActionListener(e -> dispose());
        footerPanel.add(cancelBtn);

        CustomButton saveBtn = new CustomButton(initialTask == null ? "Create Task" : "Save Changes", CustomButton.ButtonStyle.PRIMARY);
        saveBtn.setPreferredSize(new Dimension(140, 40));
        saveBtn.addActionListener(e -> handleSave());
        footerPanel.add(saveBtn);

        contentPanel.add(footerPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);
    }

    private void addDatePresetButton(JPanel container, String label, LocalDate date) {
        JButton btn = new JButton(label);
        btn.setFont(Theme.fontSmall());
        btn.setForeground(Theme.TEXT_SECONDARY);
        btn.setBackground(Theme.BG_CARD);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
                Theme.emptyPadding(3, 8, 3, 8)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> dueDateField.setText(date.format(DATE_FORMATTER)));
        container.add(btn);
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.fontSubheader());
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setBorder(Theme.emptyPadding(0, 0, 4, 0));
        return label;
    }

    private <T> void styleComboBox(JComboBox<T> combo) {
        combo.setFont(Theme.fontBody());
        combo.setBackground(Theme.BG_INPUT);
        combo.setForeground(Theme.TEXT_PRIMARY);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        combo.setPreferredSize(new Dimension(180, 38));
        combo.setBorder(BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                lbl.setBackground(isSelected ? Theme.BG_CARD_HOVER : Theme.BG_INPUT);
                lbl.setForeground(isSelected ? Theme.ACCENT_CYAN : Theme.TEXT_PRIMARY);
                lbl.setBorder(Theme.emptyPadding(6, 10, 6, 10));
                return lbl;
            }
        });
    }

    private void handleSave() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setText("Task title is required.");
            titleField.requestFocus();
            return;
        }

        LocalDate dueDate;
        try {
            String dateStr = dueDateField.getText().trim();
            dueDate = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            errorLabel.setText("Invalid date format. Please use YYYY-MM-DD.");
            dueDateField.requestFocus();
            return;
        }

        String description = descArea.getText().trim();
        TaskCategory category = (TaskCategory) categoryCombo.getSelectedItem();
        TaskPriority priority = (TaskPriority) priorityCombo.getSelectedItem();

        if (initialTask == null) {
            resultTask = new Task(title, description, category, priority, dueDate);
        } else {
            resultTask = initialTask.copy();
            resultTask.setTitle(title);
            resultTask.setDescription(description);
            resultTask.setCategory(category);
            resultTask.setPriority(priority);
            resultTask.setDueDate(dueDate);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public Task getResultTask() {
        return resultTask;
    }
}
