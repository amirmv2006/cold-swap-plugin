package ir.amv.os.intellij.plugins.cold.swap.filter.impl;

import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import ir.amv.os.intellij.plugins.cold.swap.filter.IModuleOutputFilter;

import java.io.File;
import java.util.*;

public class ModuleOutputFilterByDateImpl
        implements IModuleOutputFilter<Date> {
    private Date date;

    public ModuleOutputFilterByDateImpl(Date date) {
        this.date = date;
    }

    @Override
    public void setKey(Date date) {
        this.date = date;
    }

    @Override
    public Map<String, VirtualFile> filterModuleOutput(Module module) {
        VirtualFile moduleOutputDirectory = CompilerPaths.getModuleOutputDirectory(module, false);
        Map<String, VirtualFile> result = new HashMap<>();
        if (moduleOutputDirectory != null) {
            VirtualFile[] children = moduleOutputDirectory.getChildren();
            for (VirtualFile child : children) {
                innerFilterRecursively("", child, result);
            }
        }
        return result;
    }
    private Logger logger = Logger.getInstance(ModuleOutputFilterByDateImpl.class);
    private void innerFilterRecursively(String relativePath, VirtualFile virtualFile, Map<String, VirtualFile> result) {
        if (relativePath.equals("")) {
            relativePath = virtualFile.getName();
        } else {
            relativePath = relativePath + "/" + virtualFile.getName();
        }
        if (virtualFile.isDirectory()) {
            VirtualFile[] children = virtualFile.getChildren();
            for (VirtualFile child : children) {
                innerFilterRecursively(relativePath, child, result);
            }
        } else {
            long lastModified = new File(virtualFile.getCanonicalPath()).lastModified();
            logger.warn(new Date(lastModified).toString());
            if (lastModified > date.getTime()) {
                result.put(relativePath, virtualFile);
            }
        }
    }
}
