package ir.amv.os.intellij.plugins.cold.swap.action;

import com.intellij.history.LocalHistory;
import com.intellij.history.LocalHistoryAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
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

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ColdSwapAction
        extends AnAction {

    private Logger logger = Logger.getInstance(ColdSwapAction.class);
    private IModuleOutputFilter<?> moduleOutputFilter;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
    ;

    public ColdSwapAction() {
        logger.info("Cold Swap Action initialized");
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project openProject : openProjects) {
            LocalHistoryAction last_deployed = LocalHistory.getInstance().startAction("Last Deployed");
            CompilerManager compilerManager = CompilerManager.getInstance(openProject);
            CompileScope projectCompileScope = compilerManager.createProjectCompileScope(openProject);
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
                                try (InputStream inputStream = child.getInputStream()) {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                                    moduleOutputFilter = new ModuleOutputFilterByDateImpl(simpleDateFormat.parse(reader.readLine()));
                                    for (Module module : modules) {
                                        Map<String, VirtualFile> virtualFiles = moduleOutputFilter.filterModuleOutput(module);
                                        List<String> strings = new ArrayList<>(virtualFiles.keySet());
                                        Collections.sort(strings);
                                        for (String relPath : strings) {
                                            transfer(openProject, module, relPath, virtualFiles.get(relPath));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        // FIXME re-enable writing to the file
//                        OutputStream outputStream = child.getOutputStream(this);
//                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
//                        writer.write(simpleDateFormat.format(new Date()));
//                        writer.flush();
//                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            last_deployed.finish();
        }
        logger.info("Action performed");
    }

    private void transfer(Project project, Module module, String relPath, VirtualFile virtualFile) {
        ColdSwapConfigurationStoreObject instance = ColdSwapConfigurationStoreObject.getInstance(project);
        List<ColdSwapDestinationBaseDirConfig> destinationDirs = instance.getDestinationDirs();
        for (ColdSwapDestinationBaseDirConfig destinationDir : destinationDirs) {
            switch (destinationDir.getType()) {
                case JAR:
                    new DestinationJarTransferer(new File(destinationDir.getBaseDirPath())).transfer(module, relPath, virtualFile);
                    break;
                case EXTRACTED:
                    new DestinationExtractedTransferer(new File(destinationDir.getBaseDirPath())).transfer(module, relPath, virtualFile);
                    break;
                default:
                    break;
            }
        }
    }
}
