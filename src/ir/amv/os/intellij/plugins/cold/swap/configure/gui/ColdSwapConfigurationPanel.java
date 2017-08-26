package ir.amv.os.intellij.plugins.cold.swap.configure.gui;

import com.intellij.openapi.options.ConfigurableUi;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ToolbarDecorator;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapConfigurationStoreObject;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapDestinationBaseDirConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ColdSwapConfigurationPanel implements ConfigurableUi<ColdSwapConfigurationStoreObject> {
    private JList<ColdSwapDestinationBaseDirConfig> jarBasePathsList;
    private JList<ColdSwapDestinationBaseDirConfig> extractedPathsList;
    private JPanel rootPanel;
    private JPanel jarBasePathsPanel;
    private JPanel extractedPathsPanel;
    private DefaultListModel<ColdSwapDestinationBaseDirConfig> jarPathsListModel;
    private DefaultListModel<ColdSwapDestinationBaseDirConfig> extractedPathsListModel;

    public void initialize(Project project) {
        jarPathsListModel = new DefaultListModel<>();
        jarBasePathsList.setModel(jarPathsListModel);
        extractedPathsListModel = new DefaultListModel<>();
        extractedPathsList.setModel(extractedPathsListModel);
        addActins(jarBasePathsList, button -> addJarPath(project), button -> removeJarPath(), jarBasePathsPanel);
        addActins(extractedPathsList, button -> addExtractedPath(project), button -> removeExtractedPath(), extractedPathsPanel);
        reset(ColdSwapConfigurationStoreObject.getInstance(project));
    }

    private void addActins(JList jarBasePathsList, AnActionButtonRunnable addAction, AnActionButtonRunnable removeAction, JPanel containerPanel) {
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(jarBasePathsList)
                .setAddAction(addAction)
                .setRemoveAction(removeAction)
                .disableUpDownActions();
        containerPanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
    }

    private void removeJarPath() {
        int[] selectedIndices = jarBasePathsList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            jarPathsListModel.remove(selectedIndices[i]);
        }
    }

    private void addJarPath(Project project) {
        BaseAddDirDialog d = new BaseAddDirDialog(jarBasePathsPanel, project, ColdSwapDestinationBaseDirConfig.DestinationType.Jar);
        if (d.showAndGet()) {
            ColdSwapDestinationBaseDirConfig dirConf = d.getDirConf();
            jarPathsListModel.add(jarPathsListModel.size(), dirConf);
        }
    }

    private void removeExtractedPath() {
        int[] selectedIndices = extractedPathsList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            extractedPathsListModel.remove(selectedIndices[i]);
        }
    }

    private void addExtractedPath(Project project) {
        BaseAddDirDialog d = new BaseAddDirDialog(extractedPathsPanel, project, ColdSwapDestinationBaseDirConfig.DestinationType.Extracted);
        if (d.showAndGet()) {
            ColdSwapDestinationBaseDirConfig dirConf = d.getDirConf();
            extractedPathsListModel.add(extractedPathsListModel.size(), dirConf);
        }
    }

    @Override
    public void reset(@NotNull ColdSwapConfigurationStoreObject coldSwapConfigurationStoreObject) {
        jarPathsListModel.clear();
        extractedPathsListModel.clear();
        List<ColdSwapDestinationBaseDirConfig> destinationDirs = coldSwapConfigurationStoreObject.getDestinationDirs();
        for (ColdSwapDestinationBaseDirConfig destinationDir : destinationDirs) {
            if (destinationDir.getType().equals(ColdSwapDestinationBaseDirConfig.DestinationType.Jar)) {
                jarPathsListModel.add(jarPathsListModel.size(), destinationDir);
            } else {
                extractedPathsListModel.add(extractedPathsListModel.size(), destinationDir);
            }
        }
    }

    @Override
    public boolean isModified(@NotNull ColdSwapConfigurationStoreObject coldSwapConfigurationStoreObject) {
        ColdSwapConfigurationStoreObject myConf = new ColdSwapConfigurationStoreObject();
        try {
            apply(myConf);
            return !myConf.equals(coldSwapConfigurationStoreObject);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void apply(@NotNull ColdSwapConfigurationStoreObject coldSwapConfigurationStoreObject) throws ConfigurationException {
        coldSwapConfigurationStoreObject.getDestinationDirs().clear();
        addListModelToConfigDirs(jarPathsListModel, coldSwapConfigurationStoreObject.getDestinationDirs());
        addListModelToConfigDirs(extractedPathsListModel, coldSwapConfigurationStoreObject.getDestinationDirs());
    }

    private void addListModelToConfigDirs(DefaultListModel<ColdSwapDestinationBaseDirConfig> listModel, List<ColdSwapDestinationBaseDirConfig> destinationDirs) {
        int size = listModel.size();
        for (int i = 0; i < size; i++) {
            ColdSwapDestinationBaseDirConfig coldSwapDestinationBaseDirConfig = listModel.get(i);
            destinationDirs.add(coldSwapDestinationBaseDirConfig);
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return rootPanel;
    }
}
