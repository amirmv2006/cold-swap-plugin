package ir.amv.os.intellij.plugins.cold.swap.destination.impl;

import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapDestinationBaseDirConfig;
import ir.amv.os.intellij.plugins.cold.swap.destination.IDestinationNameMatcher;

public class DestinationNameApproximateMatcher
        implements IDestinationNameMatcher {

    @Override
    public boolean matches(String moduleName, String destinationName, ColdSwapDestinationBaseDirConfig destDir) {
        String destinationNameTransformed = transform(destinationName, destDir);
        String moduleNameTransformed = transform(moduleName, destDir);
        return moduleNameTransformed.equalsIgnoreCase(destinationNameTransformed);
    }

    private String transform(String destinationName, ColdSwapDestinationBaseDirConfig destDir) {
        if (destinationName.toLowerCase().endsWith(".jar")) {
            destinationName = destinationName.substring(0, destinationName.length() - ".jar".length());
        }
        for (String regex : destDir.getRegexToBeRemoved()) {
            destinationName = destinationName.replaceAll(regex, "");
        }
        return destinationName;
    }

}
