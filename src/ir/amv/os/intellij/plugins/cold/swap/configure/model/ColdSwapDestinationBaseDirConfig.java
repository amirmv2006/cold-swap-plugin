package ir.amv.os.intellij.plugins.cold.swap.configure.model;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.List;

public class ColdSwapDestinationBaseDirConfig {

    public enum DestinationType {
        JAR,
        EXTRACTED
    }

    private String baseDirPath;
    @Tag("exclusions")
    @AbstractCollection(surroundWithTag = false, elementTag = "pattern")
    private List<String> exclusions;
    private DestinationType type;

    public String getBaseDirPath() {
        return baseDirPath;
    }

    public void setBaseDirPath(String baseDirPath) {
        this.baseDirPath = baseDirPath;
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<String> exclusions) {
        this.exclusions = exclusions;
    }

    public DestinationType getType() {
        return type;
    }

    public void setType(DestinationType type) {
        this.type = type;
    }
}
