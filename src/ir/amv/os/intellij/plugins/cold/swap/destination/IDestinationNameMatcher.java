package ir.amv.os.intellij.plugins.cold.swap.destination;

public interface IDestinationNameMatcher {

    boolean matches(String moduleName, String destinationName);

}
