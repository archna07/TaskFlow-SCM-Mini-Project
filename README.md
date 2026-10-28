# TaskFlow — Smart Task Management System

> **College Mini Project** for the subject:  
> **Software Configuration Management (SCM) and Build Tools**  
> *Developed using Java 17, Swing, Apache Maven, and JUnit 5*

---

## 1. Project Overview

**TaskFlow** is a modern, responsive desktop Task Management System engineered to demonstrate core principles of **Software Configuration Management (SCM)**, modular object-oriented programming (OOP) in Java 17, dependency and lifecycle management with **Apache Maven**, automated unit testing with **JUnit 5**, and CI readiness for **Jenkins**.

Rather than relying on basic CRUD interfaces, TaskFlow features a bespoke **"Midnight Aurora"** dark theme inspired by modern productivity suites (such as Linear and Notion). The user interface is crafted using pure Java Swing with antialiased custom vector graphics, dynamic stat cards, gradient progress analytics, and responsive desktop layout.

---

## 2. Key Objectives

1. **SCM & Build Tools Demonstration**: Showcase Maven project conventions, dependency management, reproducible build lifecycles, and headless test execution.
2. **Clean Separation of Concerns**: Decouple domain models (`model`), service business logic (`service`), and graphical presentation (`ui`).
3. **Automated Unit Testing**: Ensure 100% automated regression protection with JUnit 5 running in headless mode for CI servers.
4. **DevOps & CI Integration**: Provide ready-to-use configuration for GitHub version control and automated Jenkins pipelines (`Jenkinsfile`).
5. **Modern Desktop UX**: Implement custom-painted UI components, high-DPI font rendering, color badges, and interactive feedback.

---

## 3. Technology Stack

| Component | Technology | Description |
|---|---|---|
| **Programming Language** | Java 17 (Temurin OpenJDK) | Modern Java with records/sealed enums readiness, streams, and java.time API |
| **Build & Dependency Tool** | Apache Maven 3.9+ | Standardized lifecycle (`compile`, `test`, `package`), plugin orchestration |
| **User Interface (GUI)** | Java Swing (Pure JDK) | Cross-platform desktop interface with custom Midnight Aurora rendering |
| **Automated Testing** | JUnit 5 Jupiter | Unit testing suite executed headlessly via `maven-surefire-plugin` |
| **Data Storage** | Java Collections | In-memory thread-safe collections (`CopyOnWriteArrayList`, atomic counters) |
| **Version Control** | Git & GitHub | Git-ready repository with clean `.gitignore` |
| **Continuous Integration** | Jenkins CI | Declarative multi-stage `Jenkinsfile` for automated build and test pipelines |

---

## 4. UI Theme — "Midnight Aurora"

The visual identity of TaskFlow incorporates modern dark-mode ergonomics:
- **Background Deep Midnight**: `#101426`
- **Sidebar Surface**: `#131832`
- **Dark Indigo Panels**: `#191F38`
- **Card Surface & Hover**: `#1E2648` / `#25305C`
- **Electric Violet Accent**: `#8B5CF6` (and `#7C3AED`)
- **Vibrant Cyan Accent**: `#43D9FF`
- **Status Indicators**:
  - Emerald Green (`#10B981`) for Completed tasks
  - Amber Orange (`#F59E0B`) for Pending tasks
  - Coral Rose (`#EF4444`) for High-priority and Overdue alerts

---

## 5. Main Application Features

### 📊 Productivity Dashboard
- **Welcome Banner**: Dynamic greeting, current formatted date, and quick task creation.
- **Dynamic Statistic Cards**: 
  - *Total Tasks*: Dynamic count of all backlog tasks.
  - *Completed Tasks*: Tasks completed with success indicators.
  - *Pending Tasks*: Active tasks awaiting completion.
  - *High Priority*: Urgent tasks requiring immediate attention.
- **Task Completion Progress**: Real-time progress bar with Violet-to-Cyan gradient fill, percentage display, dynamic status messages based on progress, and completion ratio breakdown.
- **Urgent Tasks Preview**: Instant visibility of top high-priority tasks with direct action buttons.

### 📋 Task Management & Filtering
- **Sidebar Navigation**: Seamless switching between *Dashboard*, *All Tasks*, *Today*, *Upcoming*, and *Completed*.
- **Interactive Circle Checkboxes**: Toggle tasks between Pending and Completed with instant visual update and strikethrough styling.
- **Live Search**: Case-insensitive search across task title, description, and category.
- **Category & Priority Filters**: Filter tasks by *Study*, *Work*, *Project*, *Personal*, *Other*, and *High/Medium/Low* priorities.
- **Sorting Options**: Sort by earliest due date, latest due date, priority (High-to-Low), or alphabetically (A-Z).
- **Overdue Detection**: Automatic overdue warning badges for pending tasks past their due date.

### ✏️ Create & Edit Tasks
- Modal dialog with validation (required title check with helpful red feedback).
- Description multi-line field.
- Category & Priority dropdown selectors.
- Due date picker with quick helper buttons: *Today*, *Tomorrow*, *+3 Days*, *+1 Week*.

### 🗑️ Safe Deletion
- Confirmation prompt before task deletion to prevent accidental data loss.

### 🧪 Sample Data Initialization
- On first launch, populates 6 realistic college/devops tasks with varied priorities, categories, and dates.
- Guards against duplicate task entries upon view refreshes.

---

## 6. Project Architecture & Directory Structure

```
task-management-system/
├── .gitignore                          # Standard Git ignore rules for Maven and IDEs
├── Jenkinsfile                         # Declarative Jenkins CI pipeline
├── pom.xml                             # Maven project descriptor & build configuration
├── README.md                           # Project documentation
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── taskflow/
    │               ├── Main.java                        # Application entry point
    │               ├── model/
    │               │   ├── Task.java                    # Core task entity
    │               │   ├── TaskCategory.java            # Category enum & accent colors
    │               │   ├── TaskPriority.java            # Priority levels (Low, Med, High)
    │               │   └── TaskStatus.java              # Status (Pending, Completed)
    │               ├── service/
    │               │   └── TaskManager.java             # Business logic & reactive listeners
    │               └── ui/
    │                   ├── MainFrame.java               # Main window & navigation sidebar
    │                   ├── DashboardPanel.java          # Analytics & progress dashboard
    │                   ├── TaskPanel.java               # Task list with filters & search
    │                   ├── TaskCard.java                # Individual task card widget
    │                   ├── TaskFormDialog.java          # Modal dialog for create/edit
    │                   └── components/
    │                       ├── Badge.java               # Pill badges for categories/priority
    │                       ├── CustomButton.java        # Modern buttons with hover & gradients
    │                       ├── EmptyStatePanel.java     # Empty state placeholder
    │                       ├── ModernProgressBar.java   # Gradient curved progress bar
    │                       ├── ModernTextField.java     # Inputs with placeholder & glow
    │                       ├── StatCard.java            # Dynamic metric summary cards
    │                       └── Theme.java               # Midnight Aurora design tokens
    └── test/
        └── java/
            └── com/
                └── taskflow/
                    └── service/
                        └── TaskManagerTest.java         # Headless JUnit 5 test suite
```

---

## 7. Prerequisites

Before running or building the project, ensure the following are installed on your machine:

1. **Java Development Kit (JDK) 17** or later:
   ```bash
   java -version
   ```
2. **Apache Maven 3.8+**:
   ```bash
   mvn -version
   ```

---

## 8. Build, Test, and Run Instructions

Navigate into the project directory:
```bash
cd task-management-system
```

### A. Run Automated Unit Tests
To execute the JUnit 5 test suite headlessly:
```bash
mvn test
```
*Surefire reports will be generated in `target/surefire-reports/`.*

### B. Compile and Package Application
To run all tests and package the runnable standalone JAR:
```bash
mvn clean test package
```
*The packaged JAR will be located at:*  
`target/task-management-system-1.0-SNAPSHOT.jar`

### C. Run the Application
Launch the graphical user interface from the terminal:
```bash
java -jar target/task-management-system-1.0-SNAPSHOT.jar
```
*(Or use `mvn compile exec:java -Dexec.mainClass="com.taskflow.Main"`)*

---

## 9. Future GitHub and Jenkins Integration

This repository is pre-configured for Software Configuration Management (SCM) Continuous Integration (CI) workflows:

### A. Git & GitHub Setup
1. Initialize local repository:
   ```bash
   git init
   git add .
   git commit -m "feat: Initial commit for TaskFlow SCM mini project"
   ```
2. Push to your GitHub repository:
   ```bash
   git branch -M main
   git remote add origin https://github.com/<your-username>/taskflow.git
   git push -u origin main
   ```

### B. Jenkins Pipeline Stages (`Jenkinsfile`)
The included `Jenkinsfile` configures an automated 5-stage declarative pipeline:

1. **Checkout Source**: Pulls the latest commits from the GitHub repository.
2. **Environment Validation**: Confirms the host agent has Java 17 and Maven available.
3. **Compile Java Source**: Executes `mvn clean compile` to verify syntax and compile class files.
4. **Run Automated Unit Tests**: Executes `mvn test` in headless mode and records pass/fail rates.
5. **Package Application JAR**: Runs `mvn package -DskipTests` to generate the runnable artifact.
6. **Post-Build Actions**:
   - Publishes JUnit XML test reports via `junit '**/target/surefire-reports/*.xml'`.
   - Archives the generated executable JAR in `target/`.

---

## 10. Student & Viva Quick Notes

- **Why Java 17?** Long-Term Support (LTS) release offering modern APIs (`java.time`), improved JVM performance, and broad compatibility.
- **Why Maven?** Standardizes project directory structure (`src/main/java`, `src/test/java`), automates transitive dependencies, and enables continuous integration without manual classpath configuration.
- **Why JUnit 5?** Modern test framework using annotations like `@Test`, `@BeforeEach`, and `@DisplayName` with clear assertions (`assertEquals`, `assertTrue`, `assertThrows`).
- **Headless Execution**: The `maven-surefire-plugin` runs with `-Djava.awt.headless=true`, ensuring tests execute smoothly on headless CI servers (like Jenkins) without requiring a display monitor.

---

*TaskFlow — Software Configuration Management & Build Tools Mini Project*
TaskFlow CI pipeline tested successfully with Jenkins.