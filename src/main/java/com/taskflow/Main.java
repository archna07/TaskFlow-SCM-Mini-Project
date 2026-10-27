package com.taskflow;

import com.taskflow.service.TaskManager;
import com.taskflow.ui.MainFrame;

import javax.swing.*;

/**
 * Application Entry Point for TaskFlow — Smart Task Management System.
 * Configures display rendering hints, initializes sample data, and launches MainFrame.
 */
public class Main {

    public static void main(String[] args) {
        // High DPI and Antialiasing rendering hints
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("sun.java2d.uiScale.enabled", "true");

        // Use system look and feel as baseline, components apply custom Midnight Aurora theme
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback gracefully to default Swing Look & Feel
        }

        // Initialize Service & Load initial sample data
        TaskManager taskManager = new TaskManager();
        taskManager.loadSampleData();

        // Launch UI on Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(taskManager);
            mainFrame.setVisible(true);
        });
    }
}
