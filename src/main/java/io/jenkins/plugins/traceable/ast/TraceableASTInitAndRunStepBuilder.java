package io.jenkins.plugins.traceable.ast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.Secret;
import io.jenkins.plugins.traceable.ast.scan.helper.Assets;
import io.jenkins.plugins.traceable.ast.scan.helper.TrafficType;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import jenkins.tasks.SimpleBuildStep;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@Slf4j
public class TraceableASTInitAndRunStepBuilder extends Builder implements SimpleBuildStep {

    private String scanName;
    private String testEnvironment;
    private static Secret clientToken;
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
    private String idleTimeout;
    private String scanTimeout;
    private static String scanId;
    private static Boolean scanEnded;
    private String maxRetries;
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

    public Assets getAssets() {
        return assets;
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public String getScanName() {
        return scanName;
    }

    public String getTestEnvironment() {
        return testEnvironment;
    }

    public static Secret getClientToken() {
        return clientToken;
    }

    public static String getTraceableCliBinaryLocation() {
        return traceableCliBinaryLocation;
    }

    public String getPluginsToInclude() {
        return pluginsToInclude;
    }

    public String getIncludeUrlRegex() {
        return includeUrlRegex;
    }

    public String getExcludeUrlRegex() {
        return excludeUrlRegex;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public String getTraceableServer() {
        return traceableServer;
    }

    public String getIdleTimeout() {
        return idleTimeout;
    }

    public String getScanTimeout() {
        return scanTimeout;
    }

    public static String getScanId() {
        return scanId;
    }

    public static Boolean getScanEnded() {
        return scanEnded;
    }

    public String getMaxRetries() {
        return maxRetries;
    }

    public static String getTraceableRootCaFileName() {
        return traceableRootCaFileName;
    }

    public static String getTraceableCliCertFileName() {
        return traceableCliCertFileName;
    }

    public static String getTraceableCliKeyFileName() {
        return traceableCliKeyFileName;
    }

    public String getPostmanEnvironment() {
        return postmanEnvironment;
    }

    public String getPostmanCollection() {
        return postmanCollection;
    }

    public String getOpenApiSpecFiles() {
        return openApiSpecFiles;
    }

    public String getOpenApiSpecIds() {
        return openApiSpecIds;
    }

    public String getAttackPolicy() {
        return attackPolicy;
    }

    public String getCliSource() {
        return cliSource;
    }

    public String getCliField() {
        return cliField;
    }

    public String getSuiteName() {
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

    @DataBoundSetter
    public void setCliSource(String cliSource) {
        this.cliSource = cliSource;
    }

    @DataBoundSetter
    public void setCliField(String cliField) {
        this.cliField = cliField;
    }

    @DataBoundSetter
    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    @DataBoundSetter
    public void setTestEnvironment(String testEnvironment) {
        this.testEnvironment = testEnvironment;
    }

    @DataBoundSetter
    public static void setClientToken(Secret clientToken) {
        TraceableASTInitAndRunStepBuilder.clientToken = clientToken;
    }

    @DataBoundSetter
    public static void setTraceableCliBinaryLocation(String traceableCliBinaryLocation) {
        TraceableASTInitAndRunStepBuilder.traceableCliBinaryLocation = traceableCliBinaryLocation;
    }

    @DataBoundSetter
    public void setPluginsToInclude(String pluginsToInclude) {
        this.pluginsToInclude = pluginsToInclude;
    }

    @DataBoundSetter
    public void setIncludeUrlRegex(String includeUrlRegex) {
        this.includeUrlRegex = includeUrlRegex;
    }

    @DataBoundSetter
    public void setExcludeUrlRegex(String excludeUrlRegex) {
        this.excludeUrlRegex = excludeUrlRegex;
    }

    @DataBoundSetter
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @DataBoundSetter
    public void setTraceableServer(String traceableServer) {
        this.traceableServer = traceableServer;
    }

    @DataBoundSetter
    public void setIdleTimeout(String idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @DataBoundSetter
    public void setScanTimeout(String scanTimeout) {
        this.scanTimeout = scanTimeout;
    }

    @DataBoundSetter
    public void setMaxRetries(String maxRetries) {
        this.maxRetries = maxRetries;
    }

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
    public void setPostmanEnvironment(String postmanEnvironment) {
        this.postmanEnvironment = postmanEnvironment;
        if (trafficType != TrafficType.DAST_POSTMAN_COLLECTION) {
            this.postmanEnvironment = null;
        }
    }

    @DataBoundSetter
    public void setPostmanCollection(String postmanCollection) {
        this.postmanCollection = postmanCollection;
        if (trafficType != TrafficType.DAST_POSTMAN_COLLECTION) {
            this.postmanCollection = null;
        }
    }

    @DataBoundSetter
    public void setOpenApiSpecIds(String openApiSpecIds) {
        this.openApiSpecIds = openApiSpecIds;
        if (trafficType != TrafficType.DAST_OPEN_API_SPECS) {
            this.openApiSpecIds = null;
        }
    }

    @DataBoundSetter
    public void setOpenApiSpecFiles(String openApiSpecFiles) {
        this.openApiSpecFiles = openApiSpecFiles;
        if (trafficType != TrafficType.DAST_OPEN_API_SPECS) {
            this.openApiSpecFiles = null;
        }
    }

    @DataBoundSetter
    public void setAttackPolicy(String attackPolicy) {
        this.attackPolicy = attackPolicy;
    }

    @DataBoundSetter
    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    @DataBoundSetter
    public void setIncludeEndpointLabels(String includeEndpointLabels) {
        this.includeEndpointLabels = includeEndpointLabels;
        if (assets != Assets.EndpointLabels) {
            this.includeEndpointLabels = null;
        }
    }

    @DataBoundSetter
    public void setIncludeEndpointIds(String includeEndpointIds) {
        this.includeEndpointIds = includeEndpointIds;
        if (assets != Assets.EndpointIds) {
            this.includeEndpointIds = null;
        }
    }

    @DataBoundSetter
    public void setIncludeServiceIds(String includeServiceIds) {
        this.includeServiceIds = includeServiceIds;
        if (assets != Assets.ServiceIds) {
            this.includeServiceIds = null;
        }
    }

    @DataBoundSetter
    public void setHookName(String hookName) {
        this.hookName = hookName;
    }

    @DataBoundSetter
    public void setAssets(Assets assets) {
        this.assets = assets;
        if (assets != Assets.AllEndpoints) {
            this.includeAllEndPoints = false;
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

    @DataBoundConstructor
    public TraceableASTInitAndRunStepBuilder() {
        traceableCliBinaryLocation = null;
        this.includeAllEndPoints = true;
        this.xastLive = true;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        workspacePathString = workspace.getRemote();
        scanEnded = false;
        TraceableASTInitStepBuilder.setClientToken(null);

        if (cliSource.equals("download")) {
            downloadTraceableCliBinary(workspace, listener);
        } else if (cliSource.equals("localpath")) {
            if (cliField == null || cliField.equals("")) {
                throw new InterruptedException("Location of traceable cli binary not provided.");
            } else {
                traceableCliBinaryLocation = cliField;
            }
        }

        new Thread(new Runnable() {
                    @Override
                    public void run() {
                        runAndInitScan(run, workspace, listener);
                        if (scanId != null) {
                            abortScan(workspace, listener);
                        }
                        scanEnded = true;
                    }
                })
                .start();
    }



    // Download the binary if the location of the binary is not given.
    private void downloadTraceableCliBinary(FilePath workspace, TaskListener listener) throws IOException, InterruptedException {
        workspace.act(new DownloadTraceableCliBinary(this.cliField));
        traceableCliBinaryLocation = workspace.getRemote() + "/traceable";
    }

    // Run the scan.
    private void runAndInitScan(Run<?, ?> run, FilePath workspace, TaskListener listener) {

        String replay = String.valueOf(xastReplay != null && xastReplay);
        String allEndPoint = String.valueOf(includeAllEndPoints != null && includeAllEndPoints);
        String scriptPath = "shell_scripts/run_and_init_ast_scan.sh";
        String[] args = new String[] {
            traceableCliBinaryLocation,
            traceableRootCaFileName,
            traceableCliCertFileName,
            traceableCliKeyFileName,
            scanName,
            testEnvironment,
            clientToken.getPlainText(),
            attackPolicy,
            pluginsToInclude,
            includeUrlRegex,
            excludeUrlRegex,
            targetUrl,
            traceableServer,
            idleTimeout,
            scanTimeout,
            maxRetries,
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
            replay
        };

        runScript(workspace, listener, scriptPath, args, "initAndRunScan");
    }

    // Stop the scan with the given scan ID.
    private void abortScan(FilePath workspace, TaskListener listener) {
        String scriptPath = "shell_scripts/stop_ast_scan.sh";
        String[] args = new String[] {traceableCliBinaryLocation, scanId};
        runScript(workspace, listener, scriptPath, args, "abortScan");
    }

    private void runScript(FilePath workspace, TaskListener listener, String scriptPath, String[] args, String caller) {
        try {
                String tempFilePath = workspace.act(new CopyScript(scriptPath));

                if (caller.equals("initAndRunScan")) {
                    TraceableASTInitAndRunStepBuilder.scanId = workspace.act(new RunScript(listener, tempFilePath, args, caller));
                } else if (caller.equals("abortScan")) {
                    workspace.act(new RunScript(listener, tempFilePath, args, caller));
                }

                deleteScript(workspace, listener, tempFilePath);

        } catch (Exception e) {
            log.error("Exception in running {} script : {}", scriptPath, e);
            e.printStackTrace();
        }
    }

    private void deleteScript(FilePath workspace, TaskListener listener, String scriptPath) throws IOException, InterruptedException {
        String[] command = {"rm", scriptPath};
        Launcher nodeLauncher = workspace.createLauncher(listener);

        nodeLauncher
            .launch()
            .cmds(command)
            .stdout(listener.getLogger())
            .stderr(listener.getLogger())
            .join();
    }

    // Download the binary at the workspace node if the location is remote
    private static final class DownloadTraceableCliBinary implements FileCallable<Void> {

        private String workspacePath;
        private String version;
        private String osName;
        private String arch;
        private String filename;

        DownloadTraceableCliBinary(String version) {
            this.version = version;
        }

        @Override
        public Void invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {

            this.workspacePath = f.getAbsolutePath();
            this.osName = getKernelName();
            this.arch = getArchName();

            URL url = new URL(getDownloadUrl());
            String filepath = this.workspacePath + "/" + this.filename;

            try (InputStream inp = url.openStream();
                 BufferedInputStream bis = new BufferedInputStream(inp);
                 FileOutputStream fops = new FileOutputStream(filepath)) {

                byte[] d = new byte[1024];
                int i;
                while ((i = bis.read(d, 0, 1024)) != -1) {
                    fops.write(d, 0, i);
                }
            }

            unTar(filepath);
            return null;
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {
            return;
        }

        private String getKernelName() throws IOException, InterruptedException {
            String[] command = {"uname", "-s"};
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();

            // Catch the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            p.waitFor();
            return output.toString();
        }

        private String getArchName() {
            if (this.osName.equals("Darwin")) {
                return "macosx-x86_64.tar.gz";
            }

            return "linux-x86_64.tar.gz";
        }

        private String getDownloadUrl() {
            String url = "";
            this.version = this.version.replace(" ", "");
            if (this.version == null || this.version.isEmpty()) {
                this.version = "''";
            }

            if (this.version.contains("-rc.")) {
                url = "https://downloads.traceable.ai/cli/rc/" + version + "/traceable-cli-" + version + "-" + arch;
                filename = "traceable-cli-" + version + "-" + arch;
            } else if (this.version.equals("latest") || this.version.equals("''")) {
                url = "https://downloads.traceable.ai/cli/release/latest/traceable-cli-latest-" + arch;
                filename = "traceable-cli-latest-" + arch;
            } else {
                url = "https://downloads.traceable.ai/cli/release/" + version + "/traceable-cli-" + version + "-"
                    + arch;
                filename = "traceable-cli-" + version + "-" + arch;
            }

            return url;
        }

        private void unTar(String filepath) throws IOException, InterruptedException {
            String[] command = {"tar", "-xvf", filepath, "--directory", this.workspacePath};
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            p.waitFor();
        }
    }

    private static final class CopyScript implements FileCallable<String> {

        private final String scriptPath;

        public CopyScript(String scriptPath) {
            this.scriptPath = scriptPath;
        }

        @Override
        public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {

            String bundledScript = CharStreams.toString(new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream(this.scriptPath)), Charsets.UTF_8));

            File tempFile = File.createTempFile(
                "script_" + this.scriptPath.replaceAll(".sh", "") + "_"
                    + UUID.randomUUID().toString(),
                ".sh");

            BufferedWriter x = com.google.common.io.Files.newWriter(tempFile, Charsets.UTF_8);
            x.write(bundledScript);
            x.close();

            return tempFile.getAbsolutePath();
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {
            return;
        }
    }

    private static final class RunScript implements FileCallable<String> {

        private final TaskListener listener;
        private final String scriptPath;
        private static String scanId = null;
        private String[] args;
        private final String caller;

        RunScript(TaskListener listener, String scriptPath, String[] args, String caller) {
            this.listener = listener;
            this.scriptPath = scriptPath;
            this.args = args;
            this.caller = caller;
        }

        @Override
        public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {

            List<String> command = new ArrayList<>();
            command.add("/bin/bash");
            command.add(scriptPath);

            for (int i = 0; i < args.length; i++) {
                if (!StringUtils.isEmpty(args[i])) {
                    args[i] = args[i].replace(" ", "");
                }

                if (args[i] != null && !args[i].isEmpty()) command.add(args[i]);
                else command.add("''");
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            logOutput(p.getInputStream(), "");
            logOutput(p.getErrorStream(), "Error: ");
            p.waitFor();

            return RunScript.scanId;
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {
            return;
        }

        private void logOutput(InputStream inputStream, String prefix) {
            new Thread(() -> {
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8);
                while (scanner.hasNextLine()) {
                    synchronized (this) {
                        String line = scanner.nextLine();
                        // Extract the scan ID from the cli output of scan init command.
                        if (prefix.isEmpty() && line.contains("Running scan with ID")) {
                            String[] tokens = line.split(" ");
                            RunScript.scanId = tokens[tokens.length - 1].substring(0, 36);
                        }

                        if (!caller.equals("abortScan")) {
                            listener.getLogger().println(prefix + line);
                        }
                    }
                }
                scanner.close();
            }).start();
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private final String STEP_NAME = "Traceable AST - Initialize and Run";

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
