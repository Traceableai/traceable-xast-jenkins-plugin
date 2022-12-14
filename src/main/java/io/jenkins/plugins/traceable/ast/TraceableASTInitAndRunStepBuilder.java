package io.jenkins.plugins.traceable.ast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import hudson.Launcher;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import lombok.ToString;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.UUID;

import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundSetter;


public class TraceableASTInitAndRunStepBuilder extends Builder implements SimpleBuildStep {

    private String scanName;
    private String testEnvironment;
    private static String clientToken;
    private String pluginsList;
    private String pluginsToInclude;
    private Boolean selectedInstallCli;
    private Boolean selectedUseInstalledCli;
    private static Boolean selectedLocalCliEnvironment;
    private String cliVersion;
    private static String traceableCliBinaryLocation;
    private String includeUrlRegex;
    private String excludeUrlRegex;
    private String targetUrl;
    private String traceableServer;
    private String idleTimeout;
    private String scanTimeout;
    private static String scanId;
    private static Boolean scanEnded;
    private String referenceEnv;
    private String maxRetries;
    private static String traceableRootCaFileName;
    private static String traceableCliCertFileName;
    private static String traceableCliKeyFileName;


    public String getScanName() { return scanName; }
    public String getTestEnvironment() { return testEnvironment; }
    public static String getClientToken() { return clientToken; }
    public Boolean getSelectedInstallCli() { return selectedInstallCli; }
    public Boolean getSelectedUseInstalledCli() { return  selectedUseInstalledCli; }
    public static Boolean getSelectedLocalCliEnvironment() { return selectedLocalCliEnvironment; }
    public String getCliVersion() { return cliVersion; }
    public static String getTraceableCliBinaryLocation() { return traceableCliBinaryLocation; }
    public String getPluginsToInclude() { return pluginsToInclude; }
    public String getIncludeUrlRegex() { return includeUrlRegex; }
    public String getExcludeUrlRegex() { return excludeUrlRegex; }
    public String getTargetUrl() { return targetUrl; }
    public String getTraceableServer() { return traceableServer; }
    public String getIdleTimeout() { return idleTimeout; }
    public String getScanTimeout() { return scanTimeout; }
    public static String getScanId() { return scanId; }
    public static Boolean getScanEnded() { return scanEnded; }
    public String getReferenceEnv() { return referenceEnv; }
    public String getMaxRetries() { return maxRetries; }
    public static String getTraceableRootCaFileName() { return traceableRootCaFileName; }
    public static String getTraceableCliCertFileName() { return traceableCliCertFileName; }
    public static String getTraceableCliKeyFileName() { return traceableCliKeyFileName; }

    @DataBoundConstructor
    public TraceableASTInitAndRunStepBuilder() {
        traceableCliBinaryLocation = null;
        selectedLocalCliEnvironment = true;
        String propFilePackage = "io.jenkins.plugins.traceable.ast.TraceableASTInitAndRunStepBuilder.config";
        ResourceBundle rb = ResourceBundle.getBundle(propFilePackage);
        if (rb != null) {
            pluginsList = rb.getString("PluginsList");
        } else { return; }
        pluginsToInclude = pluginsList;
    }

    @DataBoundSetter
    public void setScanName(String scanName) { this.scanName = scanName; }

    @DataBoundSetter
    public void setTestEnvironment(String testEnvironment) { this.testEnvironment = testEnvironment; }

    @DataBoundSetter
    public static void setClientToken(String clientToken) { TraceableASTInitAndRunStepBuilder.clientToken = clientToken; }

    @DataBoundSetter
    public void setSelectedInstallCli(Boolean selectedInstallCli) {
        this.selectedInstallCli = selectedInstallCli;
    }

    @DataBoundSetter
    public void setSelectedUseInstalledCli(Boolean selectedUseInstalledCli) {
        this.selectedUseInstalledCli = selectedUseInstalledCli;
    }

    @DataBoundSetter
    public static void setSelectedLocalCliEnvironment(Boolean selectedLocalCliEnvironment) {
        TraceableASTInitAndRunStepBuilder.selectedLocalCliEnvironment = selectedLocalCliEnvironment;
    }

    @DataBoundSetter
    public void setCliVersion(String cliVersion) {
        this.cliVersion = cliVersion;
    }

    @DataBoundSetter
    public static void setTraceableCliBinaryLocation(String traceableCliBinaryLocation) {
        TraceableASTInitAndRunStepBuilder.traceableCliBinaryLocation = traceableCliBinaryLocation;
    }

    @DataBoundSetter
    public void setPluginsToInclude(String pluginsToInclude) {
        if(!(pluginsToInclude == null || pluginsToInclude.equals(""))) { this.pluginsToInclude = pluginsToInclude; }
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

    @DataBoundSetter
    public void setReferenceEnv(String referenceEnv) { this.referenceEnv = referenceEnv;}

    @DataBoundSetter
    public void setMaxRetries(String maxRetries) { this.maxRetries = maxRetries; }

    @DataBoundSetter
    public static void setTraceableRootCaFileName(String traceableRootCaFileName) {
        TraceableASTInitAndRunStepBuilder.traceableRootCaFileName = traceableRootCaFileName;
    }

    @DataBoundSetter
    public static void setTraceableCliCertFileName(String traceableCliCertFileName) {
        TraceableASTInitAndRunStepBuilder.traceableCliCertFileName = traceableCliCertFileName;
    }

    @DataBoundSetter
    public static void setTraceableCliKeyFileName(String traceableCliKeyFileName) {
        TraceableASTInitAndRunStepBuilder.traceableCliKeyFileName = traceableCliKeyFileName;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        scanEnded = false;
        TraceableASTInitStepBuilder.setClientToken(null);

        if (traceableCliBinaryLocation == null || traceableCliBinaryLocation.equals("")) {
          downloadTraceableCliBinary(listener);
        }
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        runAndInitScan(listener, run);
                        if (scanId != null) {
                            abortScan(listener);
                        }
                        scanEnded = true;
                    }
                }).start();
    }

    // Download the binary if the location of the binary is not given.
    private void downloadTraceableCliBinary(TaskListener listener) {
        String script_path = "shell_scripts/download_traceable_cli_binary.sh";
        String[] args = new String[]{
                cliVersion
        };
        runScript(script_path, args, listener, "downloadTraceableCliBinary");
        traceableCliBinaryLocation = "./traceable" ;
    }

    // Run the scan.
    private void runAndInitScan( TaskListener listener, Run<?, ?> run ){
        String scriptPath = "shell_scripts/run_and_init_ast_scan.sh";
        String[] args =
          new String[] {
            selectedLocalCliEnvironment.toString(),
            traceableCliBinaryLocation,
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
            run.getUrl(),
            referenceEnv,
            maxRetries,
            traceableRootCaFileName,
            traceableCliCertFileName,
            traceableCliKeyFileName
          };
        runScript(scriptPath, args, listener, "runAndInitScan");
    }

    //Stop the scan with the given scan ID.
    private void abortScan(TaskListener listener) {
        String scriptPath = "shell_scripts/stop_ast_scan.sh";
        String[] args = new String[]{
                selectedLocalCliEnvironment.toString(),
                traceableCliBinaryLocation,
                scanId
            };
        runScript(scriptPath, args, listener, "abortScan");
    }

    private void runScript(String scriptPath, String[] args, TaskListener listener, String caller) {
        try{
        // Read the bundled script as string
        String bundledScript = CharStreams.toString(
                new InputStreamReader(getClass().getResourceAsStream(scriptPath), Charsets.UTF_8));
        // Create a temp file with uuid appended to the name just to be safe
        File tempFile = File.createTempFile("script_" + UUID.randomUUID().toString(), ".sh");
        // Write the string to temp file
        BufferedWriter x = Files.newWriter(tempFile,  Charsets.UTF_8);
        x.write(bundledScript);
        x.close();
            String execScript = new StringBuffer().append("/bin/bash ").append(tempFile.getAbsolutePath()).toString();
        for(int i=0;i<args.length;i++) {
            if(args[i]!=null && !args[i].equals(""))
                execScript = new StringBuffer().append(execScript).append(" ").append(args[i]).toString();
            else execScript = new StringBuffer().append(execScript).append(" ''").toString();
        }
        Process pb = Runtime.getRuntime().exec(execScript);
        logOutput(pb.getInputStream(), "", listener, caller);
        logOutput(pb.getErrorStream(), "Error: ", listener, caller);
        pb.waitFor();
        boolean deleted_temp = tempFile.delete();
        if(!deleted_temp) {
            throw new FileNotFoundException("Temp script file not found");
        }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private void logOutput(InputStream inputStream, String prefix, TaskListener listener, String caller) {
    new Thread(
            () -> {
              Scanner scanner = new Scanner(inputStream, "UTF-8");
              while (scanner.hasNextLine()) {
                synchronized (this) {
                  String line = scanner.nextLine();

                  // Extract the scan ID from the cli output of scan init command.
                  if (prefix.equals("") && line.contains("Running scan with ID")) {
                    String[] tokens = line.split(" ");
                    scanId = tokens[tokens.length - 1];
                  }

                  // Don't output the logs of abort scan.
                  if (!caller.equals("abortScan")) {
                    listener.getLogger().println(prefix + line);
                  }
                }
              }
              scanner.close();
            })
        .start();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String STEP_NAME = "Traceable AST - Initialize and Run";

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
