package ir.amv.os.intellij.plugins.cold.swap.action;

import com.intellij.history.LocalHistory;
import com.intellij.history.LocalHistoryAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapConfigurationStoreObject;
import ir.amv.os.intellij.plugins.cold.swap.configure.model.ColdSwapDestinationBaseDirConfig;
import ir.amv.os.intellij.plugins.cold.swap.destination.impl.DestinationExtractedTransferer;
import ir.amv.os.intellij.plugins.cold.swap.destination.impl.DestinationJarTransferer;
import ir.amv.os.intellij.plugins.cold.swap.filter.IModuleOutputFilter;
import ir.amv.os.intellij.plugins.cold.swap.filter.impl.ModuleOutputFilterByDateImpl;
import ir.amv.os.intellij.plugins.cold.swap.tools.ExceptionStackTraceWriter;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class ColdSwapAction
        extends AnAction {

    private Logger logger = Logger.getInstance(ColdSwapAction.class);
    private IModuleOutputFilter<?> moduleOutputFilter;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss[SSS]");
    ;

    public ColdSwapAction() {
        logger.info("Cold Swap Action initialized");
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project openProject : openProjects) {
            LocalHistoryAction last_deployed = LocalHistory.getInstance().startAction("Last Deployed");
            Module[] modules = ModuleManager.getInstance(openProject).getModules();
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    String name = "last.deply.txt";
                    VirtualFile baseDir = openProject.getBaseDir();
                    VirtualFile child = baseDir.findChild(name);
                    try {
                        if (child == null) {
                            child = baseDir.createChildData(this, name);
                        } else {
                            try {
                                String lastModify = null;
                                try (InputStream inputStream = child.getInputStream()) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                    lastModify = reader.readLine();
                                    if (lastModify != null) {
                                        try {
                                            moduleOutputFilter = new ModuleOutputFilterByDateImpl(simpleDateFormat.parse(lastModify));
                                        } catch (ParseException e) {
                                            logger.error("Unable to parse date", e);
                                        }
                                    }
                                }
                                try (OutputStream outputStream = child.getOutputStream(this)){
                                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                                    writer.write(simpleDateFormat.format(new Date()) + System.lineSeparator());
                                    writer.flush();
                                    if (moduleOutputFilter != null) {
                                        for (Module module : modules) {
                                            Map<String, VirtualFile> virtualFiles = moduleOutputFilter.filterModuleOutput(module);
                                            List<String> strings = new ArrayList<>(virtualFiles.keySet());
                                            Collections.sort(strings);
                                            for (String relPath : strings) {
                                                transfer(openProject, module, relPath, virtualFiles.get(relPath), s -> {
                                                    try {
                                                        writer.write(s + System.lineSeparator());
                                                        writer.flush();
                                                    } catch (IOException e) {
                                                        ExceptionStackTraceWriter.printStackTrace(e, s1 -> logger.error("Error writing", e));
                                                    }
                                                });
                                            }
                                        }
                                    } else {
                                        writer.write("No previous deploy found. Considering deploy is already up to date");
                                        writer.flush();
                                    }
                                }
                            } catch (Exception e) {
                                ExceptionStackTraceWriter.printStackTrace(e, s -> logger.error("Unknnown Error", e));
                            }
                        }
                    } catch (IOException e) {
                        ExceptionStackTraceWriter.printStackTrace(e, s -> logger.error("Unknnown Error", e));
                    }
                }
            });
            last_deployed.finish();
        }
        logger.info("Action performed");
    }

    private void transfer(Project project, Module module, String relPath, VirtualFile virtualFile, Consumer<String> logger) {
        ColdSwapConfigurationStoreObject instance = ColdSwapConfigurationStoreObject.getInstance(project);
        List<ColdSwapDestinationBaseDirConfig> destinationDirs = instance.getDestinationDirs();
        for (ColdSwapDestinationBaseDirConfig destinationDir : destinationDirs) {
            List<String> exclusions = destinationDir.getExclusions();
            if (shouldBeExcluded(virtualFile.getName(), exclusions)) {
                continue;
            }
            switch (destinationDir.getType()) {
                case Jar:
                    new DestinationJarTransferer(new File(destinationDir.getBaseDirPath())).transfer(module, relPath, virtualFile, destinationDir, logger);
                    break;
                case Extracted:
                    new DestinationExtractedTransferer(new File(destinationDir.getBaseDirPath())).transfer(module, relPath, virtualFile, destinationDir, logger);
                    break;
                default:
                    break;
            }
        }
    }

    public static boolean shouldBeExcluded(String fileName, List<String> exclusions) {
        boolean shouldBeExcluded = false;
        if (exclusions != null) {
            for (String exclusion : exclusions) {
                exclusion = exclusion.replace("*", "");
                if (fileName.endsWith(exclusion)) {
                    shouldBeExcluded = true;
                    break;
                }
            }
        }
        return shouldBeExcluded;
    }
}
