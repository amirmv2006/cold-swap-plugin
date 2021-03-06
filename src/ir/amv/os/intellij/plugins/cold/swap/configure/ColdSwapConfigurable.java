package ir.amv.os.intellij.plugins.cold.swap.configure;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import ir.amv.os.intellij.plugins.cold.swap.configure.gui.ColdSwapConfigurationPanel;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapConfigurationStoreObject;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ColdSwapConfigurable
        implements SearchableConfigurable {

    private Project project;
    private ColdSwapConfigurationPanel configurationPanel;

    public ColdSwapConfigurable(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public String getId() {
        return "preferences.ColdSwapDestinations";
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Cold Swap Destinations";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        configurationPanel = new ColdSwapConfigurationPanel();
        configurationPanel.initialize(project);
        return configurationPanel.getComponent();
    }

    @Override
    public boolean isModified() {
        return configurationPanel.isModified(ColdSwapConfigurationStoreObject.getInstance(project));
    }

    @Override
    public void apply() throws ConfigurationException {
        configurationPanel.apply(ColdSwapConfigurationStoreObject.getInstance(project));
    }
}
