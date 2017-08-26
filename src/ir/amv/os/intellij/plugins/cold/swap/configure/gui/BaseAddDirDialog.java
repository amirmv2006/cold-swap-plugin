package ir.amv.os.intellij.plugins.cold.swap.configure.gui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class BaseAddDirDialog extends DialogWrapper {
    private JPanel contentPane;
    private TextFieldWithBrowseButton basePathChooser;

    public BaseAddDirDialog(JPanel panel, Project project) {
        super(project, false);
        setModal(true);
        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        basePathChooser.addActionListener(e -> {
            @NonNls String path = basePathChooser.getText().trim();
            selectConfigurationDirectory(path, s -> basePathChooser.setText(s), project, panel);
        });
        init();
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
}
