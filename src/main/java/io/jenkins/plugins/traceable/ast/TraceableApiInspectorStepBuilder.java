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
import hudson.util.Secret;
import io.jenkins.plugins.traceable.ast.scan.utils.ApiInspector;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@Slf4j
public class TraceableApiInspectorStepBuilder extends Builder implements SimpleBuildStep {

    private String repoPath;
    private String specFilePath;
    private String traceableServer;
    private static Secret traceableToken;
    private StringBuilder report;

    public String getRepoPath() {
        return repoPath;
    }

    public String getSpecFilePath() {
        return specFilePath;
    }

    public String getTraceableServer() {
        return traceableServer;
    }

    public static Secret getTraceableToken() {
        return traceableToken;
    }

    @DataBoundSetter
    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    @DataBoundSetter
    public void setSpecFilePath(String specFilePath) {
        this.specFilePath = specFilePath;
    }

    @DataBoundSetter
    public void setTraceableServer(String traceableServer) {
        this.traceableServer = traceableServer;
    }

    @DataBoundSetter
    public static void setTraceableToken(Secret traceableToken) {
        TraceableApiInspectorStepBuilder.traceableToken = traceableToken;
    }

    @DataBoundConstructor
    public TraceableApiInspectorStepBuilder() {}

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        report = new StringBuilder();
        String scriptPath = "shell_scripts/api_inspector.sh";
        String repoWorkspacePath = workspace.getRemote();

        if (!StringUtils.isBlank(repoPath)) {
            repoWorkspacePath += repoPath;
        }

        if (StringUtils.isBlank(specFilePath)) {
            findAndInspect(workspace, listener, repoWorkspacePath, scriptPath);
        } else {
            String absoluteSpecFilePath = repoWorkspacePath + "/" + specFilePath;
            String[] args = new String[] {traceableServer, traceableToken.getPlainText(), absoluteSpecFilePath};
            report.append(runScript(workspace, listener, scriptPath, args, true));
        }

        run.addAction(new TraceableApiInspectorReportAction(report.toString()));
    }

    private void findAndInspect(
            FilePath workspace, TaskListener listener, String repoWorkspacePath, String scriptPath) {
        String findSpecScriptPath = "shell_scripts/spec_finder.sh";
        String[] workspaceArgs = {repoWorkspacePath};

        String specFilePaths = runScript(workspace, listener, findSpecScriptPath, workspaceArgs, false);
        String[] specFilePathsList = specFilePaths.split("\n");

        if (specFilePathsList.length == 0) {
            listener.getLogger().println("No Open Api Spec Found at " + repoWorkspacePath);
        } else {
            listener.getLogger().println("Found open api specs at " + repoWorkspacePath + " : \n" + specFilePaths);
        }

        for (String specFile : specFilePathsList) {
            String[] args = new String[] {traceableServer, traceableToken.getPlainText(), specFile};
            String newOpenApiSpecString =
                    "========================================\nUploading open api spec : " + specFile + "\n";
            listener.getLogger().println(newOpenApiSpecString);
            report.append(newOpenApiSpecString);
            report.append(runScript(workspace, listener, scriptPath, args, true));
        }
    }

    private String runScript(
            FilePath workspace, TaskListener listener, String scriptPath, String[] args, boolean printLogsToConsole) {
        try {
            return workspace.act(new ApiInspector(listener, scriptPath, args, printLogsToConsole));
        } catch (Exception e) {
            log.error("Exception in running {} script : {}", scriptPath, e);
            e.printStackTrace();
        }
        return "";
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private final String STEP_NAME = "Traceable API Inspector";

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
