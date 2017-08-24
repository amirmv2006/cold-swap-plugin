package ir.amv.os.intellij.plugins.cold.swap.configure;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ColdSwapConfigurationPanel {
    private JTable pathsTable;
    private JPanel rootPanel;

    public ColdSwapConfigurationPanel() {
    }

    public void initialize() {
        DefaultTableModel dataModel = new DefaultTableModel(new Object[][]{{1, 2, 3}}, new Object[]{"One", "Two", "Three"});
        pathsTable.setModel(dataModel);
    }

    public JPanel getRootPanel() {
        return rootPanel;
    }
}
