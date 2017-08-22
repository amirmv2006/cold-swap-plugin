package ir.amv.os.intellij.plugins.cold.swap.destination.impl;

import ir.amv.os.intellij.plugins.cold.swap.destination.IDestinationNameMatcher;

public class DestinationNameApproximateMatcher
        implements IDestinationNameMatcher {

    @Override
    public boolean matches(String moduleName, String destinationName) {
        String destinationNameTransformed = transform(destinationName);
        String moduleNameTransformed = transform(destinationName);
        return moduleNameTransformed.equalsIgnoreCase(destinationNameTransformed);
    }

    private String transform(String destinationName) {
        if (destinationName.toLowerCase().endsWith(".jar")) {
            destinationName = destinationName.substring(0, destinationName.length() - ".jar".length());
        }
        return destinationName.replaceAll("[^a-zA-Z]", "");
    }

}
