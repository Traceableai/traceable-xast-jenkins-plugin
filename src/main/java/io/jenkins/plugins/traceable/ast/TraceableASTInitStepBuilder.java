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
import io.jenkins.plugins.traceable.ast.scan.helper.Assets;
import io.jenkins.plugins.traceable.ast.scan.helper.TrafficType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import jenkins.tasks.SimpleBuildStep;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.UUID;

@Slf4j
public class TraceableASTInitStepBuilder extends Builder implements SimpleBuildStep {

    private String scanName;
    private String testEnvironment;
    private static String clientToken;
    private String attackPolicy;
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
    private String scanTimeout;
    private static Boolean scanEnded;
    private static String traceableRootCaFileName;
    private static String traceableCliCertFileName;
    private static String traceableCliKeyFileName;
    private String workspacePathString;

    private String suiteName;

    private String includeEndpointLabels;

    private String includeEndpointIds;

    private String includeServiceIds;


    private String hookName;

    private Assets assets;

    private TrafficType trafficType;

    private Boolean includeAllEndPoints;

    private Boolean xastLive;

    private Boolean xastReplay;

    public Assets getAssets(){
        return assets;
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }


    public String getScanName() { return scanName; }
    public String getTestEnvironment() { return testEnvironment; }
    public static String getClientToken() { return clientToken; }
    public static String getTraceableCliBinaryLocation() { return traceableCliBinaryLocation; }
    public String getPluginsToInclude() { return pluginsToInclude; }
    public String getIncludeUrlRegex() { return includeUrlRegex; }
    public String getExcludeUrlRegex() { return excludeUrlRegex; }
    public String getTargetUrl() { return targetUrl; }
    public String getTraceableServer() { return traceableServer; }
    public String getScanTimeout() { return scanTimeout; }
    public static Boolean getScanEnded() { return scanEnded; }
    public static String getTraceableRootCaFileName() { return traceableRootCaFileName; }
    public static String getTraceableCliCertFileName() { return traceableCliCertFileName; }
    public static String getTraceableCliKeyFileName() { return traceableCliKeyFileName; }

    public String getPostmanEnvironment() { return postmanEnvironment; }

    public String getPostmanCollection() { return postmanCollection; }

    public String getOpenApiSpecFiles() { return openApiSpecFiles; }

    public String getOpenApiSpecIds() { return openApiSpecIds; }

    public String getAttackPolicy() { return attackPolicy; }

    public String getCliSource() {
        return cliSource;
    }

    public String getCliField() {
        return cliField;
    }

    public String getSuiteName(){
        return suiteName;
    }

    public String getIncludeEndpointLabels() {
        return includeEndpointLabels;
    }

    public String getIncludeEndpointIds() {
        return includeEndpointIds;
    }

    public String getIncludeServiceIds() {
        return includeServiceIds;
    }


    public String getHookName() {
        return hookName;
    }



    @DataBoundConstructor
    public TraceableASTInitStepBuilder() {
        traceableCliBinaryLocation = null;
        this.includeAllEndPoints = true;
        this.xastLive = true;
    }

    @DataBoundSetter
    public void setIncludeEndpointLabels(String includeEndpointLabels) {
        this.includeEndpointLabels = includeEndpointLabels;
        if(assets != Assets.EndpointLabels) {
            this.includeEndpointLabels = null;
        }
    }

    @DataBoundSetter
    public void setIncludeEndpointIds(String includeEndpointIds) {
        this.includeEndpointIds = includeEndpointIds;
        if(assets != Assets.EndpointIds) {
            this.includeEndpointIds = null;
        }
    }

    @DataBoundSetter
    public void setIncludeServiceIds(String includeServiceIds) {
        this.includeServiceIds = includeServiceIds;
        if(assets != Assets.ServiceIds) {
            this.includeServiceIds = null;
        }
    }

    @DataBoundSetter
    public void setTrafficType(TrafficType trafficType) {
        this.trafficType = trafficType;
        switch (trafficType) {
            case XAST_LIVE:
                this.xastLive = true;
                this.xastReplay = false;
                break;
            case XAST_REPLAY:
                this.xastLive = false;
                this.xastReplay = true;
                break;
            default:
                this.xastLive = false;
                this.xastReplay = false;
        }

    }

    @DataBoundSetter
    public void setAssets(Assets assets) {
        this.assets = assets;
        if(assets != Assets.AllEndpoints) {
            this.includeAllEndPoints = false;
        }
    }

    @DataBoundSetter
    public void setHookName(String hookName) {
        this.hookName = hookName;
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
    public static void setClientToken(String clientToken) { TraceableASTInitStepBuilder.clientToken = clientToken; }

    @DataBoundSetter
    public static void setTraceableCliBinaryLocation(String traceableCliBinaryLocation) {
        TraceableASTInitStepBuilder.traceableCliBinaryLocation = traceableCliBinaryLocation;
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
    public void setScanTimeout(String  scanTimeout) { this.scanTimeout = scanTimeout; }

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


    @DataBoundSetter
    public void setPostmanEnvironment(String postmanEnvironment) {
        this.postmanEnvironment = postmanEnvironment;
        if(trafficType != TrafficType.DAST_POSTMAN_COLLECTION) {
            this.postmanEnvironment = null;
        }
    }

    @DataBoundSetter
    public void setPostmanCollection(String postmanCollection) {
        this.postmanCollection = postmanCollection;
        if(trafficType != TrafficType.DAST_POSTMAN_COLLECTION) {
            this.postmanCollection = null;
        }
    }

    @DataBoundSetter
    public void setOpenApiSpecIds(String openApiSpecIds) {
        this.openApiSpecIds = openApiSpecIds;
        if(trafficType != TrafficType.DAST_OPEN_API_SPECS) {
            this.openApiSpecIds = null;
        }
    }

    @DataBoundSetter
    public void setOpenApiSpecFiles(String openApiSpecFiles) {
        this.openApiSpecFiles = openApiSpecFiles;
        if(trafficType != TrafficType.DAST_OPEN_API_SPECS) {
            this.openApiSpecFiles = null;
        }
    }

    @DataBoundSetter
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    @DataBoundSetter
    public void setAttackPolicy(String attackPolicy) {
        this.attackPolicy = attackPolicy;
    }

    public static void setScanEnded(Boolean scanEnded) {
        TraceableASTInitStepBuilder.scanEnded = scanEnded;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        workspacePathString = workspace.getRemote();
        scanEnded = false;
        TraceableASTInitAndRunStepBuilder.setClientToken(null);

        if (cliSource.equals("download")) {
            downloadTraceableCliBinary(listener);
        } else if(cliSource.equals("localpath")) {
            if(cliField == null || cliField.equals("")) {
                throw new InterruptedException("Location of traceable cli binary not provided.");
            } else {
                traceableCliBinaryLocation = cliField;
            }
        }

        initScan(listener, run);
    }

    // Download the binary if the location of the binary is not given.
    private void downloadTraceableCliBinary(TaskListener listener) throws IOException, InterruptedException {
        String script_path = "shell_scripts/download_traceable_cli_binary.sh";
        String[] args = new String[]{
                workspacePathString,
                cliField
        };
        runScript(script_path, args, listener, "downloadTraceableCliBinary");
        traceableCliBinaryLocation = workspacePathString + "/traceable";
    }

    // Run the scan.
    private void initScan( TaskListener listener, Run<?, ?> run ){
        String replay = String.valueOf(xastReplay != null && xastReplay);
        String allEndPoint = String.valueOf(includeAllEndPoints != null && includeAllEndPoints);

        String scriptPath = "shell_scripts/init_ast_scan.sh";
        String[] args =
                new String[] {
                        traceableCliBinaryLocation,
                        traceableRootCaFileName,
                        traceableCliCertFileName,
                        traceableCliKeyFileName,
                        scanName,
                        testEnvironment,
                        clientToken,
                        attackPolicy,
                        pluginsToInclude,
                        includeUrlRegex,
                        excludeUrlRegex,
                        targetUrl,
                        traceableServer,
                        scanTimeout,
                        openApiSpecIds,
                        openApiSpecFiles,
                        postmanCollection,
                        postmanEnvironment,
                        suiteName,
                        includeServiceIds,
                        includeEndpointIds,
                        includeEndpointLabels,
                        hookName,
                        allEndPoint,
                        replay,
                };
        runScript(scriptPath, args, listener, "runAndInitScan");
    }

    private void runScript(String scriptPath, String[] args, TaskListener listener,  String caller) {
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
                if(!StringUtils.isEmpty(args[i])) args[i] = args[i].replace(" ","");
                if(args[i]!=null && !args[i].equals(""))
                    execScript = new StringBuffer().append(execScript).append(" ").append(args[i]).toString();
                else execScript = new StringBuffer().append(execScript).append(" ''").toString();
            }
            Process pb = Runtime.getRuntime().exec(execScript);
            logOutput(pb.getInputStream(), "", listener);
            if(!caller.equals("downloadTraceableCliBinary")) {
                logOutput(pb.getErrorStream(), "Error: ", listener);
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

