package ir.amv.os.intellij.plugins.cold.swap.destination.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import ir.amv.os.intellij.plugins.cold.swap.action.ColdSwapAction;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapDestinationBaseDirConfig;
import ir.amv.os.intellij.plugins.cold.swap.destination.IDestinationNameMatcher;
import ir.amv.os.intellij.plugins.cold.swap.destination.IDestinationTransferer;
import ir.amv.os.intellij.plugins.cold.swap.tools.ExceptionStackTraceWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class DestinationJarTransferer
        implements IDestinationTransferer {

    private File baseRootPath;
    private IDestinationNameMatcher nameMatcher = new DestinationNameApproximateMatcher();

    public DestinationJarTransferer(File baseRootPath) {
        this.baseRootPath = baseRootPath;
    }

    @Override
    public void transfer(Module module, String fqn, VirtualFile virtualFile, ColdSwapDestinationBaseDirConfig destDir, Consumer<String> logger) {
        SearchResult searchResult;
        try {
            searchResult = searchRec(baseRootPath, fqn, module, destDir);
            if (searchResult != null) {
                Logger.getInstance(DestinationExtractedTransferer.class).warn("should transfer " + virtualFile + " to " + searchResult.file + " -> " + searchResult.jarEntry);
                List<JarModification> modifications = new ArrayList<>();
                if (virtualFile.isDirectory()) {
                    VirtualFile[] children = virtualFile.getChildren();
                    List<JarEntry> destChildren = getJarEntriesUnder(searchResult.file, searchResult.jarEntry.getName());
                    for (JarEntry destChild : destChildren) {
                        boolean exists = false;
                        for (VirtualFile child : children) {
                            if (destChild.getName().substring(0, destChild.getName().length() - 1).endsWith(child.getName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists && !ColdSwapAction.shouldBeExcluded(destChild.getName(), destDir.getExclusions())) {
                            modifications.add(new JarModification(JarModification.ModificationType.delete, destChild, null));
                        }
                    }
                    for (VirtualFile child : children) {
                        boolean exists = false;
                        for (JarEntry destChild : destChildren) {
                            if (destChild.getName().substring(0, destChild.getName().length() - 1).endsWith(child.getName())) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists && !ColdSwapAction.shouldBeExcluded(child.getName(), destDir.getExclusions())) {
                            modifications.add(new JarModification(JarModification.ModificationType.add, new JarEntry(fqn + "/" + child.getName()), child));
                        }
                    }
                } else {
                    modifications.add(new JarModification(JarModification.ModificationType.update, searchResult.jarEntry, virtualFile));
                }
                if (!modifications.isEmpty()) {
                    updateJarFile(searchResult.file, modifications, logger);
                }
            }
        } catch (IOException e) {
            ExceptionStackTraceWriter.printStackTrace(e, logger);
        }
    }

    private List<JarEntry> getJarEntriesUnder(File file, String parent) throws IOException {
        List<JarEntry> result = new ArrayList<>();
        try (JarFile jarFile = new JarFile(file)) {
            Enumeration jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry entry = (JarEntry) jarEntries.nextElement();
                if (entry.getName().startsWith(parent) && !entry.getName().equals(parent)) {
                    String substring = entry.getName().substring(parent.length());
                    substring = substring.substring(0, substring.length() - 1);
                    if (!substring.contains("/")) {
                        result.add(entry);
                    }
                }
            }
        }
        return result;
    }

    private SearchResult searchRec(File file, String fqn, Module module, ColdSwapDestinationBaseDirConfig destDir) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            assert children != null;
            for (File child : children) {
                SearchResult searchRec = searchRec(child, fqn, module, destDir);
                if (searchRec != null) {
                    return searchRec;
                }
            }
        } else {
            if (file.getName().toLowerCase().endsWith(".jar") && nameMatcher.matches(module.getName(), file.getName(), destDir)) {
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (jarEntry.getName().equals(fqn) || jarEntry.getName().equals(fqn + "/")) {
                        return new SearchResult(jarEntry, file);
                    }
                }
            }
        }
        return null;
    }

    private static class SearchResult {
        private JarEntry jarEntry;
        private File file;

        SearchResult(JarEntry jarEntry, File file) {
            this.jarEntry = jarEntry;
            this.file = file;
        }
    }

    private static class JarModification {
        private enum ModificationType {
            add,
            delete,
            update;

            public String log() {
                switch (this) {
                    case add: return    "Inserting";
                    case delete: return "Deleting";
                    case update: return "Updating";
                }
                return super.toString();
            }
        }

        private ModificationType modificationType;
        private JarEntry jarEntry; // existing zip entry
        private VirtualFile newFile; // newFileContent

        JarModification(ModificationType modificationType, JarEntry jarEntry, VirtualFile newFile) {
            this.modificationType = modificationType;
            this.jarEntry = jarEntry;
            this.newFile = newFile;
        }
    }

    private static void updateJarFile(File srcJarFile, List<JarModification> modifications, Consumer<String> logger) throws IOException {
        File srcJarTmp = new File(srcJarFile.getCanonicalPath() + ".bak");
        Files.copy(new FileInputStream(srcJarFile), Paths.get(srcJarTmp.toURI()));
        File tmpJarFile = new File(srcJarFile.getCanonicalPath());
        try (JarFile jarFile = new JarFile(srcJarTmp)) {
            JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tmpJarFile));

            try {
                //Copy original jar file to the temporary one.
                Enumeration jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) jarEntries.nextElement();
                    boolean skip = false;
                    for (JarModification modification : modifications) {
                        if (entry.getName().startsWith(modification.jarEntry.getName())) {
                            if (modification.modificationType.equals(JarModification.ModificationType.delete)) {
                                skip = true;
                                logger.accept("[JarUpdateAction]\tRemoving\t" + entry.getName() + " from " + srcJarFile.getCanonicalPath());
                                break;
                            }
                        }
                        if (entry.getName().equals(modification.jarEntry.getName())) {
                            if (modification.modificationType.equals(JarModification.ModificationType.update)) {
                                skip = true;
                                break;
                            }
                        }
                    }
                    if (skip) {
                        continue;
                    }
                    InputStream entryInputStream = jarFile.getInputStream(entry);
                    int bytesRead;
                    byte[] buffer = new byte[1024];
                    bytesRead = entryInputStream.read(buffer);
                    tempJarOutputStream.putNextEntry(new JarEntry(entry.getName()));
                    while (bytesRead != -1) {
                        tempJarOutputStream.write(buffer, 0, bytesRead);
                        bytesRead = entryInputStream.read(buffer);
                    }
                }
                //Added the new files to the jar.
                for (JarModification modification : modifications) {
                    if (modification.modificationType.equals(JarModification.ModificationType.update) ||
                            modification.modificationType.equals(JarModification.ModificationType.add)) {
                        String entryName = modification.jarEntry.getName();
                        if (modification.newFile.isDirectory() && !entryName.endsWith("/")) {
                            entryName += "/";
                        }
                        JarEntry entry = new JarEntry(entryName);
                        tempJarOutputStream.putNextEntry(entry);
                        if (!modification.newFile.isDirectory()) {
                            try (InputStream fis = modification.newFile.getInputStream()) {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = fis.read(buffer)) != -1) {
                                    tempJarOutputStream.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                        tempJarOutputStream.closeEntry();
                        logger.accept("[JarUpdateAction]\t" + modification.modificationType.log() + "\t" + modification.jarEntry.getName() + " in " + srcJarFile.getCanonicalPath());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                tempJarOutputStream.putNextEntry(new JarEntry("stub"));
            } finally {
                tempJarOutputStream.close();
            }
        }
        srcJarTmp.delete();
    }
}
