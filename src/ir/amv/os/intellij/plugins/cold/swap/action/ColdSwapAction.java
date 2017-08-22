package ir.amv.os.intellij.plugins.cold.swap.action;

import com.intellij.compiler.impl.CompileContextImpl;
import com.intellij.compiler.progress.CompilerTask;
import com.intellij.history.Label;
import com.intellij.history.LocalHistory;
import com.intellij.history.LocalHistoryAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.compiler.CompileContext;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerPaths;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import ir.amv.os.intellij.plugins.cold.swap.destination.impl.DestinationExtractedTransferer;
import ir.amv.os.intellij.plugins.cold.swap.destination.impl.DestinationJarTransferer;
import ir.amv.os.intellij.plugins.cold.swap.filter.IModuleOutputFilter;
import ir.amv.os.intellij.plugins.cold.swap.filter.impl.ModuleOutputFilterByDateImpl;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        DestinationExtractedTransferer destinationExtractedTransferer = new DestinationExtractedTransferer(new File("D:/Video/temp"));
        DestinationJarTransferer destinationJarTransferer = new DestinationJarTransferer(new File("D:\\repos\\ir\\amv\\snippets"));
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
                                        for (String relPath : virtualFiles.keySet()) {
                                            logger.warn("Dirty" + relPath);
                                            destinationExtractedTransferer.transfer(module, relPath, virtualFiles.get(relPath));
                                            destinationJarTransferer.transfer(module, relPath, virtualFiles.get(relPath));
                                        }
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        OutputStream outputStream = child.getOutputStream(this);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
                        writer.write(simpleDateFormat.format(new Date()));
                        writer.flush();
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            last_deployed.finish();
        }
        logger.info("Action performed");
    }
}
