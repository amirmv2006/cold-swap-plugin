package ir.amv.os.intellij.plugins.cold.swap.destination;

import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapDestinationBaseDirConfig;

public interface IDestinationNameMatcher {

    boolean matches(String moduleName, String destinationName, ColdSwapDestinationBaseDirConfig destDir);

}
