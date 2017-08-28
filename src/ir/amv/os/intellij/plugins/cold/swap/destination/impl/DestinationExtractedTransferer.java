package ir.amv.os.intellij.plugins.cold.swap.destination.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import ir.amv.os.intellij.plugins.cold.swap.action.ColdSwapAction;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapDestinationBaseDirConfig;
import ir.amv.os.intellij.plugins.cold.swap.destination.IDestinationTransferer;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

public class DestinationExtractedTransferer
        implements IDestinationTransferer {

    private File baseRootPath;

    public DestinationExtractedTransferer(File baseRootPath) {
        this.baseRootPath = baseRootPath;
    }

    @Override
    public void transfer(Module module, String fqn, VirtualFile virtualFile, ColdSwapDestinationBaseDirConfig destDir, Consumer<String> logger) {
        File searchResult = searchRec(baseRootPath, fqn, "");
        if (searchResult != null) {
            Logger.getInstance(DestinationExtractedTransferer.class).warn("should transfer " + virtualFile + " to " + searchResult);
            try {
                if (virtualFile.isDirectory()) {
                    VirtualFile[] children = virtualFile.getChildren();
                    File[] destChildren = searchResult.listFiles();
                    for (File destChild : destChildren) {
                        boolean exists = false;
                        for (VirtualFile child : children) {
                            if (child.getName().equals(destChild.getName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists && !ColdSwapAction.shouldBeExcluded(destChild.getName(), destDir.getExclusions())) {
                            try {
                                if (destChild.isDirectory()) {
                                    deleteDirectory(destChild);
                                } else {
                                    Files.delete(Paths.get(destChild.toURI()));
                                }
                                logger.accept("[ExtractedAction]\tRemoved\t\t" + destChild.getCanonicalPath());
                            } catch (IOException e) {
                            }
                        }
                    }
                    for (VirtualFile child : children) {
                        boolean exists = false;
                        for (File destChild : destChildren) {
                            if (destChild.getName().equals(child.getName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists && !ColdSwapAction.shouldBeExcluded(child.getName(), destDir.getExclusions())) {
                            try {
                                Path newFilePath = Paths.get(URI.create(searchResult.toURI() + child.getName()));
                                if (child.isDirectory()) {
                                    Files.createDirectory(newFilePath);
                                    logger.accept("[ExtractedAction]\tAdded\t\t" + newFilePath);
                                } else {
                                    newFilePath = Files.createFile(newFilePath);
                                    Files.copy(child.getInputStream(), newFilePath, StandardCopyOption.REPLACE_EXISTING);
                                    logger.accept("[ExtractedAction]\tAdded\t\t" + newFilePath);
                                }
                            } catch (IOException e) {
                            }
                        }
                    }
                } else {
                    Files.copy(virtualFile.getInputStream(), Paths.get(searchResult.toURI()), StandardCopyOption.REPLACE_EXISTING);
                    logger.accept("[ExtractedAction]\tUpdated\t\t" + searchResult.getCanonicalPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* * Right way to delete a non empty directory in Java */
    private static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        } // either file or an empty directory
        return dir.delete();
    }

    private File searchRec(File file, String fqn, String matchedFqn) {
        if (file.getName().equals(fqn)) {
            return file;
        }
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
        }
        return null;
    }
}
