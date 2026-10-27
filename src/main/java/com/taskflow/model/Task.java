package com.taskflow.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a single task in the TaskFlow system.
 * Contains all business metadata, validation, and lifecycle states.
 */
public class Task {
    private String id;
    private String title;
    private String description;
    private TaskCategory category;
    private TaskPriority priority;
    private LocalDate dueDate;
    private TaskStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Task() {
        this.category = TaskCategory.OTHER;
        this.priority = TaskPriority.MEDIUM;
        this.status = TaskStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Task(String title, String description, TaskCategory category, TaskPriority priority, LocalDate dueDate) {
        this();
        setTitle(title);
        this.description = description != null ? description.trim() : "";
        this.category = category != null ? category : TaskCategory.OTHER;
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
        this.dueDate = dueDate;
    }

    public Task(String id, String title, String description, TaskCategory category, TaskPriority priority, LocalDate dueDate, TaskStatus status) {
        this(title, description, category, priority, dueDate);
        this.id = id;
        this.status = status != null ? status : TaskStatus.PENDING;
        if (this.status == TaskStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * Creates a deep copy of this task for safe editing.
     */
    public Task copy() {
        Task clone = new Task();
        clone.id = this.id;
        clone.title = this.title;
        clone.description = this.description;
        clone.category = this.category;
        clone.priority = this.priority;
        clone.dueDate = this.dueDate;
        clone.status = this.status;
        clone.createdAt = this.createdAt;
        clone.completedAt = this.completedAt;
        return clone;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty or null.");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }

    public TaskCategory getCategory() {
        return category != null ? category : TaskCategory.OTHER;
    }

    public void setCategory(TaskCategory category) {
        this.category = category != null ? category : TaskCategory.OTHER;
    }

    public TaskPriority getPriority() {
        return priority != null ? priority : TaskPriority.MEDIUM;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority != null ? priority : TaskPriority.MEDIUM;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public TaskStatus getStatus() {
        return status != null ? status : TaskStatus.PENDING;
    }

    public void setStatus(TaskStatus status) {
        this.status = status != null ? status : TaskStatus.PENDING;
        if (this.status == TaskStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        } else if (this.status == TaskStatus.PENDING) {
            this.completedAt = null;
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    /**
     * Checks if this task is pending and past its due date.
     */
    public boolean isOverdue() {
        return status == TaskStatus.PENDING && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    /**
     * Checks if this task is scheduled for today.
     */
    public boolean isDueToday() {
        return dueDate != null && dueDate.isEqual(LocalDate.now());
    }

    /**
     * Checks if this task is scheduled for future dates.
     */
    public boolean isUpcoming() {
        return dueDate != null && dueDate.isAfter(LocalDate.now());
    }

    /**
     * Marks the task as completed.
     */
    public void markCompleted() {
        setStatus(TaskStatus.COMPLETED);
    }

    /**
     * Marks the task as pending.
     */
    public void markPending() {
        setStatus(TaskStatus.PENDING);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", priority=" + priority +
                ", dueDate=" + dueDate +
                ", status=" + status +
                '}';
    }
}
