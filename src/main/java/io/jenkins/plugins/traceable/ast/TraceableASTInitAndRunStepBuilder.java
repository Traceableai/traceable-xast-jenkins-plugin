package io.jenkins.plugins.traceable.ast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
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
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.UUID;


@Slf4j
public class TraceableASTInitAndRunStepBuilder extends Builder implements SimpleBuildStep {

    private String scanName;
    private String testEnvironment;
    private static String clientToken;
    private String policyName;
    private String scanEvalCriteria;
    private String openApiSpecIds;
    private String openApiSpecFiles;
    private String postmanCollection;
    private String postmanEnvironment;
    private String pluginsToInclude;
    private String cliSource;
    private String cliField;
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
    private String workspacePathString;

    private String suiteName;

    public String getScanName() { return scanName; }
    public String getTestEnvironment() { return testEnvironment; }
    public static String getClientToken() { return clientToken; }
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

    public String getScanEvalCriteria() { return scanEvalCriteria; }

    public String getPostmanEnvironment() { return postmanEnvironment; }

    public String getPostmanCollection() { return postmanCollection; }

    public String getOpenApiSpecFiles() { return openApiSpecFiles; }

    public String getOpenApiSpecIds() { return openApiSpecIds; }

    public String getPolicyName() { return policyName; }

    public String getCliSource() {
        return cliSource;
    }

    public String getCliField() {
        return cliField;
    }

    public String getSuiteName() {
        return suiteName;
    }

    @DataBoundConstructor
    public TraceableASTInitAndRunStepBuilder() {
        traceableCliBinaryLocation = null;
    }


    @DataBoundSetter
    public void setCliSource(String cliSource) {
        this.cliSource = cliSource;
    }
    @DataBoundSetter
    public void setCliField(String cliField) {
        this.cliField = cliField;
    }
    @DataBoundSetter
    public void setScanName(String scanName) { this.scanName = scanName; }

    @DataBoundSetter
    public void setTestEnvironment(String testEnvironment) { this.testEnvironment = testEnvironment; }

    @DataBoundSetter
    public static void setClientToken(String clientToken) { TraceableASTInitAndRunStepBuilder.clientToken = clientToken; }

    @DataBoundSetter
    public static void setTraceableCliBinaryLocation(String traceableCliBinaryLocation) {
        TraceableASTInitAndRunStepBuilder.traceableCliBinaryLocation = traceableCliBinaryLocation;
    }

    @DataBoundSetter
    public void setPluginsToInclude(String pluginsToInclude) { this.pluginsToInclude = pluginsToInclude; }

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

    @DataBoundSetter
    public void setScanEvalCriteria(String scanEvalCriteria) {
        this.scanEvalCriteria = scanEvalCriteria;
    }

    @DataBoundSetter
    public void setPostmanEnvironment(String postmanEnvironment) {
        this.postmanEnvironment = postmanEnvironment;
    }

    @DataBoundSetter
    public void setPostmanCollection(String postmanCollection) {
        this.postmanCollection = postmanCollection;
    }

    @DataBoundSetter
    public void setOpenApiSpecIds(String openApiSpecIds) {
        this.openApiSpecIds = openApiSpecIds;
    }

    @DataBoundSetter
    public void setOpenApiSpecFiles(String openApiSpecFiles) {
        this.openApiSpecFiles = openApiSpecFiles;
    }

    @DataBoundSetter
    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    @DataBoundSetter
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }


    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        workspacePathString = workspace.getRemote();
        scanEnded = false;
        TraceableASTInitStepBuilder.setClientToken(null);

        if (cliSource.equals("download")) {
          downloadTraceableCliBinary(listener);
        } else if(cliSource.equals("localpath")) {
            if(cliField == null || cliField.equals("")) {
                throw new InterruptedException("Location of traceable cli binary not provided.");
            } else {
                traceableCliBinaryLocation = cliField;
            }
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
                workspacePathString,
                cliField
        };
        runScript(script_path, args, listener, "downloadTraceableCliBinary");
        traceableCliBinaryLocation = workspacePathString + "/traceable" ;
    }

    // Run the scan.
    private void runAndInitScan( TaskListener listener, Run<?, ?> run ){
        String configFile= "scan:\n plugins:\n  disabled: true\n  custom:\n   disabled: false\n " + scanEvalCriteria.replaceAll("\n","\n ");
        Path configPath = null;
        try {

        // Creating an instance of file
        configPath = Paths.get(workspacePathString , "/config.yaml");
        byte[] arr = configFile.getBytes();

        // Write the string to file
            java.nio.file.Files.write(configPath, arr);
        } catch (IOException e) {
            log.error("Error writing to config.yaml the config: {}", configFile);
            throw new RuntimeException(e);
        }
        String scriptPath = "shell_scripts/run_and_init_ast_scan.sh";
        String[] args =
          new String[] {
            traceableCliBinaryLocation,
            scanName,
            testEnvironment,
            clientToken,
            policyName,
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
            openApiSpecIds,
            openApiSpecFiles,
            postmanCollection,
            postmanEnvironment,
            traceableRootCaFileName,
            traceableCliCertFileName,
            traceableCliKeyFileName,
            suiteName,
            configPath.toString()
          };
        runScript(scriptPath, args, listener, "runAndInitScan");
    }

    //Stop the scan with the given scan ID.
    private void abortScan(TaskListener listener) {
        String scriptPath = "shell_scripts/stop_ast_scan.sh";
        String[] args = new String[]{
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
        File tempFile = File.createTempFile("script_" + scriptPath.replaceAll(".sh","") +"_"+ UUID.randomUUID().toString(), ".sh");
        // Write the string to temp file
        BufferedWriter x = com.google.common.io.Files.newWriter(tempFile,  Charsets.UTF_8);
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
        if(!caller.equals("downloadTraceableCliBinary")) {
            logOutput(pb.getErrorStream(), "Error: ", listener, caller);
        }
        pb.waitFor();
        boolean deleted_temp = tempFile.delete();
        if(!deleted_temp) {
            throw new FileNotFoundException("Temp script file not found");
        }

        } catch (Exception e){
            log.error("Exception in running {} script : {}", scriptPath, e);
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
                    scanId = tokens[tokens.length - 1].substring(0,36);
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
