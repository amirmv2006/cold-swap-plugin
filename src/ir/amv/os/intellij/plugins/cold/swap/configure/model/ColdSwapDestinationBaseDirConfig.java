package ir.amv.os.intellij.plugins.cold.swap.configure.model;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.List;

public class ColdSwapDestinationBaseDirConfig {

    public enum DestinationType {
        Jar,
        Extracted
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

    @Override
    public String toString() {
        String result = baseDirPath;
        if (exclusions != null && !exclusions.isEmpty()) {
            result +=  ", excluding " + exclusions;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColdSwapDestinationBaseDirConfig that = (ColdSwapDestinationBaseDirConfig) o;

        if (baseDirPath != null ? !baseDirPath.equals(that.baseDirPath) : that.baseDirPath != null) return false;
        if (exclusions != null ? !exclusions.equals(that.exclusions) : that.exclusions != null) return false;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        int result = baseDirPath != null ? baseDirPath.hashCode() : 0;
        result = 31 * result + (exclusions != null ? exclusions.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
