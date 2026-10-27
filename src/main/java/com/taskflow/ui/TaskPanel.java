package com.taskflow.ui;

import com.taskflow.model.Task;
import com.taskflow.model.TaskCategory;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import com.taskflow.service.TaskManager;
import com.taskflow.ui.components.CustomButton;
import com.taskflow.ui.components.EmptyStatePanel;
import com.taskflow.ui.components.ModernTextField;
import com.taskflow.ui.components.Theme;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Main task viewing and management panel supporting filters (All, Today, Upcoming, Completed),
 * live search, category/priority dropdowns, and sorting.
 */
public class TaskPanel extends JPanel implements TaskManager.TaskChangeListener {

    public enum FilterMode {
        ALL("All Tasks", "Everything in your task backlog"),
        TODAY("Today's Tasks", "Tasks scheduled for today"),
        UPCOMING("Upcoming Tasks", "Tasks scheduled for upcoming days"),
        COMPLETED("Completed Tasks", "Archive of finished tasks");

        private final String title;
        private final String subtitle;

        FilterMode(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
        }

        public String getTitle() { return title; }
        public String getSubtitle() { return subtitle; }
    }

    private final TaskManager taskManager;
    private final Runnable onNewTaskRequested;
    private final Consumer<Task> onEditRequested;

    private FilterMode currentFilter = FilterMode.ALL;
    private String searchQuery = "";
    private String selectedCategory = "All Categories";
    private String selectedPriority = "All Priorities";
    private String selectedSort = "Due Date (Earliest)";

    // UI elements
    private JLabel viewTitleLabel;
    private JLabel viewSubtitleLabel;
    private ModernTextField searchField;
    private JComboBox<String> categoryFilterCombo;
    private JComboBox<String> priorityFilterCombo;
    private JComboBox<String> sortCombo;
    private JPanel taskListContainer;

    public TaskPanel(TaskManager taskManager, Runnable onNewTaskRequested, Consumer<Task> onEditRequested) {
        this.taskManager = taskManager;
        this.onNewTaskRequested = onNewTaskRequested;
        this.onEditRequested = onEditRequested;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(Theme.emptyPadding(24, 28, 24, 28));

        taskManager.addChangeListener(this);

        initComponents();
        refreshTaskList();
    }

    private void initComponents() {
        // --- Top Header & Toolbar ---
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);

        // Header Title Row: Title & Subtitle + "+ New Task"
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JPanel titlesStacked = new JPanel();
        titlesStacked.setLayout(new BoxLayout(titlesStacked, BoxLayout.Y_AXIS));
        titlesStacked.setOpaque(false);

        viewTitleLabel = new JLabel(currentFilter.getTitle());
        viewTitleLabel.setFont(Theme.fontHero());
        viewTitleLabel.setForeground(Theme.TEXT_PRIMARY);
        titlesStacked.add(viewTitleLabel);

        titlesStacked.add(Box.createVerticalStrut(4));

        viewSubtitleLabel = new JLabel(currentFilter.getSubtitle());
        viewSubtitleLabel.setFont(Theme.fontSmall());
        viewSubtitleLabel.setForeground(Theme.TEXT_SECONDARY);
        titlesStacked.add(viewSubtitleLabel);

        titleRow.add(titlesStacked, BorderLayout.WEST);

        CustomButton newBtn = new CustomButton("+ New Task", CustomButton.ButtonStyle.PRIMARY);
        newBtn.setPreferredSize(new Dimension(135, 40));
        newBtn.addActionListener(e -> {
            if (onNewTaskRequested != null) onNewTaskRequested.run();
        });
        titleRow.add(newBtn, BorderLayout.EAST);

        topSection.add(titleRow);
        topSection.add(Box.createVerticalStrut(18));

        // Filters Toolbar Row: Search Field + Category Filter + Priority Filter + Sort
        JPanel toolbar = new JPanel(new BorderLayout(14, 0));
        toolbar.setOpaque(false);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Search Field on left
        searchField = new ModernTextField("Search tasks by title, description, or category...", 20);
        searchField.setPreferredSize(new Dimension(320, 38));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updateSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updateSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updateSearch(); }
            private void updateSearch() {
                searchQuery = searchField.getText().trim();
                refreshTaskList();
            }
        });
        toolbar.add(searchField, BorderLayout.CENTER);

        // Filter Dropdowns on right
        JPanel dropdowns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        dropdowns.setOpaque(false);

        // Category Filter
        categoryFilterCombo = new JComboBox<>(new String[]{
                "All Categories", "Study", "Work", "Project", "Personal", "Other"
        });
        styleFilterCombo(categoryFilterCombo, 130);
        categoryFilterCombo.addActionListener(e -> {
            selectedCategory = (String) categoryFilterCombo.getSelectedItem();
            refreshTaskList();
        });
        dropdowns.add(categoryFilterCombo);

        // Priority Filter
        priorityFilterCombo = new JComboBox<>(new String[]{
                "All Priorities", "High Priority", "Medium Priority", "Low Priority"
        });
        styleFilterCombo(priorityFilterCombo, 130);
        priorityFilterCombo.addActionListener(e -> {
            selectedPriority = (String) priorityFilterCombo.getSelectedItem();
            refreshTaskList();
        });
        dropdowns.add(priorityFilterCombo);

        // Sort Combo
        sortCombo = new JComboBox<>(new String[]{
                "Due Date (Earliest)", "Due Date (Latest)", "Priority (High to Low)", "Title (A-Z)"
        });
        styleFilterCombo(sortCombo, 160);
        sortCombo.addActionListener(e -> {
            selectedSort = (String) sortCombo.getSelectedItem();
            refreshTaskList();
        });
        dropdowns.add(sortCombo);

        toolbar.add(dropdowns, BorderLayout.EAST);

        topSection.add(toolbar);
        topSection.add(Box.createVerticalStrut(18));

        add(topSection, BorderLayout.NORTH);

        // --- Center Task List (Scrollable) ---
        taskListContainer = new JPanel();
        taskListContainer.setLayout(new BoxLayout(taskListContainer, BoxLayout.Y_AXIS));
        taskListContainer.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(taskListContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleFilterCombo(JComboBox<String> combo, int width) {
        combo.setFont(Theme.fontSmallBold());
        combo.setBackground(Theme.BG_INPUT);
        combo.setForeground(Theme.TEXT_SECONDARY);
        combo.setPreferredSize(new Dimension(width, 38));
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

    public void setFilterMode(FilterMode mode) {
        this.currentFilter = mode;
        if (viewTitleLabel != null) {
            viewTitleLabel.setText(mode.getTitle());
            viewSubtitleLabel.setText(mode.getSubtitle());
        }
        refreshTaskList();
    }

    public void applySearchQuery(String query) {
        this.searchQuery = query != null ? query.trim() : "";
        if (searchField != null) {
            searchField.setText(searchQuery);
        }
        refreshTaskList();
    }

    public void refreshTaskList() {
        if (taskListContainer == null) return;

        taskListContainer.removeAll();

        // 1. Base list according to filter mode
        List<Task> list = switch (currentFilter) {
            case ALL -> taskManager.getAllTasks();
            case TODAY -> taskManager.getTodayTasks();
            case UPCOMING -> taskManager.getUpcomingTasks();
            case COMPLETED -> taskManager.getCompletedTasks();
        };

        // 2. Filter by search query
        if (!searchQuery.isEmpty()) {
            String q = searchQuery.toLowerCase();
            list = list.stream()
                    .filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(q)) ||
                            (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)) ||
                            (t.getCategory().getDisplayName().toLowerCase().contains(q)))
                    .toList();
        }

        // 3. Filter by Category dropdown
        if (selectedCategory != null && !"All Categories".equals(selectedCategory)) {
            list = list.stream()
                    .filter(t -> t.getCategory().getDisplayName().equalsIgnoreCase(selectedCategory))
                    .toList();
        }

        // 4. Filter by Priority dropdown
        if (selectedPriority != null && !"All Priorities".equals(selectedPriority)) {
            String pStr = selectedPriority.replace(" Priority", "").trim();
            list = list.stream()
                    .filter(t -> t.getPriority().getDisplayName().equalsIgnoreCase(pStr))
                    .toList();
        }

        // 5. Sort list
        List<Task> sortedList = new ArrayList<>(list);
        if ("Due Date (Earliest)".equals(selectedSort)) {
            sortedList.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder())));
        } else if ("Due Date (Latest)".equals(selectedSort)) {
            sortedList.sort(Comparator.comparing(Task::getDueDate, Comparator.nullsFirst(Comparator.reverseOrder())));
        } else if ("Priority (High to Low)".equals(selectedSort)) {
            sortedList.sort((a, b) -> {
                int pA = a.getPriority() == TaskPriority.HIGH ? 3 : (a.getPriority() == TaskPriority.MEDIUM ? 2 : 1);
                int pB = b.getPriority() == TaskPriority.HIGH ? 3 : (b.getPriority() == TaskPriority.MEDIUM ? 2 : 1);
                return Integer.compare(pB, pA);
            });
        } else if ("Title (A-Z)".equals(selectedSort)) {
            sortedList.sort(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER));
        }

        // Update title with count
        if (viewTitleLabel != null) {
            viewTitleLabel.setText(currentFilter.getTitle() + " (" + sortedList.size() + ")");
        }

        // 6. Render Task Cards or Empty State
        if (sortedList.isEmpty()) {
            String emptyTitle = !searchQuery.isEmpty() ? "No matching tasks found" : "No tasks in this view";
            String emptyDesc = !searchQuery.isEmpty() ?
                    "Try checking your spelling or clearing search filters." :
                    "Keep up the great work or plan a new task to get started.";

            EmptyStatePanel emptyState = new EmptyStatePanel(emptyTitle, emptyDesc, onNewTaskRequested);
            taskListContainer.add(emptyState);
        } else {
            for (Task t : sortedList) {
                TaskCard card = new TaskCard(
                        t,
                        task -> taskManager.toggleTaskStatus(task.getId()),
                        onEditRequested,
                        task -> taskManager.deleteTask(task.getId())
                );
                taskListContainer.add(card);
                taskListContainer.add(Box.createVerticalStrut(10));
            }
        }

        taskListContainer.revalidate();
        taskListContainer.repaint();
    }

    @Override
    public void onTasksChanged() {
        SwingUtilities.invokeLater(this::refreshTaskList);
    }
}
