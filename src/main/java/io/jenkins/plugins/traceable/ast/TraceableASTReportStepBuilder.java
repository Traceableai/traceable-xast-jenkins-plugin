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
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;


public class TraceableASTReportStepBuilder extends Builder implements SimpleBuildStep {
    private String clientToken;

    @DataBoundConstructor
    public TraceableASTReportStepBuilder(String clientToken) {
        this.clientToken = clientToken;
    }

    public String getClientToken() {
        return clientToken;
    }

    @DataBoundSetter
    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        if (TraceableASTInitAndRunStepBuilder.getClientToken() != null) {
            while (TraceableASTInitAndRunStepBuilder.getScanEnded() == null || !TraceableASTInitAndRunStepBuilder.getScanEnded()) {
                Thread.sleep(15000);
                listener.getLogger().println("Scan Running waiting for scan to end");
            }
            if(TraceableASTInitAndRunStepBuilder.getScanId() == null) { return; }
        }
        else if(TraceableASTInitStepBuilder.getClientToken() != null) {
            while (TraceableASTInitStepBuilder.getScanEnded() == null || !TraceableASTInitStepBuilder.getScanEnded()) {}
            if(TraceableASTInitStepBuilder.getScanId() == null) { return; }
        }

        run.addAction(new TraceableASTGenerateReportAction(clientToken));
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String STEP_NAME = "Traceable AST - Generate Report";

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

