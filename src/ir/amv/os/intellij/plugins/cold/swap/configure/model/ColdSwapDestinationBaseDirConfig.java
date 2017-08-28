package ir.amv.os.intellij.plugins.cold.swap.configure.model;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    @Tag("regexToBeRemoved")
    @AbstractCollection(surroundWithTag = false, elementTag = "regex")
    private List<String> regexToBeRemoved = new ArrayList<>(Collections.singletonList("[^a-zA-Z]"));

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

    public List<String> getRegexToBeRemoved() {
        return regexToBeRemoved;
    }

    public void setRegexToBeRemoved(List<String> regexToBeRemoved) {
        this.regexToBeRemoved = regexToBeRemoved;
    }

    @Override
    public String toString() {
        String result = baseDirPath;
        if (exclusions != null && !exclusions.isEmpty()) {
            result +=  ", excluding " + exclusions;
        }
        result += ", regexToBeRemoved " + regexToBeRemoved;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColdSwapDestinationBaseDirConfig that = (ColdSwapDestinationBaseDirConfig) o;

        if (baseDirPath != null ? !baseDirPath.equals(that.baseDirPath) : that.baseDirPath != null) return false;
        if (exclusions != null ? !exclusions.equals(that.exclusions) : that.exclusions != null) return false;
        if (type != that.type) return false;
        return regexToBeRemoved != null ? regexToBeRemoved.equals(that.regexToBeRemoved) : that.regexToBeRemoved == null;
    }

    @Override
    public int hashCode() {
        int result = baseDirPath != null ? baseDirPath.hashCode() : 0;
        result = 31 * result + (exclusions != null ? exclusions.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (regexToBeRemoved != null ? regexToBeRemoved.hashCode() : 0);
        return result;
    }
}
