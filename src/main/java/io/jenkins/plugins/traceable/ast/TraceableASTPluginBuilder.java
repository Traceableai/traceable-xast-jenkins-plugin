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
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundSetter;


public class TraceableASTPluginBuilder extends Builder implements SimpleBuildStep {

    private String scanName;
    private String testEnvironment;
    private String clientToken;
    private String pluginsToInclude;
    private String traceableCliBinaryLocation;
    private String includeUrlRegex;
    private String excludeUrlRegex;
    private String targetUrl;
    private String traceableServer;
    private String idleTimeout;
    private String scanTimeout;
    private String scanId;


    public String getScanName() { return scanName; }
    public String getTestEnvironment() { return testEnvironment; }
    public String getClientToken() { return clientToken; }
    public String getTraceableCliBinaryLocation() { return traceableCliBinaryLocation; }
    public String getPluginsToInclude() { return pluginsToInclude; }
    public String getIncludeUrlRegex() { return includeUrlRegex; }
    public String getExcludeUrlRegex() { return excludeUrlRegex; }
    public String getTargetUrl() { return targetUrl; }
    public String getTraceableServer() { return traceableServer; }
    public String getIdleTimeout() { return idleTimeout; }
    public String getScanTimeout() { return scanTimeout; }
    public String getScanId() { return scanId; }


    @DataBoundConstructor
    public TraceableASTPluginBuilder(){
        this.pluginsToInclude = "unauthenticated_access,bola,parameter_tampering,mass_assignment,os_command_injection,java_log4shell," +
                "sqli_blind,self_signed_certificate,tls_not_implemented,weak_ciphers,logjam,lucky13,beast,certificate_name_mismatch," +
                "revoked_certificate,crime,sweet32,expired_certificate,drown,broken_certificate_chain,poodle,ssrf_blind";
    }

    @DataBoundSetter
    public void setScanName(String scanName) { this.scanName = scanName; }

    @DataBoundSetter
    public void setTestEnvironment(String testEnvironment) { this.testEnvironment = testEnvironment; }

    @DataBoundSetter
    public void setClientToken(String clientToken) { this.clientToken = clientToken; }

    @DataBoundSetter
    public void setTraceableCliBinaryLocation(String traceableCliBinaryLocation ) { this.traceableCliBinaryLocation = traceableCliBinaryLocation; }

    @DataBoundSetter
    public void setPluginsToInclude(String pluginsToInclude) {
        if(!(pluginsToInclude==null || pluginsToInclude.equals(""))) { this.pluginsToInclude = pluginsToInclude; }
    }

    @DataBoundSetter
    public void setIncludeUrlRegex(String includeUrlRegex) { this.includeUrlRegex = includeUrlRegex; }

    @DataBoundSetter
    public void setExcludeUrlRegex(String excludeUrlRegex) { this.excludeUrlRegex = excludeUrlRegex; }

    @DataBoundSetter
    public void setTargetUrl(String targetUrl) { this.targetUrl = targetUrl; }

    @DataBoundSetter
    public void setTraceableServer(String traceableServer) { this.traceableServer = traceableServer; }

    @DataBoundSetter
    public void setIdleTimeout(String idleTimeout) { this.idleTimeout = idleTimeout; }

    @DataBoundSetter
    public void setScanTimeout(String  scanTimeout) { this.scanTimeout = scanTimeout; }


    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {

        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "src/main/resources/io/jenkins/plugins/traceable/ast/TraceableASTPluginBuilder/shell_scripts/run_ast_scan.sh",
                    scanName,
                    testEnvironment,
                    clientToken,
                    pluginsToInclude,
                    includeUrlRegex,
                    excludeUrlRegex,
                    targetUrl,
                    traceableServer,
                    idleTimeout,
                    scanTimeout,
                    run.getId(),
                    run.getUrl()
            );
            Process runAstScan = pb.start();
            logOutput(runAstScan.getInputStream(), "",listener);
            logOutput(runAstScan.getErrorStream(), "Error: ",listener);
            runAstScan.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        if (scanId != null) {
            run.addAction(new AbortScanAction(scanId, listener));
            run.addAction(new GenerateReportAction(scanId));
        }
    }

    private void logOutput(InputStream inputStream, String prefix, TaskListener listener) {
        new Thread(() -> {
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            while (scanner.hasNextLine()) {
                synchronized (this) {
                    String line = scanner.nextLine();

                    // Extract the scan ID from the cli output of scan init command.
                    if(prefix.equals("") && line.contains("Running scan with ID")) {
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
