package ir.amv.os.intellij.plugins.cold.swap.filter;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Map;

public interface IModuleOutputFilter<Key> {

    void setKey(Key key);

    Map<String, VirtualFile> filterModuleOutput(Module module);
}
