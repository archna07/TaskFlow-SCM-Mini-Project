package com.taskflow.service;

import com.taskflow.model.Task;
import com.taskflow.model.TaskCategory;
import com.taskflow.model.TaskPriority;
import com.taskflow.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated unit test suite for TaskManager business logic.
 * Runs in headless environment for continuous integration (Jenkins/GitHub Actions).
 */
public class TaskManagerTest {

    private TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new TaskManager();
        taskManager.clearAll();
    }

    @Test
    @DisplayName("Should successfully add a valid task and auto-generate ID")
    void testAddTaskSuccessfully() {
        Task task = new Task("Implement Unit Tests", "Write comprehensive JUnit tests", TaskCategory.PROJECT, TaskPriority.HIGH, LocalDate.now());
        Task added = taskManager.addTask(task);

        assertNotNull(added.getId(), "Generated task ID should not be null");
        assertTrue(added.getId().startsWith("TF-"), "Task ID should start with prefix TF-");
        assertEquals("Implement Unit Tests", added.getTitle());
        assertEquals(1, taskManager.getTotalCount());
    }

    @Test
    @DisplayName("Should reject adding task with null or empty title")
    void testAddTaskRejectsEmptyOrNullTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Task("", "Description", TaskCategory.STUDY, TaskPriority.LOW, LocalDate.now());
        }, "Constructor should reject empty title");

        assertThrows(IllegalArgumentException.class, () -> {
            new Task("   ", "Description", TaskCategory.STUDY, TaskPriority.LOW, LocalDate.now());
        }, "Constructor should reject blank title");

        assertThrows(IllegalArgumentException.class, () -> {
            taskManager.addTask(null);
        }, "TaskManager should reject null task");
    }

    @Test
    @DisplayName("Should retrieve all added tasks")
    void testGetAllTasks() {
        taskManager.addTask(new Task("Task 1", "Desc 1", TaskCategory.WORK, TaskPriority.LOW, LocalDate.now()));
        taskManager.addTask(new Task("Task 2", "Desc 2", TaskCategory.STUDY, TaskPriority.MEDIUM, LocalDate.now()));
        taskManager.addTask(new Task("Task 3", "Desc 3", TaskCategory.PROJECT, TaskPriority.HIGH, LocalDate.now()));

        List<Task> all = taskManager.getAllTasks();
        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("Should find a task by ID and return empty for nonexistent ID")
    void testGetTaskById() {
        Task task = taskManager.addTask(new Task("Check SCM concepts", "Git & Maven", TaskCategory.STUDY, TaskPriority.MEDIUM, LocalDate.now()));
        String id = task.getId();

        Optional<Task> found = taskManager.getTaskById(id);
        assertTrue(found.isPresent());
        assertEquals("Check SCM concepts", found.get().getTitle());

        Optional<Task> nonexistent = taskManager.getTaskById("TF-9999");
        assertFalse(nonexistent.isPresent(), "Nonexistent ID should return empty optional");

        Optional<Task> nullId = taskManager.getTaskById(null);
        assertFalse(nullId.isPresent(), "Null ID should return empty optional");
    }

    @Test
    @DisplayName("Should successfully update existing task properties")
    void testUpdateTaskSuccessfully() {
        Task task = taskManager.addTask(new Task("Initial Title", "Initial Desc", TaskCategory.WORK, TaskPriority.LOW, LocalDate.now()));
        String id = task.getId();

        Task updated = task.copy();
        updated.setTitle("Updated Title");
        updated.setDescription("Updated Description");
        updated.setPriority(TaskPriority.HIGH);
        updated.setCategory(TaskCategory.PROJECT);

        boolean result = taskManager.updateTask(updated);
        assertTrue(result, "Updating existing task should return true");

        Optional<Task> fetched = taskManager.getTaskById(id);
        assertTrue(fetched.isPresent());
        assertEquals("Updated Title", fetched.get().getTitle());
        assertEquals("Updated Description", fetched.get().getDescription());
        assertEquals(TaskPriority.HIGH, fetched.get().getPriority());
        assertEquals(TaskCategory.PROJECT, fetched.get().getCategory());
    }

    @Test
    @DisplayName("Should fail when updating nonexistent task or invalid task")
    void testUpdateNonexistentTaskReturnsFalse() {
        Task phantom = new Task("Phantom", "Does not exist", TaskCategory.OTHER, TaskPriority.LOW, LocalDate.now());
        phantom.setId("TF-8888");

        boolean result = taskManager.updateTask(phantom);
        assertFalse(result, "Updating nonexistent task should return false");

        assertFalse(taskManager.updateTask(null), "Updating null task should return false");
    }

    @Test
    @DisplayName("Should delete existing task and update task counts")
    void testDeleteTask() {
        Task t1 = taskManager.addTask(new Task("Task A", "Desc A", TaskCategory.STUDY, TaskPriority.LOW, LocalDate.now()));
        Task t2 = taskManager.addTask(new Task("Task B", "Desc B", TaskCategory.WORK, TaskPriority.HIGH, LocalDate.now()));

        assertEquals(2, taskManager.getTotalCount());

        boolean deleted = taskManager.deleteTask(t1.getId());
        assertTrue(deleted, "Deleting existing task should return true");
        assertEquals(1, taskManager.getTotalCount());
        assertFalse(taskManager.getTaskById(t1.getId()).isPresent());

        boolean deleteAgain = taskManager.deleteTask(t1.getId());
        assertFalse(deleteAgain, "Deleting already-deleted task should return false");

        boolean deleteNull = taskManager.deleteTask(null);
        assertFalse(deleteNull, "Deleting null task ID should return false");
    }

    @Test
    @DisplayName("Should complete and toggle task status")
    void testCompleteAndToggleTaskStatus() {
        Task task = taskManager.addTask(new Task("Complete Milestone", "Desc", TaskCategory.PROJECT, TaskPriority.HIGH, LocalDate.now()));
        assertEquals(TaskStatus.PENDING, task.getStatus());

        // Mark completed
        boolean completed = taskManager.markCompleted(task.getId());
        assertTrue(completed);
        assertEquals(TaskStatus.COMPLETED, taskManager.getTaskById(task.getId()).get().getStatus());
        assertNotNull(taskManager.getTaskById(task.getId()).get().getCompletedAt());

        // Toggle back to pending
        boolean toggledToPending = taskManager.toggleTaskStatus(task.getId());
        assertTrue(toggledToPending);
        assertEquals(TaskStatus.PENDING, taskManager.getTaskById(task.getId()).get().getStatus());
        assertNull(taskManager.getTaskById(task.getId()).get().getCompletedAt());

        // Toggle back to completed
        boolean toggledToCompleted = taskManager.toggleTaskStatus(task.getId());
        assertTrue(toggledToCompleted);
        assertEquals(TaskStatus.COMPLETED, taskManager.getTaskById(task.getId()).get().getStatus());
    }

    @Test
    @DisplayName("Should search tasks by title, description, and category case-insensitively")
    void testSearchTasks() {
        taskManager.addTask(new Task("Jenkins Pipeline Setup", "Configure declarative Jenkinsfile", TaskCategory.PROJECT, TaskPriority.HIGH, LocalDate.now()));
        taskManager.addTask(new Task("Git Branching Strategy", "Review git rebase and merge", TaskCategory.STUDY, TaskPriority.MEDIUM, LocalDate.now()));
        taskManager.addTask(new Task("Buy Stationery", "Notebooks and pens", TaskCategory.PERSONAL, TaskPriority.LOW, LocalDate.now()));

        // Search by title (lowercase)
        List<Task> searchResults1 = taskManager.searchTasks("jenkins");
        assertEquals(1, searchResults1.size());
        assertEquals("Jenkins Pipeline Setup", searchResults1.get(0).getTitle());

        // Search by description (mixed case)
        List<Task> searchResults2 = taskManager.searchTasks("ReBaSe");
        assertEquals(1, searchResults2.size());
        assertEquals("Git Branching Strategy", searchResults2.get(0).getTitle());

        // Search by category
        List<Task> searchResults3 = taskManager.searchTasks("personal");
        assertEquals(1, searchResults3.size());
        assertEquals("Buy Stationery", searchResults3.get(0).getTitle());

        // Empty search query returns all
        List<Task> all = taskManager.searchTasks("");
        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("Should correctly filter tasks by status, priority, and date")
    void testFilterByStatusAndPriority() {
        LocalDate today = LocalDate.now();
        taskManager.addTask(new Task("TF-1", "Task 1", "Desc", TaskCategory.STUDY, TaskPriority.HIGH, today, TaskStatus.PENDING));
        taskManager.addTask(new Task("TF-2", "Task 2", "Desc", TaskCategory.WORK, TaskPriority.HIGH, today.plusDays(2), TaskStatus.COMPLETED));
        taskManager.addTask(new Task("TF-3", "Task 3", "Desc", TaskCategory.PROJECT, TaskPriority.LOW, today.plusDays(3), TaskStatus.PENDING));

        // Filter by Status
        assertEquals(2, taskManager.getTasksByStatus(TaskStatus.PENDING).size());
        assertEquals(1, taskManager.getTasksByStatus(TaskStatus.COMPLETED).size());

        // Filter by Priority
        assertEquals(2, taskManager.getTasksByPriority(TaskPriority.HIGH).size());
        assertEquals(1, taskManager.getTasksByPriority(TaskPriority.LOW).size());
        assertEquals(0, taskManager.getTasksByPriority(TaskPriority.MEDIUM).size());

        // Date filters
        assertEquals(1, taskManager.getTodayTasks().size());
        assertEquals(2, taskManager.getUpcomingTasks().size());
    }

    @Test
    @DisplayName("Should calculate task statistics accurately")
    void testStatisticsCalculations() {
        // Zero state
        assertEquals(0, taskManager.getTotalCount());
        assertEquals(0, taskManager.getCompletedCount());
        assertEquals(0, taskManager.getPendingCount());
        assertEquals(0, taskManager.getHighPriorityCount());
        assertEquals(0.0, taskManager.getCompletionPercentage());

        LocalDate today = LocalDate.now();
        taskManager.addTask(new Task("TF-1", "Task 1", "Desc", TaskCategory.STUDY, TaskPriority.HIGH, today, TaskStatus.COMPLETED));
        taskManager.addTask(new Task("TF-2", "Task 2", "Desc", TaskCategory.WORK, TaskPriority.HIGH, today, TaskStatus.PENDING));
        taskManager.addTask(new Task("TF-3", "Task 3", "Desc", TaskCategory.PROJECT, TaskPriority.MEDIUM, today, TaskStatus.PENDING));
        taskManager.addTask(new Task("TF-4", "Task 4", "Desc", TaskCategory.PERSONAL, TaskPriority.LOW, today, TaskStatus.COMPLETED));

        assertEquals(4, taskManager.getTotalCount());
        assertEquals(2, taskManager.getCompletedCount());
        assertEquals(2, taskManager.getPendingCount());
        assertEquals(2, taskManager.getHighPriorityCount());
        assertEquals(50.0, taskManager.getCompletionPercentage());
    }

    @Test
    @DisplayName("Should initialize sample data without duplication")
    void testSampleDataInitialization() {
        assertEquals(0, taskManager.getTotalCount());
        taskManager.loadSampleData();
        assertEquals(6, taskManager.getTotalCount());

        // Calling again should not duplicate
        taskManager.loadSampleData();
        assertEquals(6, taskManager.getTotalCount());
    }
}
