package ir.amv.os.intellij.plugins.cold.swap.destination;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;
import java.util.function.Consumer;

public interface IDestinationTransferer {

    void transfer(Module module, String fqn, VirtualFile virtualFile, List<String> exclusions, Consumer<String> logger);
}
