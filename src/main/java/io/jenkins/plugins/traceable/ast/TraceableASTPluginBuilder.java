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

import jnr.ffi.annotations.In;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundSetter;

import static io.jenkins.cli.shaded.com.sun.activation.registries.LogSupport.log;

public class TraceableASTPluginBuilder extends Builder implements SimpleBuildStep {

    private String scanName;
    private String testEnvironment;
    private String clientToken;

    private String traceableCliBinaryLocation;
    private String traceableServer;
    private String idleTimeout;
    private String scanTimeout;
    private String scanId;


    public String getScanName() { return scanName; }
    public String getTestEnvironment() { return testEnvironment; }
    public String getClientToken() { return clientToken; }

    public String getTraceableCliBinaryLocation() { return traceableCliBinaryLocation; }
    public String getTraceableServer() { return traceableServer; }
    public String getIdleTimeout() { return idleTimeout; }
    public String getScanTimeout() { return scanTimeout; }

    @DataBoundConstructor
    public TraceableASTPluginBuilder(){}

    @DataBoundSetter
    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    @DataBoundSetter
    public void setTestEnvironment(String testEnvironment) { this.testEnvironment = testEnvironment; }

    @DataBoundSetter
    public void setClientToken(String clientToken) { this.clientToken = clientToken; }

    @DataBoundSetter
    public void setTraceableCliBinaryLocation(String traceableCliBinaryLocation ) { this.traceableCliBinaryLocation = traceableCliBinaryLocation; }

    @DataBoundSetter
    public void setTraceableServer(String traceableServer) { this.traceableServer = traceableServer; }

    @DataBoundSetter
    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @DataBoundSetter
    public void setScanTimeout(String  scanTimeout) {
        this.scanTimeout = scanTimeout;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "src/main/resources/io/jenkins/plugins/traceable/ast/TraceableASTPluginBuilder/shell_scripts/run_ast_scan.sh",
                    scanName,
                    testEnvironment,
                    clientToken,
                    traceableServer,
                    idleTimeout,
                    scanTimeout
            );
            Process runAstScan = pb.start();
            logOutput(runAstScan.getInputStream(), "",listener);
            logOutput(runAstScan.getErrorStream(), "Error: ",listener);
            runAstScan.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if(scanId != null)
        run.addAction(new AbortScanAction(scanId, listener));
        run.addAction(new GenerateReportAction());
    }

    private void logOutput(InputStream inputStream, String prefix, TaskListener listener) {
        new Thread(() -> {
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            while (scanner.hasNextLine()) {
                synchronized (this) {
                    String line = scanner.nextLine();

                    // Extract the scan ID from the cli output of scan init command.
                    if(prefix == "" && line.contains("Running scan with ID")) {
                        String[] tokens = line.split(" ");
                        scanId = tokens[ tokens.length -1 ];
                    }

                    listener.getLogger().println(prefix + line);
                }
            }
            scanner.close();
        }).start();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String PLUGIN_NAME = "Traceable AST";
        public FormValidation doCheckName( @QueryParameter String scanName, @QueryParameter String testEnvironment )

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
