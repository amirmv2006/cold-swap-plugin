package ir.amv.os.intellij.plugins.cold.swap.configure.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@State(
        name="ColdSwapConfigurationStoreObject",
        storages = {
                @Storage("ColdSwapConf.xml")}
)
public class ColdSwapConfigurationStoreObject implements PersistentStateComponent<ColdSwapConfigurationStoreObject> {

    @Tag("destinations")
    @AbstractCollection(elementTag = "destDir")
    public List<ColdSwapDestinationBaseDirConfig> destinationDirs = new ArrayList<>();

    @Nullable
    @Override
    public ColdSwapConfigurationStoreObject getState() {
        return this;
    }

    @Override
    public void loadState(ColdSwapConfigurationStoreObject coldSwapConfigurationStoreObject) {
        XmlSerializerUtil.copyBean(coldSwapConfigurationStoreObject, this);
    }

    public static ColdSwapConfigurationStoreObject getInstance(Project project) {
        return ServiceManager.getService(project, ColdSwapConfigurationStoreObject.class);
    }

    public List<ColdSwapDestinationBaseDirConfig> getDestinationDirs() {
        return destinationDirs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColdSwapConfigurationStoreObject that = (ColdSwapConfigurationStoreObject) o;

        return destinationDirs != null ? destinationDirs.equals(that.destinationDirs) : that.destinationDirs == null;
    }

    @Override
    public int hashCode() {
        return destinationDirs != null ? destinationDirs.hashCode() : 0;
    }
}
