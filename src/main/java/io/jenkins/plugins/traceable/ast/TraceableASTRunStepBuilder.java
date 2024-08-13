package io.jenkins.plugins.traceable.ast;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import io.jenkins.plugins.traceable.ast.scan.utils.RunScript;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
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

            String returnValue = workspace.act(new RunScript(listener, scriptPath, args, caller));

            if (caller.equals("runScan")) {
                TraceableASTRunStepBuilder.setScanId(returnValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
