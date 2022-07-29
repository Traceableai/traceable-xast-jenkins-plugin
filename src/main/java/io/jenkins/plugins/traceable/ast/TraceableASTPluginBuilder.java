package io.jenkins.plugins.traceable.ast;

import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

public class TraceableASTPluginBuilder extends Builder implements SimpleBuildStep {

    private String scanName;
    private String testEnvironment;
    private String clientToken;
    private String traceableServer;

    @DataBoundConstructor
    public TraceableASTPluginBuilder(String scanName) {
        this.scanName = scanName;
    }

    public String getScanName() { return scanName; }
    public String getTestEnvironment() { return testEnvironment; }
    public String getClientToken() { return clientToken; }
    public String getTraceableServer() { return traceableServer; }


    @DataBoundSetter
    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    @DataBoundSetter
    public void setTestEnvironment(String testEnvironment) { this.testEnvironment = testEnvironment; }

    @DataBoundSetter
    public void setClientToken(String clientToken) { this.clientToken = clientToken; }

    @DataBoundSetter
    public void setTraceableServer(String traceableServer) { this.traceableServer = traceableServer; }


    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        listener.getLogger().println(scanName);
        listener.getLogger().println(clientToken);
        listener.getLogger().println(traceableServer);
        listener.getLogger().println(testEnvironment);
        run.addAction(new GenerateReportAction());
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String PLUGIN_NAME = "Traceable AST";
        public FormValidation doCheckName( @QueryParameter String scanName, @QueryParameter String testEnvironment,
                                           @QueryParameter String clientToken, @QueryParameter String traceableServer )
                throws IOException, ServletException {

            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return PLUGIN_NAME;
        }

    }

}
