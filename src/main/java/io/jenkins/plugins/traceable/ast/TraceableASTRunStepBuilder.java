package io.jenkins.plugins.traceable.ast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class TraceableASTRunStepBuilder extends Builder implements SimpleBuildStep {

    private String idleTimeout;
    private String maxRetries;
    private static String scanId;

    public String getIdleTimeout() {
        return idleTimeout;
    }

    public String getMaxRetries() {
        return maxRetries;
    }

    public static String getScanId() {
        return scanId;
    }

    @DataBoundConstructor
    public TraceableASTRunStepBuilder() {}

    @DataBoundSetter
    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @DataBoundSetter
    public void setMaxRetries(String maxRetries) {
        this.maxRetries = maxRetries;
    }

    public static void setScanId(String scanId) {
        TraceableASTRunStepBuilder.scanId = scanId;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        runScan(run, workspace, listener);
        if (scanId != null) {
            abortScan(workspace, listener);
        }
        TraceableASTInitStepBuilder.setScanEnded(true);
    }

    // Run the scan.
    private void runScan(Run<?, ?> run, FilePath workspace, TaskListener listener) {
        String scriptPath = "shell_scripts/run_ast_scan.sh";
        String[] args = new String[] {
            TraceableASTInitStepBuilder.getTraceableCliBinaryLocation(),
            TraceableASTInitStepBuilder.getClientToken().getPlainText(),
            idleTimeout,
            maxRetries,
            TraceableASTInitStepBuilder.getTraceableRootCaFileName(),
            TraceableASTInitStepBuilder.getTraceableCliCertFileName(),
            TraceableASTInitStepBuilder.getTraceableCliKeyFileName()
        };
        runScript(workspace, listener, scriptPath, args, "runScan");
    }

    // Stop the scan with the given scan ID.
    private void abortScan(FilePath workspace, TaskListener listener) {
        String scriptPath = "shell_scripts/stop_ast_scan.sh";
        String[] args = new String[] {TraceableASTInitStepBuilder.getTraceableCliBinaryLocation(), scanId};
        runScript(workspace, listener, scriptPath, args, "abortScan");
    }

    private void runScript(FilePath workspace, TaskListener listener, String scriptPath, String[] args, String caller) {
        try {
            String tempFilePath = workspace.act(new CopyScript(scriptPath));

            if (caller.equals("runScan")) {
                TraceableASTRunStepBuilder.setScanId(workspace.act(new RunScript(listener, tempFilePath, args, caller)));
            } else if (caller.equals("abortScan")) {
                workspace.act(new RunScript(listener, tempFilePath, args, caller));
            }

            deleteScript(workspace, listener, tempFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteScript(FilePath workspace, TaskListener listener, String scriptPath)
        throws IOException, InterruptedException {
        String[] command = {"rm", scriptPath};
        Launcher nodeLauncher = workspace.createLauncher(listener);

        nodeLauncher
            .launch()
            .cmds(command)
            .stdout(listener.getLogger())
            .stderr(listener.getLogger())
            .join();
    }

    private static final class CopyScript implements FileCallable<String> {

        private final String scriptPath;

        public CopyScript(String scriptPath) {
            this.scriptPath = scriptPath;
        }

        @Override
        public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {

            String bundledScript = CharStreams.toString(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(this.scriptPath)), Charsets.UTF_8));

            File tempFile = File.createTempFile(
                "script_" + this.scriptPath.replaceAll(".sh", "") + "_"
                    + UUID.randomUUID().toString(),
                ".sh");

            BufferedWriter x = com.google.common.io.Files.newWriter(tempFile, Charsets.UTF_8);
            x.write(bundledScript);
            x.close();

            return tempFile.getAbsolutePath();
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {
            return;
        }
    }

    private static final class RunScript implements FileCallable<String> {

        private final TaskListener listener;
        private final String scriptPath;
        private static String scanId = null;
        private String[] args;
        private final String caller;

        RunScript(TaskListener listener, String scriptPath, String[] args, String caller) {
            this.listener = listener;
            this.scriptPath = scriptPath;
            this.args = args;
            this.caller = caller;
        }

        @Override
        public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {

            List<String> command = new ArrayList<>();
            command.add("/bin/bash");
            command.add(scriptPath);

            for (int i = 0; i < args.length; i++) {
                if (!StringUtils.isEmpty(args[i])) {
                    args[i] = args[i].replace(" ", "");
                }

                if (args[i] != null && !args[i].isEmpty()) command.add(args[i]);
                else command.add("''");
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            logOutput(p.getInputStream(), "");
            logOutput(p.getErrorStream(), "Error: ");
            p.waitFor();

            return RunScript.scanId;
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {
            return;
        }

        private void logOutput(InputStream inputStream, String prefix) {
            new Thread(() -> {
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                while (scanner.hasNextLine()) {
                    synchronized (this) {
                        String line = scanner.nextLine();
                        // Extract the scan ID from the cli output of scan init command.
                        if (prefix.isEmpty() && line.contains("Running scan with ID")) {
                            String[] tokens = line.split(" ");
                            RunScript.scanId = tokens[tokens.length - 1].substring(0, 36);
                        }

                        if (!caller.equals("abortScan")) {
                            listener.getLogger().println(prefix + line);
                        }
                    }
                }
                scanner.close();
            })
                .start();
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private final String STEP_NAME = "Traceable AST - Run";

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return STEP_NAME;
        }
    }
}
