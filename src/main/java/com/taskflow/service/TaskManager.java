package com.taskflow.service;

import com.taskflow.model.Task;
import com.taskflow.model.TaskCategory;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Service layer managing task storage, filtering, search, and statistical metrics.
 * Uses standard in-memory Java collections and supports change listeners for reactive UI updates.
 */
public class TaskManager {

    public interface TaskChangeListener {
        void onTasksChanged();
    }

    private final List<Task> tasks = new CopyOnWriteArrayList<>();
    private final AtomicInteger idCounter = new AtomicInteger(100);
    private final List<TaskChangeListener> listeners = new CopyOnWriteArrayList<>();

    public TaskManager() {
        // Empty constructor
    }

    /**
     * Registers a listener to be notified whenever tasks are added, updated, or deleted.
     */
    public void addChangeListener(TaskChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a previously registered change listener.
     */
    public void removeChangeListener(TaskChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all registered listeners of a change in task state.
     */
    private void notifyListeners() {
        for (TaskChangeListener listener : listeners) {
            try {
                listener.onTasksChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Generates a unique task identifier formatted like "TF-101".
     */
    public synchronized String generateNextId() {
        return "TF-" + idCounter.incrementAndGet();
    }

    /**
     * Adds a new task to memory. Validates title and assigns an ID if not present.
     */
    public Task addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }

        if (task.getId() == null || task.getId().trim().isEmpty()) {
            task.setId(generateNextId());
        }

        tasks.add(task);
        notifyListeners();
        return task;
    }

    /**
     * Retrieves all tasks currently stored in memory.
     */
    public List<Task> getAllTasks() {
        return Collections.unmodifiableList(new ArrayList<>(tasks));
    }

    /**
     * Finds a task by its unique identifier.
     */
    public Optional<Task> getTaskById(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return tasks.stream()
                .filter(t -> id.equalsIgnoreCase(t.getId()))
                .findFirst();
    }

    /**
     * Updates an existing task with new properties.
     */
    public boolean updateTask(Task updatedTask) {
        if (updatedTask == null || updatedTask.getId() == null) {
            return false;
        }
        if (updatedTask.getTitle() == null || updatedTask.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }

        for (int i = 0; i < tasks.size(); i++) {
            Task current = tasks.get(i);
            if (current.getId().equalsIgnoreCase(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                notifyListeners();
                return true;
            }
        }
        return false;
    }

    /**
     * Deletes a task by its ID.
     */
    public boolean deleteTask(String id) {
        if (id == null) {
            return false;
        }
        boolean removed = tasks.removeIf(t -> id.equalsIgnoreCase(t.getId()));
        if (removed) {
            notifyListeners();
        }
        return removed;
    }

    /**
     * Toggles a task's status between PENDING and COMPLETED.
     */
    public boolean toggleTaskStatus(String id) {
        Optional<Task> opt = getTaskById(id);
        if (opt.isPresent()) {
            Task task = opt.get();
            if (task.getStatus() == TaskStatus.COMPLETED) {
                task.markPending();
            } else {
                task.markCompleted();
            }
            notifyListeners();
            return true;
        }
        return false;
    }

    /**
     * Explicitly marks a task as completed.
     */
    public boolean markCompleted(String id) {
        Optional<Task> opt = getTaskById(id);
        if (opt.isPresent()) {
            opt.get().markCompleted();
            notifyListeners();
            return true;
        }
        return false;
    }

    /**
     * Explicitly marks a task as pending.
     */
    public boolean markPending(String id) {
        Optional<Task> opt = getTaskById(id);
        if (opt.isPresent()) {
            opt.get().markPending();
            notifyListeners();
            return true;
        }
        return false;
    }

    /**
     * Searches tasks by matching query against title, description, and category.
     * Case-insensitive.
     */
    public List<Task> searchTasks(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllTasks();
        }
        String cleanQuery = query.trim().toLowerCase();
        return tasks.stream()
                .filter(t -> (t.getTitle() != null && t.getTitle().toLowerCase().contains(cleanQuery)) ||
                        (t.getDescription() != null && t.getDescription().toLowerCase().contains(cleanQuery)) ||
                        (t.getCategory() != null && t.getCategory().getDisplayName().toLowerCase().contains(cleanQuery)))
                .collect(Collectors.toList());
    }

    /**
     * Filters tasks by status.
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        if (status == null) {
            return getAllTasks();
        }
        return tasks.stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    /**
     * Filters tasks by priority.
     */
    public List<Task> getTasksByPriority(TaskPriority priority) {
        if (priority == null) {
            return getAllTasks();
        }
        return tasks.stream()
                .filter(t -> t.getPriority() == priority)
                .collect(Collectors.toList());
    }

    /**
     * Filters tasks by category.
     */
    public List<Task> getTasksByCategory(TaskCategory category) {
        if (category == null) {
            return getAllTasks();
        }
        return tasks.stream()
                .filter(t -> t.getCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves tasks due today.
     */
    public List<Task> getTodayTasks() {
        LocalDate today = LocalDate.now();
        return tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(today))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves upcoming tasks due after today.
     */
    public List<Task> getUpcomingTasks() {
        LocalDate today = LocalDate.now();
        return tasks.stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isAfter(today))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all completed tasks.
     */
    public List<Task> getCompletedTasks() {
        return getTasksByStatus(TaskStatus.COMPLETED);
    }

    /**
     * Retrieves pending tasks that are overdue.
     */
    public List<Task> getOverdueTasks() {
        return tasks.stream()
                .filter(Task::isOverdue)
                .collect(Collectors.toList());
    }

    // --- Statistics Calculations ---

    public int getTotalCount() {
        return tasks.size();
    }

    public int getCompletedCount() {
        return (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
    }

    public int getPendingCount() {
        return (int) tasks.stream().filter(t -> t.getStatus() == TaskStatus.PENDING).count();
    }

    public int getHighPriorityCount() {
        return (int) tasks.stream().filter(t -> t.getPriority() == TaskPriority.HIGH).count();
    }

    public double getCompletionPercentage() {
        if (tasks.isEmpty()) {
            return 0.0;
        }
        double percentage = ((double) getCompletedCount() / tasks.size()) * 100.0;
        return Math.round(percentage * 10.0) / 10.0;
    }

    public Map<TaskCategory, Long> getCategoryDistribution() {
        return tasks.stream()
                .collect(Collectors.groupingBy(Task::getCategory, Collectors.counting()));
    }

    /**
     * Clears all tasks in memory. Useful for unit tests.
     */
    public void clearAll() {
        tasks.clear();
        idCounter.set(100);
        notifyListeners();
    }

    /**
     * Populates the system with realistic sample data if currently empty.
     * Prevents duplication on multiple calls.
     */
    public void loadSampleData() {
        if (!tasks.isEmpty()) {
            return; // Avoid duplicating sample data
        }

        LocalDate today = LocalDate.now();

        // 1. Complete Maven practical
        addTask(new Task(
                "TF-101",
                "Complete Maven practical",
                "Configure pom.xml dependencies, plugins, and verify local build lifecycle.",
                TaskCategory.STUDY,
                TaskPriority.HIGH,
                today,
                TaskStatus.COMPLETED
        ));

        // 2. Prepare Jenkins pipeline
        addTask(new Task(
                "TF-102",
                "Prepare Jenkins pipeline",
                "Author declarative Jenkinsfile with build, test, and artifact archiving stages.",
                TaskCategory.PROJECT,
                TaskPriority.HIGH,
                today.plusDays(1),
                TaskStatus.PENDING
        ));

        // 3. Revise Java OOP concepts
        addTask(new Task(
                "TF-103",
                "Revise Java OOP concepts",
                "Review encapsulation, inheritance, polymorphism, and abstraction with practical examples.",
                TaskCategory.STUDY,
                TaskPriority.MEDIUM,
                today,
                TaskStatus.COMPLETED
        ));

        // 4. Push project to GitHub
        addTask(new Task(
                "TF-104",
                "Push project to GitHub",
                "Initialize git repository, commit clean structure with .gitignore, and push to remote.",
                TaskCategory.WORK,
                TaskPriority.MEDIUM,
                today.plusDays(2),
                TaskStatus.PENDING
        ));

        // 5. Write JUnit test cases
        addTask(new Task(
                "TF-105",
                "Write JUnit test cases",
                "Implement automated unit tests for service business logic and verify 100% pass rate.",
                TaskCategory.PROJECT,
                TaskPriority.HIGH,
                today.plusDays(3),
                TaskStatus.PENDING
        ));

        // 6. Complete mini-project documentation
        addTask(new Task(
                "TF-106",
                "Complete mini-project documentation",
                "Draft comprehensive README with architecture overview, Maven commands, and CI instructions.",
                TaskCategory.OTHER,
                TaskPriority.LOW,
                today.plusDays(5),
                TaskStatus.PENDING
        ));
    }
}
