package ir.amv.os.intellij.plugins.cold.swap.configure.gui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.util.Consumer;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapDestinationBaseDirConfig;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

public class BaseAddDirDialog extends DialogWrapper {
    private final DefaultListModel<String> exclusionsListModel;
    private final DefaultListModel<String> regexListModel;
    private JPanel contentPane;
    private TextFieldWithBrowseButton basePathChooser;
    private JList<String> exclusionsList;
    private JPanel exclusionsPanel;
    private JPanel regexPanel;
    private JList<String> regexList;
    private ColdSwapDestinationBaseDirConfig.DestinationType type;
    private JPopupMenu editExclusionPopup;
    private JTextField editExclusionTextField;
    private JPopupMenu editRegexPopup;
    private JTextField editRegexTextField;

    public BaseAddDirDialog(JPanel panel, Project project, ColdSwapDestinationBaseDirConfig.DestinationType type) {
        super(project, false);
        this.type = type;
        setModal(true);
        setTitle("Base path directory for " + type);
        exclusionsListModel = new DefaultListModel<>();
        exclusionsList.setModel(exclusionsListModel);
        ToolbarDecorator exclusionsToolbarDecorator = ToolbarDecorator.createDecorator(exclusionsList)
                .setAddAction(e -> addExclusion())
                .setRemoveAction(e -> removeExclusion())
                .disableUpDownActions();
        exclusionsPanel.add(exclusionsToolbarDecorator.createPanel(), BorderLayout.CENTER);
        createEditExclusionPopup();
        regexListModel = new DefaultListModel<>();
        regexListModel.add(0, "[^a-zA-Z]");
        regexList.setModel(regexListModel);
        ToolbarDecorator regexToolbarDecorator = ToolbarDecorator.createDecorator(regexList)
                .setAddAction(e -> addRegex())
                .setRemoveAction(e -> removeRegex())
                .disableUpDownActions();
        regexPanel.add(regexToolbarDecorator.createPanel(), BorderLayout.CENTER);
        createEditRegexPopup();
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        basePathChooser.addActionListener(e -> {
            @NonNls String path = basePathChooser.getText().trim();
            if (path.equals("")) {
                File powerCurveProgramFiles = new File("C:\\Program Files\\PowerCurve");
                if (powerCurveProgramFiles.exists()) {
                    try {
                        path = powerCurveProgramFiles.getCanonicalPath();
                    } catch (IOException ignored) {
                    }
                }
            }
            selectConfigurationDirectory(path, s -> basePathChooser.setText(s), project, panel);
        });
        init();
    }

    private void removeExclusion() {
        int[] selectedIndices = exclusionsList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            exclusionsListModel.remove(selectedIndices[i]);
        }
    }

    private void addExclusion() {
        if (!editExclusionPopup.isVisible()) {
            showEditExclusionsPopup();
        }
    }

    private void showEditExclusionsPopup() {
        editExclusionTextField.setText("");
        editExclusionTextField.setPreferredSize(new Dimension(exclusionsList.getWidth(), 20));
        int x = 0;
        int y = 0;
        if (!exclusionsListModel.isEmpty()) {
            int lastItemIndex = exclusionsListModel.size() - 1;
            Rectangle lastCellBounds = exclusionsList.getCellBounds(lastItemIndex, lastItemIndex);
            x = lastCellBounds.x;
            y = lastCellBounds.y + lastCellBounds.height;
        }
        editExclusionPopup.show(exclusionsList, x, y);
        editExclusionTextField.grabFocus();
    }

    private void createEditExclusionPopup() {
        editExclusionPopup = new JPopupMenu();
        editExclusionTextField = new JTextField();
        Border border = UIManager.getBorder("List.focusCellHighlightBorder");
        editExclusionTextField.setBorder( border );

        //  Add an Action to the text field to save the new value to the model

        editExclusionTextField.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String value = editExclusionTextField.getText();
                exclusionsListModel.add(exclusionsListModel.size(), value);
                editExclusionPopup.setVisible(false);
            }
        });

        //  Add the editor to the popup

        editExclusionPopup.setBorder( new EmptyBorder(0, 0, 0, 0) );
        editExclusionPopup.add(editExclusionTextField);
    }

    private void removeRegex() {
        int[] selectedIndices = regexList.getSelectedIndices();
        for (int i = selectedIndices.length - 1; i >= 0; i--) {
            regexListModel.remove(selectedIndices[i]);
        }
    }

    private void addRegex() {
        if (!editRegexPopup.isVisible()) {
            showRegexEditPopup();
        }
    }

    private void showRegexEditPopup() {
        editRegexTextField.setText("");
        editRegexTextField.setPreferredSize(new Dimension(regexList.getWidth(), 20));
        int x = 0;
        int y = 0;
        if (!regexListModel.isEmpty()) {
            int lastItemIndex = regexListModel.size() - 1;
            Rectangle lastCellBounds = regexList.getCellBounds(lastItemIndex, lastItemIndex);
            x = lastCellBounds.x;
            y = lastCellBounds.y + lastCellBounds.height;
        }
        editRegexPopup.show(regexList, x, y);
        editRegexTextField.grabFocus();
    }

    private void createEditRegexPopup() {
        editRegexPopup = new JPopupMenu();
        editRegexTextField = new JTextField();
        Border border = UIManager.getBorder("List.focusCellHighlightBorder");
        editRegexTextField.setBorder( border );

        //  Add an Action to the text field to save the new value to the model

        editRegexTextField.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                String value = editRegexTextField.getText();
                regexListModel.add(regexListModel.size(), value);
                editRegexPopup.setVisible(false);
            }
        });

        //  Add the editor to the popup

        editRegexPopup.setBorder( new EmptyBorder(0, 0, 0, 0) );
        editRegexPopup.add(editRegexTextField);
    }

    public static void selectConfigurationDirectory(@NotNull String path,
                                                    @NotNull final Consumer<String> dirConsumer,
                                                    final Project project,
                                                    @Nullable final Component component) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withTitle("Select Base Path")
                .withDescription("Ask Amir")
                .withShowFileSystemRoots(true)
                .withHideIgnored(false)
                .withShowHiddenFiles(true);

        path = "file://" + path.replace(File.separatorChar, '/');
        VirtualFile root = VirtualFileManager.getInstance().findFileByUrl(path);

        VirtualFile file = FileChooser.chooseFile(descriptor, component, project, root);
        if (file == null) {
            return;
        }
        final String resultPath = file.getPath().replace('/', File.separatorChar);
        dirConsumer.consume(resultPath);
    }

    private void onOK() {
        // add your code here
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public ColdSwapDestinationBaseDirConfig getDirConf() {
        ColdSwapDestinationBaseDirConfig result = new ColdSwapDestinationBaseDirConfig();
        result.setBaseDirPath(basePathChooser.getText());
        result.setType(type);
        if (!exclusionsListModel.isEmpty()) {
            ArrayList<String> exclusions = new ArrayList<>();
            Enumeration<String> elements = exclusionsListModel.elements();
            while (elements.hasMoreElements()) {
                exclusions.add(elements.nextElement());
            }
            result.setExclusions(exclusions);
        }
        if (!regexListModel.isEmpty()) {
            ArrayList<String> regexs = new ArrayList<>();
            Enumeration<String> elements = regexListModel.elements();
            while (elements.hasMoreElements()) {
                regexs.add(elements.nextElement());
            }
            result.setRegexToBeRemoved(regexs);
        }
        return result;
    }
}
