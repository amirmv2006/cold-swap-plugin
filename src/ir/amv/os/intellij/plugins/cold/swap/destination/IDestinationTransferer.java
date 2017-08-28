package ir.amv.os.intellij.plugins.cold.swap.destination;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapDestinationBaseDirConfig;

import java.util.function.Consumer;

public interface IDestinationTransferer {

    void transfer(Module module, String fqn, VirtualFile virtualFile, ColdSwapDestinationBaseDirConfig exclusions, Consumer<String> logger);
}
