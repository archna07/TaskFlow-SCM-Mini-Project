/**
 * TaskFlow — Smart Task Management System
 * Midnight Aurora Web Application Logic
 */

document.addEventListener('DOMContentLoaded', () => {
  // --- Constants & State ---
  const STORAGE_KEY = 'taskflow_tasks';
  const ID_COUNTER_KEY = 'taskflow_id_counter';

  let tasks = [];
  let currentNavView = 'dashboard';
  let searchQuery = '';
  let categoryFilter = 'ALL';
  let priorityFilter = 'ALL';
  let sortOption = 'DATE_ASC';

  let taskToDeleteId = null;

  // --- DOM Elements ---
  const viewsContainer = document.getElementById('views-container');
  const viewDashboard = document.getElementById('view-dashboard');
  const viewTasks = document.getElementById('view-tasks');

  const navItems = document.querySelectorAll('.nav-item');
  const currentDateDisplay = document.getElementById('current-date-display');
  const globalSearchInput = document.getElementById('global-search-input');

  // Dashboard Stats & Progress
  const statTotal = document.getElementById('stat-total-value');
  const statCompleted = document.getElementById('stat-completed-value');
  const statPending = document.getElementById('stat-pending-value');
  const statHigh = document.getElementById('stat-high-value');

  const progressBarFill = document.getElementById('progress-bar-fill');
  const progressPercentageLabel = document.getElementById('progress-percentage-label');
  const progressStatusMsg = document.getElementById('progress-status-msg');
  const progressRatioLabel = document.getElementById('progress-ratio-label');
  const urgentTasksContainer = document.getElementById('urgent-tasks-container');

  // Tasks View elements
  const tasksViewTitle = document.getElementById('tasks-view-title');
  const tasksViewSubtitle = document.getElementById('tasks-view-subtitle');
  const allTasksContainer = document.getElementById('all-tasks-container');
  const tasksEmptyState = document.getElementById('tasks-empty-state');
  const emptyStateTitle = document.getElementById('empty-state-title');
  const emptyStateSub = document.getElementById('empty-state-sub');

  const tasksFilterSearch = document.getElementById('tasks-filter-search');
  const filterCategorySelect = document.getElementById('filter-category-select');
  const filterPrioritySelect = document.getElementById('filter-priority-select');
  const sortSelect = document.getElementById('sort-select');

  // Modals
  const taskDialog = document.getElementById('task-dialog');
  const taskForm = document.getElementById('task-form');
  const modalTaskTitle = document.getElementById('modal-task-title');
  const modalTaskSub = document.getElementById('modal-task-sub');
  const taskEditId = document.getElementById('task-edit-id');
  const inputTaskTitle = document.getElementById('input-task-title');
  const inputTaskDesc = document.getElementById('input-task-desc');
  const selectTaskCategory = document.getElementById('select-task-category');
  const selectTaskPriority = document.getElementById('select-task-priority');
  const inputTaskDuedate = document.getElementById('input-task-duedate');
  const titleErrorMsg = document.getElementById('title-error-msg');
  const btnCloseTaskDialog = document.getElementById('btn-close-task-dialog');
  const btnCancelTaskDialog = document.getElementById('btn-cancel-task-dialog');

  const deleteDialog = document.getElementById('delete-dialog');
  const deleteTaskPrompt = document.getElementById('delete-task-prompt');
  const btnCloseDeleteDialog = document.getElementById('btn-close-delete-dialog');
  const btnCancelDelete = document.getElementById('btn-cancel-delete');
  const btnConfirmDelete = document.getElementById('btn-confirm-delete');

  // Create Task Triggers
  const newButtons = [
    document.getElementById('btn-sidebar-new-task'),
    document.getElementById('btn-header-new-task'),
    document.getElementById('btn-hero-new-task'),
    document.getElementById('btn-tasks-toolbar-new-task'),
    document.getElementById('btn-empty-state-create')
  ];

  // --- Date Formatting Helpers ---
  function getLocalDateString(offsetDays = 0) {
    const d = new Date();
    d.setDate(d.getDate() + offsetDays);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  function formatDisplayDate(dateStr) {
    if (!dateStr) return 'No due date';
    const [y, m, d] = dateStr.split('-').map(Number);
    const date = new Date(y, m - 1, d);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const diffDays = Math.round((date - today) / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Tomorrow';
    if (diffDays === -1) return 'Yesterday';

    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  function isOverdue(task) {
    if (task.status === 'Completed' || !task.dueDate) return false;
    const [y, m, d] = task.dueDate.split('-').map(Number);
    const date = new Date(y, m - 1, d);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return date < today;
  }

  function isDueToday(task) {
    if (!task.dueDate) return false;
    return task.dueDate === getLocalDateString(0);
  }

  function isUpcoming(task) {
    if (!task.dueDate) return false;
    return task.dueDate > getLocalDateString(0);
  }

  // --- Initial Sample Data ---
  function loadInitialTasks() {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        tasks = JSON.parse(saved);
        return;
      } catch (e) {
        console.error('Failed to parse saved tasks:', e);
      }
    }

    // Default 6 Sample Tasks matching specification
    tasks = [
      {
        id: 'TF-101',
        title: 'Complete Maven practical',
        description: 'Configure pom.xml dependencies, plugins, and verify local build lifecycle.',
        category: 'Study',
        priority: 'High',
        dueDate: getLocalDateString(0),
        status: 'Completed',
        createdAt: new Date().toISOString()
      },
      {
        id: 'TF-102',
        title: 'Prepare Jenkins pipeline',
        description: 'Author declarative Jenkinsfile with build, test, and artifact archiving stages.',
        category: 'Project',
        priority: 'High',
        dueDate: getLocalDateString(1),
        status: 'Pending',
        createdAt: new Date().toISOString()
      },
      {
        id: 'TF-103',
        title: 'Revise Java OOP concepts',
        description: 'Review encapsulation, inheritance, polymorphism, and abstraction with practical examples.',
        category: 'Study',
        priority: 'Medium',
        dueDate: getLocalDateString(0),
        status: 'Completed',
        createdAt: new Date().toISOString()
      },
      {
        id: 'TF-104',
        title: 'Push project to GitHub',
        description: 'Initialize git repository, commit clean structure with .gitignore, and push to remote.',
        category: 'Work',
        priority: 'Medium',
        dueDate: getLocalDateString(2),
        status: 'Pending',
        createdAt: new Date().toISOString()
      },
      {
        id: 'TF-105',
        title: 'Write JUnit test cases',
        description: 'Implement automated unit tests for service business logic and verify 100% pass rate.',
        category: 'Project',
        priority: 'High',
        dueDate: getLocalDateString(3),
        status: 'Pending',
        createdAt: new Date().toISOString()
      },
      {
        id: 'TF-106',
        title: 'Complete mini-project documentation',
        description: 'Draft comprehensive README with architecture overview, Maven commands, and CI instructions.',
        category: 'Other',
        priority: 'Low',
        dueDate: getLocalDateString(5),
        status: 'Pending',
        createdAt: new Date().toISOString()
      }
    ];

    saveTasks();
  }

  function saveTasks() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(tasks));
    updateApp();
  }

  function generateNextId() {
    let current = parseInt(localStorage.getItem(ID_COUNTER_KEY) || '106', 10);
    current += 1;
    localStorage.setItem(ID_COUNTER_KEY, String(current));
    return `TF-${current}`;
  }

  // --- Dynamic Dashboard & Statistics Math ---
  function updateDashboardStats() {
    const total = tasks.length;
    const completed = tasks.filter(t => t.status === 'Completed').length;
    const pending = tasks.filter(t => t.status === 'Pending').length;
    const high = tasks.filter(t => t.priority === 'High' && t.status === 'Pending').length;

    statTotal.textContent = total;
    statCompleted.textContent = completed;
    statPending.textContent = pending;
    statHigh.textContent = high;

    const percentage = total === 0 ? 0 : Math.round((completed / total) * 1000) / 10;
    progressBarFill.style.width = `${percentage}%`;
    progressPercentageLabel.textContent = `${percentage.toFixed(1)}%`;
    progressRatioLabel.textContent = `${completed} of ${total} tasks completed`;

    if (percentage >= 100) {
      progressBarFill.classList.add('complete');
      progressStatusMsg.textContent = '🎉 All tasks completed! Outstanding focus & execution!';
    } else {
      progressBarFill.classList.remove('complete');
      if (total === 0) {
        progressStatusMsg.textContent = 'No tasks created yet. Plan your goals today!';
      } else if (percentage >= 75) {
        progressStatusMsg.textContent = 'Almost there! Finish the final few tasks strong.';
      } else if (percentage >= 50) {
        progressStatusMsg.textContent = 'Halfway milestone passed! Outstanding focus.';
      } else if (percentage > 0) {
        progressStatusMsg.textContent = 'Good start! Keep up the momentum.';
      } else {
        progressStatusMsg.textContent = 'Ready to conquer today? Start with your highest priority task.';
      }
    }

    renderUrgentTasks();
  }

  function renderUrgentTasks() {
    urgentTasksContainer.innerHTML = '';
    const urgent = tasks
      .filter(t => t.status === 'Pending')
      .sort((a, b) => {
        const pOrder = { High: 3, Medium: 2, Low: 1 };
        return (pOrder[b.priority] || 0) - (pOrder[a.priority] || 0);
      })
      .slice(0, 4);

    if (urgent.length === 0) {
      urgentTasksContainer.innerHTML = `
        <div style="padding: 24px; text-align: center; color: var(--status-green); background: var(--bg-card); border-radius: var(--radius-md); border: 1px solid var(--border-subtle);">
          ✨ All pending tasks are cleared! Incredible job!
        </div>
      `;
      return;
    }

    urgent.forEach(task => {
      urgentTasksContainer.appendChild(createTaskCardElement(task));
    });
  }

  // --- Task Card Element Builder ---
  function createTaskCardElement(task) {
    const card = document.createElement('div');
    card.className = `task-card ${task.priority === 'High' ? 'high-priority' : ''} ${task.status === 'Completed' ? 'is-completed' : ''}`;
    card.id = `task-card-${task.id}`;

    const overdue = isOverdue(task);
    const dueToday = isDueToday(task);

    // Format display date
    const dateFormatted = formatDisplayDate(task.dueDate);

    card.innerHTML = `
      <button type="button" class="task-checkbox" data-id="${task.id}" title="${task.status === 'Completed' ? 'Mark as Pending' : 'Mark as Completed'}" aria-label="Toggle task status">
        ✓
      </button>

      <div class="task-info-block">
        <div class="task-title-row">
          <span class="task-id-tag">${task.id}</span>
          <span class="task-title">${escapeHtml(task.title)}</span>
          ${overdue ? '<span class="badge badge-date overdue">OVERDUE</span>' : ''}
        </div>

        ${task.description ? `<p class="task-description">${escapeHtml(task.description)}</p>` : ''}

        <div class="task-meta-row">
          <span class="badge badge-cat-${task.category.toLowerCase()}">
            <span class="badge-dot"></span>
            ${task.category}
          </span>

          <span class="badge badge-pri-${task.priority.toLowerCase()}">
            ${task.priority} Priority
          </span>

          ${task.dueDate ? `
            <span class="badge badge-date ${overdue ? 'overdue' : (dueToday ? 'today' : '')}">
              📅 ${overdue ? 'Overdue: ' : 'Due: '}${dateFormatted}
            </span>
          ` : ''}

          <span class="badge badge-status-${task.status.toLowerCase()}">
            ${task.status}
          </span>
        </div>
      </div>

      <div class="task-actions">
        <button type="button" class="task-action-btn ${task.status === 'Completed' ? '' : 'btn-complete'}" data-action="toggle" data-id="${task.id}">
          ${task.status === 'Completed' ? 'Reopen' : 'Complete'}
        </button>
        <button type="button" class="task-action-btn" data-action="edit" data-id="${task.id}">
          Edit
        </button>
        <button type="button" class="task-action-btn btn-delete" data-action="delete" data-id="${task.id}">
          Delete
        </button>
      </div>
    `;

    return card;
  }

  function escapeHtml(text) {
    if (!text) return '';
    return text
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  // --- Filter and Search Tasks View ---
  function renderAllTasksView() {
    allTasksContainer.innerHTML = '';

    // 1. Filter by view mode (All, Today, Upcoming, Completed)
    let filtered = tasks.filter(t => {
      switch (currentNavView) {
        case 'today':
          return isDueToday(t);
        case 'upcoming':
          return isUpcoming(t);
        case 'completed':
          return t.status === 'Completed';
        case 'all':
        default:
          return true;
      }
    });

    // 2. Filter by search query
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      filtered = filtered.filter(t =>
        (t.title && t.title.toLowerCase().includes(q)) ||
        (t.description && t.description.toLowerCase().includes(q)) ||
        (t.category && t.category.toLowerCase().includes(q))
      );
    }

    // 3. Filter by Category dropdown
    if (categoryFilter !== 'ALL') {
      filtered = filtered.filter(t => t.category.toLowerCase() === categoryFilter.toLowerCase());
    }

    // 4. Filter by Priority dropdown
    if (priorityFilter !== 'ALL') {
      filtered = filtered.filter(t => t.priority.toLowerCase() === priorityFilter.toLowerCase());
    }

    // 5. Sort list
    filtered.sort((a, b) => {
      if (sortOption === 'DATE_ASC') {
        if (!a.dueDate) return 1;
        if (!b.dueDate) return -1;
        return a.dueDate.localeCompare(b.dueDate);
      } else if (sortOption === 'DATE_DESC') {
        if (!a.dueDate) return 1;
        if (!b.dueDate) return -1;
        return b.dueDate.localeCompare(a.dueDate);
      } else if (sortOption === 'PRIORITY_DESC') {
        const pOrder = { High: 3, Medium: 2, Low: 1 };
        return (pOrder[b.priority] || 0) - (pOrder[a.priority] || 0);
      } else if (sortOption === 'TITLE_ASC') {
        return a.title.localeCompare(b.title);
      }
      return 0;
    });

    // Update title with count
    const titlesMap = {
      all: 'All Tasks',
      today: "Today's Tasks",
      upcoming: 'Upcoming Tasks',
      completed: 'Completed Tasks'
    };
    const currentTitle = titlesMap[currentNavView] || 'All Tasks';
    tasksViewTitle.textContent = `${currentTitle} (${filtered.length})`;

    if (filtered.length === 0) {
      tasksEmptyState.style.display = 'flex';
      if (searchQuery.trim()) {
        emptyStateTitle.textContent = 'No matching tasks found';
        emptyStateSub.textContent = `No tasks matched query "${searchQuery}". Try clearing filters or creating a task.`;
      } else {
        emptyStateTitle.textContent = 'No tasks in this section';
        emptyStateSub.textContent = 'You are all caught up! Plan your next task to get started.';
      }
    } else {
      tasksEmptyState.style.display = 'none';
      filtered.forEach(task => {
        allTasksContainer.appendChild(createTaskCardElement(task));
      });
    }
  }

  function updateApp() {
    updateDashboardStats();
    renderAllTasksView();
  }

  // --- Navigation View Switcher ---
  function switchView(viewKey) {
    currentNavView = viewKey;

    navItems.forEach(item => {
      item.classList.toggle('active', item.getAttribute('data-view') === viewKey);
    });

    if (viewKey === 'dashboard') {
      viewDashboard.classList.add('active');
      viewTasks.classList.remove('active');
    } else {
      viewDashboard.classList.remove('active');
      viewTasks.classList.add('active');

      const subtitleMap = {
        all: 'Everything in your task backlog',
        today: 'Tasks scheduled for today',
        upcoming: 'Tasks scheduled for upcoming days',
        completed: 'Archive of finished tasks'
      };
      tasksViewSubtitle.textContent = subtitleMap[viewKey] || 'Backlog overview';

      renderAllTasksView();
    }
  }

  // Nav Item click events
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      const viewKey = item.getAttribute('data-view');
      switchView(viewKey);
    });
  });

  document.getElementById('btn-view-all-urgent')?.addEventListener('click', () => {
    switchView('all');
  });

  // --- Global & Toolbar Search Listeners ---
  globalSearchInput.addEventListener('input', (e) => {
    searchQuery = e.target.value;
    tasksFilterSearch.value = searchQuery;
    if (currentNavView === 'dashboard') {
      switchView('all');
    } else {
      renderAllTasksView();
    }
  });

  tasksFilterSearch.addEventListener('input', (e) => {
    searchQuery = e.target.value;
    globalSearchInput.value = searchQuery;
    renderAllTasksView();
  });

  filterCategorySelect.addEventListener('change', (e) => {
    categoryFilter = e.target.value;
    renderAllTasksView();
  });

  filterPrioritySelect.addEventListener('change', (e) => {
    priorityFilter = e.target.value;
    renderAllTasksView();
  });

  sortSelect.addEventListener('change', (e) => {
    sortOption = e.target.value;
    renderAllTasksView();
  });

  // --- Modal: Create & Edit Task Dialog ---
  function openTaskDialog(taskToEdit = null) {
    titleErrorMsg.style.display = 'none';

    if (taskToEdit) {
      modalTaskTitle.textContent = 'Edit Task';
      modalTaskSub.textContent = `Modify properties for ${taskToEdit.id}`;
      taskEditId.value = taskToEdit.id;
      inputTaskTitle.value = taskToEdit.title;
      inputTaskDesc.value = taskToEdit.description || '';
      selectTaskCategory.value = taskToEdit.category;
      selectTaskPriority.value = taskToEdit.priority;
      inputTaskDuedate.value = taskToEdit.dueDate || getLocalDateString(0);
    } else {
      modalTaskTitle.textContent = 'Create New Task';
      modalTaskSub.textContent = 'Fill in the details to plan your workflow';
      taskEditId.value = '';
      inputTaskTitle.value = '';
      inputTaskDesc.value = '';
      selectTaskCategory.value = 'Project';
      selectTaskPriority.value = 'Medium';
      inputTaskDuedate.value = getLocalDateString(0);
    }

    taskDialog.showModal();
    inputTaskTitle.focus();
  }

  function closeTaskDialog() {
    taskDialog.close();
  }

  newButtons.forEach(btn => {
    btn?.addEventListener('click', () => openTaskDialog(null));
  });

  btnCloseTaskDialog.addEventListener('click', closeTaskDialog);
  btnCancelTaskDialog.addEventListener('click', closeTaskDialog);

  // Close dialog on backdrop click (Light-dismiss)
  taskDialog.addEventListener('click', (e) => {
    const rect = taskDialog.getBoundingClientRect();
    if (e.clientX < rect.left || e.clientX > rect.right || e.clientY < rect.top || e.clientY > rect.bottom) {
      taskDialog.close();
    }
  });

  deleteDialog.addEventListener('click', (e) => {
    const rect = deleteDialog.getBoundingClientRect();
    if (e.clientX < rect.left || e.clientX > rect.right || e.clientY < rect.top || e.clientY > rect.bottom) {
      deleteDialog.close();
    }
  });

  // Date Presets
  document.querySelectorAll('.date-preset-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      const offset = parseInt(chip.getAttribute('data-offset') || '0', 10);
      inputTaskDuedate.value = getLocalDateString(offset);
    });
  });

  // Form Submit Handler
  taskForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const title = inputTaskTitle.value.trim();

    if (!title) {
      titleErrorMsg.style.display = 'block';
      inputTaskTitle.focus();
      return;
    }

    const editId = taskEditId.value;
    const desc = inputTaskDesc.value.trim();
    const cat = selectTaskCategory.value;
    const pri = selectTaskPriority.value;
    const due = inputTaskDuedate.value || getLocalDateString(0);

    if (editId) {
      // Edit existing task
      const targetIndex = tasks.findIndex(t => t.id === editId);
      if (targetIndex !== -1) {
        tasks[targetIndex].title = title;
        tasks[targetIndex].description = desc;
        tasks[targetIndex].category = cat;
        tasks[targetIndex].priority = pri;
        tasks[targetIndex].dueDate = due;
      }
    } else {
      // Create new task
      const newTask = {
        id: generateNextId(),
        title: title,
        description: desc,
        category: cat,
        priority: pri,
        dueDate: due,
        status: 'Pending',
        createdAt: new Date().toISOString()
      };
      tasks.unshift(newTask);
    }

    saveTasks();
    closeTaskDialog();
  });

  // --- Task Card Event Delegation (Toggle, Edit, Delete) ---
  document.addEventListener('click', (e) => {
    const toggleCheckbox = e.target.closest('.task-checkbox');
    if (toggleCheckbox) {
      const id = toggleCheckbox.getAttribute('data-id');
      toggleTaskStatus(id);
      return;
    }

    const actionBtn = e.target.closest('.task-action-btn');
    if (actionBtn) {
      const action = actionBtn.getAttribute('data-action');
      const id = actionBtn.getAttribute('data-id');

      if (action === 'toggle') {
        toggleTaskStatus(id);
      } else if (action === 'edit') {
        const task = tasks.find(t => t.id === id);
        if (task) openTaskDialog(task);
      } else if (action === 'delete') {
        openDeleteModal(id);
      }
    }
  });

  function toggleTaskStatus(id) {
    const task = tasks.find(t => t.id === id);
    if (!task) return;

    if (task.status === 'Completed') {
      task.status = 'Pending';
      delete task.completedAt;
    } else {
      task.status = 'Completed';
      task.completedAt = new Date().toISOString();
    }
    saveTasks();
  }

  function openDeleteModal(id) {
    const task = tasks.find(t => t.id === id);
    if (!task) return;

    taskToDeleteId = id;
    deleteTaskPrompt.innerHTML = `Are you sure you want to delete <strong>"${escapeHtml(task.title)}"</strong> (${task.id})? This action cannot be undone.`;
    deleteDialog.showModal();
  }

  btnCloseDeleteDialog.addEventListener('click', () => deleteDialog.close());
  btnCancelDelete.addEventListener('click', () => deleteDialog.close());

  btnConfirmDelete.addEventListener('click', () => {
    if (taskToDeleteId) {
      tasks = tasks.filter(t => t.id !== taskToDeleteId);
      taskToDeleteId = null;
      saveTasks();
      deleteDialog.close();
    }
  });

  // --- Date Banner Header Initialization ---
  function initHeaderDate() {
    const now = new Date();
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    currentDateDisplay.textContent = `📅 ${now.toLocaleDateString('en-US', options)} • Midnight Aurora Workspace`;
  }

  // --- Start Application ---
  initHeaderDate();
  loadInitialTasks();
  updateApp();
});
