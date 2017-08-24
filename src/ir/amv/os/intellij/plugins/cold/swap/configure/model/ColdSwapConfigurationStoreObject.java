package ir.amv.os.intellij.plugins.cold.swap.configure.model;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
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
}
