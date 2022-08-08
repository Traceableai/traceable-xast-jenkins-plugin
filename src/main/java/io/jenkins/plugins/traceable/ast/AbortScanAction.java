package io.jenkins.plugins.traceable.ast;

import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.model.RunAction2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public class AbortScanAction implements RunAction2 {
    private String scanId;
    private TaskListener listener;

    // Constructor
    public  AbortScanAction(String scanId, TaskListener listener) {
        this.scanId = scanId;
        this.listener = listener;
    }

    @Override
    public void onAttached(Run<?, ?> r) {

        try {
        ProcessBuilder pb = new ProcessBuilder(
                "src/main/resources/io/jenkins/plugins/traceable/ast/TraceableASTPluginBuilder/shell_scripts/stop_ast_scan.sh",
                scanId
        );
        Process stopAstScan = pb.start();
        logOutput(stopAstScan.getInputStream(), "",listener);
        logOutput(stopAstScan.getErrorStream(), "Error: ",listener);
        stopAstScan.waitFor();
        } catch (IOException | InterruptedException e) {
           e.printStackTrace();
        }
    }

    @Override
    public void onLoad(Run<?, ?> r) {}

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return null;
    }

    private void logOutput(InputStream inputStream, String prefix, TaskListener listener) {
        new Thread(() -> {
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            while (scanner.hasNextLine()) {
                synchronized (this) {
                    String line = scanner.nextLine();
                    listener.getLogger().println(prefix + line);
                }
            }
            scanner.close();
        }).start();
    }
}
