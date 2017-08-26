package ir.amv.os.intellij.plugins.cold.swap.configure.gui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.PopupMenuListenerAdapter;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.util.Consumer;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapDestinationBaseDirConfig;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;

public class BaseAddDirDialog extends DialogWrapper {
    private final DefaultListModel<String> exclusionsListModel;
    private JPanel contentPane;
    private TextFieldWithBrowseButton basePathChooser;
    private JList<String> exclusionsList;
    private JPanel exclusionsPanel;
    private ColdSwapDestinationBaseDirConfig.DestinationType type;
    private JPopupMenu editExclusionPopup;
    private JTextField editExclusionTextField;

    public BaseAddDirDialog(JPanel panel, Project project, ColdSwapDestinationBaseDirConfig.DestinationType type) {
        super(project, false);
        this.type = type;
        setModal(true);
        setTitle("Base path directory for " + type);
        exclusionsListModel = new DefaultListModel<>();
        exclusionsList.setModel(exclusionsListModel);
        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(exclusionsList)
                .setAddAction(e -> addExclusion())
                .setRemoveAction(e -> removeExclusion())
                .disableUpDownActions();
        exclusionsPanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);
        createEditPopup();
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
            showEditPopup();
        }
    }

    private void showEditPopup() {
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

    private void createEditPopup() {
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
        return result;
    }
}
