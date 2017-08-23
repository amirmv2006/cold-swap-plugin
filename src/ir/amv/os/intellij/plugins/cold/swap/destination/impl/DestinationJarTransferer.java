package ir.amv.os.intellij.plugins.cold.swap.destination.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;
import ir.amv.os.intellij.plugins.cold.swap.destination.IDestinationNameMatcher;
import ir.amv.os.intellij.plugins.cold.swap.destination.IDestinationTransferer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
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
    public void transfer(Module module, String fqn, VirtualFile virtualFile) {
        SearchResult searchResult = null;
        try {
            searchResult = searchRec(baseRootPath, fqn, module);
            if (searchResult != null) {
                Logger.getInstance(DestinationExtractedTransferer.class).warn("should transfer " + virtualFile + " to " + searchResult.file + " -> " + searchResult.jarEntry);
                if (virtualFile.isDirectory()) {

                } else {
                    updateJarFile(searchResult.file, Arrays.asList(new JarModification(JarModification.ModificationType.update, searchResult.jarEntry, virtualFile)));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SearchResult searchRec(File file, String fqn, Module module) throws IOException {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File child : children) {
                SearchResult searchRec = searchRec(child, fqn, module);
                if (searchRec != null) {
                    return searchRec;
                }
            }
        } else {
            if (file.getName().toLowerCase().endsWith(".jar") && nameMatcher.matches(module.getName(), file.getName())) {
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

        public SearchResult(JarEntry jarEntry, File file) {
            this.jarEntry = jarEntry;
            this.file = file;
        }
    }

    private static class JarModification {
        private enum ModificationType {
            add,
            delete,
            update
        }

        private ModificationType modificationType;
        private JarEntry jarEntry; // existing zip entry
        private VirtualFile newFile; // newFileContent

        public JarModification(ModificationType modificationType, JarEntry jarEntry, VirtualFile newFile) {
            this.modificationType = modificationType;
            this.jarEntry = jarEntry;
            this.newFile = newFile;
        }
    }

    public static void updateJarFile(File srcJarFile, List<JarModification> modifications) throws IOException {
        File srcJarTmp = new File(srcJarFile.getCanonicalPath() + ".bak");
        Files.copy(new FileInputStream(srcJarFile), Paths.get(srcJarTmp.toURI()));
        File tmpJarFile = new File(srcJarFile.getCanonicalPath());
        JarFile jarFile = new JarFile(srcJarTmp);
        boolean jarUpdated = false;

        try {
            JarOutputStream tempJarOutputStream = new JarOutputStream(new FileOutputStream(tmpJarFile));

            try {
                //Copy original jar file to the temporary one.
                Enumeration jarEntries = jarFile.entries();
                while (jarEntries.hasMoreElements()) {
                    JarEntry entry = (JarEntry) jarEntries.nextElement();
                    boolean skip = false;
                    for (JarModification modification : modifications) {
                        if (modification.jarEntry.getName().equals(entry.getName())) {
                            if (modification.modificationType.equals(JarModification.ModificationType.delete) ||
                                    modification.modificationType.equals(JarModification.ModificationType.update)) {
                                skip = true;
                                break;
                            }
                        }
                    }
                    if (skip) {
                        continue;
                    }
                    InputStream entryInputStream = jarFile.getInputStream(entry);
                    tempJarOutputStream.putNextEntry(entry);
                    byte[] buffer = new byte[1024];
                    int bytesRead = 0;
                    while ((bytesRead = entryInputStream.read(buffer)) != -1) {
                        tempJarOutputStream.write(buffer, 0, bytesRead);
                    }
                }
                //Added the new files to the jar.
                for (JarModification modification : modifications) {
                    if (modification.modificationType.equals(JarModification.ModificationType.update) ||
                            modification.modificationType.equals(JarModification.ModificationType.add)) {
                        InputStream fis = modification.newFile.getInputStream();
                        try {
                            byte[] buffer = new byte[1024];
                            int bytesRead = 0;
                            JarEntry entry = new JarEntry(modification.jarEntry.getName());
                            tempJarOutputStream.putNextEntry(entry);
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                tempJarOutputStream.write(buffer, 0, bytesRead);
                            }
                        } finally {
                            fis.close();
                        }
                    }
                }
                jarUpdated = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                tempJarOutputStream.putNextEntry(new JarEntry("stub"));
            } finally {
                tempJarOutputStream.close();
            }

        } finally {
            jarFile.close();
        }

        srcJarTmp.delete();
    }
}
