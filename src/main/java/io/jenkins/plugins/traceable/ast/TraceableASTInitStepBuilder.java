package io.jenkins.plugins.traceable.ast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import io.jenkins.cli.shaded.org.apache.commons.lang.ObjectUtils;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.UUID;

public class TraceableASTInitStepBuilder extends Builder implements SimpleBuildStep {

    private String scanName;
    private String testEnvironment;
    private static String clientToken;
    private String pluginsList;
    private String pluginsToInclude;
    private Boolean selectedInstallCli;
    private Boolean selectedUseInstalledCli;
    private static Boolean selectedLocalCliEnvironment;
    private static String traceableCliBinaryLocation;
    private String includeUrlRegex;
    private String excludeUrlRegex;
    private String targetUrl;
    private String traceableServer;
    private String scanTimeout;
    private static String scanId;
    private static Boolean scanEnded;
    private String referenceEnv;
    private static String traceableRootCaFileName;
    private static String traceableCliCertFileName;
    private static String traceableCliKeyFileName;


    public String getScanName() { return scanName; }
    public String getTestEnvironment() { return testEnvironment; }
    public static String getClientToken() { return clientToken; }
    public Boolean getSelectedInstallCli() { return selectedInstallCli; }
    public Boolean getSelectedUseInstalledCli() { return  selectedUseInstalledCli; }
    public static Boolean getSelectedLocalCliEnvironment() { return selectedLocalCliEnvironment; }
    public static String getTraceableCliBinaryLocation() { return traceableCliBinaryLocation; }
    public String getPluginsToInclude() { return pluginsToInclude; }
    public String getIncludeUrlRegex() { return includeUrlRegex; }
    public String getExcludeUrlRegex() { return excludeUrlRegex; }
    public String getTargetUrl() { return targetUrl; }
    public String getTraceableServer() { return traceableServer; }
    public String getScanTimeout() { return scanTimeout; }
    public static String getScanId() { return scanId; }
    public static Boolean getScanEnded() { return scanEnded; }
    public String getReferenceEnv() { return referenceEnv; }
    public static String getTraceableRootCaFileName() { return traceableRootCaFileName; }
    public static String getTraceableCliCertFileName() { return traceableCliCertFileName; }
    public static String getTraceableCliKeyFileName() { return traceableCliKeyFileName; }



    @DataBoundConstructor
    public TraceableASTInitStepBuilder() {
        traceableCliBinaryLocation = null;
        selectedLocalCliEnvironment = true;
        String propFilePackage = "io.jenkins.plugins.traceable.ast.TraceableASTInitStepBuilder.config";
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
    public static void setClientToken(String clientToken) { TraceableASTInitStepBuilder.clientToken = clientToken; }
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
        TraceableASTInitStepBuilder.selectedLocalCliEnvironment = selectedLocalCliEnvironment;
    }

    @DataBoundSetter
    public static void setTraceableCliBinaryLocation(String traceableCliBinaryLocation) {
        TraceableASTInitStepBuilder.traceableCliBinaryLocation = traceableCliBinaryLocation;
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
    public void setScanTimeout(String  scanTimeout) { this.scanTimeout = scanTimeout; }

    @DataBoundSetter
    public void setReferenceEnv(String referenceEnv) { this.referenceEnv = referenceEnv;}

    @DataBoundSetter
    public static void setTraceableRootCaFileName(String traceableRootCaFileName) {
        TraceableASTInitStepBuilder.traceableRootCaFileName = traceableRootCaFileName;
    }

    @DataBoundSetter
    public static void setTraceableCliCertFileName(String traceableCliCertFileName) {
        TraceableASTInitStepBuilder.traceableCliCertFileName = traceableCliCertFileName;
    }

    @DataBoundSetter
    public static void setTraceableCliKeyFileName(String traceableCliKeyFileName) {
        TraceableASTInitStepBuilder.traceableCliKeyFileName = traceableCliKeyFileName;
    }

    public static void setScanId(String scanId) {
        TraceableASTInitStepBuilder.scanId = scanId;
    }
    public static void setScanEnded(Boolean scanEnded) {
        TraceableASTInitStepBuilder.scanEnded = scanEnded;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        scanEnded = false;
        TraceableASTInitAndRunStepBuilder.setClientToken(null);
        if (traceableCliBinaryLocation == null || traceableCliBinaryLocation.equals("")) {
          downloadTraceableCliBinary(listener);
        }
        initScan(listener, run);
    }

    // Download the binary if the location of the binary is not given.
    private void downloadTraceableCliBinary(TaskListener listener) {
        String script_path = "shell_scripts/download_traceable_cli_binary.sh";
        String[] args = new String[]{};
        runScript(script_path, args, listener);
        traceableCliBinaryLocation = "./traceable";
    }

    // Run the scan.
    private void initScan( TaskListener listener, Run<?, ?> run ){
        String scriptPath = "shell_scripts/init_ast_scan.sh";
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
                        scanTimeout,
                        run.getId(),
                        run.getUrl(),
                        referenceEnv,
                        traceableRootCaFileName,
                        traceableCliCertFileName,
                        traceableCliKeyFileName
                };
        runScript(scriptPath, args, listener);
    }

    private void runScript(String scriptPath, String[] args, TaskListener listener) {
        try{
            // Read the bundled script as string
            String bundledScript = CharStreams.toString(
                    new InputStreamReader(getClass().getResourceAsStream(scriptPath), StandardCharsets.UTF_8));
            // Create a temp file with uuid appended to the name just to be safe
            File tempFile = File.createTempFile("script_" + UUID.randomUUID().toString(), ".sh");
            // Write the string to temp file
            BufferedWriter x = Files.newWriter(tempFile,  Charsets.UTF_8);
            x.write(bundledScript);
            x.close();
            String execScript = "/bin/bash " + tempFile.getAbsolutePath();
            for(int i=0;i<args.length;i++) {
                if(args[i]!=null && !args[i].equals(""))
                    execScript += " " + args[i];
                else execScript += " ''";
            }
            Process pb = Runtime.getRuntime().exec(execScript);
            logOutput(pb.getInputStream(), "",listener);
            logOutput(pb.getErrorStream(), "Error: ",listener);
            pb.waitFor();
            boolean deleted_temp = tempFile.delete();
            if(!deleted_temp) {
                throw new FileNotFoundException("Temp script file not found");
            }

        } catch (Exception e){
            e.printStackTrace();
        }
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

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String STEP_NAME = "Traceable AST - Initialize";

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

