package ir.amv.os.intellij.plugins.cold.swap.configure.gui;

import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;

import javax.swing.*;
import java.awt.*;

public class ColdSwapConfigurationPanel {
    private JList jarBasePathsList;
    private JList extractedPathsList;
    private JPanel rootPanel;
    private JPanel jarBasePathsPanel;
    private JPanel extractedPathsPanel;

    public ColdSwapConfigurationPanel() {
    }

    public void initialize(Project project) {
        ListModel listModel = new DefaultListModel();
        jarBasePathsList.setModel(listModel);
        addActins(jarBasePathsList, button -> addJarPath(project), button -> removeJarPath(), jarBasePathsPanel);
        addActins(extractedPathsList, button -> addJarPath(project), button -> removeJarPath(), extractedPathsPanel);
    }

    private void addActins(JList jarBasePathsList, AnActionButtonRunnable addAction, AnActionButtonRunnable removeAction, JPanel containerPanel) {
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(jarBasePathsList)
                .setAddAction(addAction)
                .setRemoveAction(removeAction)
                .disableUpDownActions();
        containerPanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
    }

    private void removeJarPath() {

    }

    private void addJarPath(Project project) {
        BaseAddDirDialog d = new BaseAddDirDialog(jarBasePathsPanel, project);
        d.showAndGet();
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}
