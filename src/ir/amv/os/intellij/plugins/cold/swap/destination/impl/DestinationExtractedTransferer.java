package ir.amv.os.intellij.plugins.cold.swap.destination.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import ir.amv.os.intellij.plugins.cold.swap.destination.IDestinationTransferer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DestinationExtractedTransferer
        implements IDestinationTransferer {

    private File baseRootPath;

    public DestinationExtractedTransferer(File baseRootPath) {
        this.baseRootPath = baseRootPath;
    }

    @Override
    public void transfer(Module module, String fqn, VirtualFile virtualFile) {
        File searchResult = searchRec(baseRootPath, fqn, "");
        if (searchResult != null) {
            Logger.getInstance(DestinationExtractedTransferer.class).warn("should transfer " + virtualFile + " to " + searchResult);
            try {
                if (virtualFile.isDirectory()) {

                } else {
                    Files.copy(virtualFile.getInputStream(), Paths.get(searchResult.toURI()), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File searchRec(File file, String fqn, String matchedFqn) {
        if (file.isDirectory()) {
            int indexOf = fqn.indexOf("/");
            if (indexOf != -1) {
                String firstPart = fqn.substring(0, indexOf);
                if (file.getName().equals(firstPart)) {
                    matchedFqn = (matchedFqn.equals("") ? "" : (matchedFqn + "/")) + firstPart;
                    fqn = fqn.substring(indexOf + 1);
                }
            }
            File[] children = file.listFiles();
            for (File child : children) {
                File searchRec = searchRec(child, fqn, matchedFqn);
                if (searchRec != null) {
                    return searchRec;
                }
            }
        } else {
            if (file.getName().equals(fqn)) {
                return file;
            }
        }
        return null;
    }
}
