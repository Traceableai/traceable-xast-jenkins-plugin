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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jenkins.tasks.SimpleBuildStep;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@Slf4j
public class TraceableASTInitStepBuilder extends Builder implements SimpleBuildStep {

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

    public String getScanTimeout() {
        return scanTimeout;
    }

    public static Boolean getScanEnded() {
        return scanEnded;
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

    @DataBoundConstructor
    public TraceableASTInitStepBuilder() {
        traceableCliBinaryLocation = null;
        this.includeAllEndPoints = (Boolean) true;
        this.xastLive = (Boolean) true;
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
    public void setTrafficType(TrafficType trafficType) {
        this.trafficType = trafficType;
        switch (trafficType) {
            case XAST_LIVE:
                this.xastLive = (Boolean) true;
                this.xastReplay = (Boolean) false;
                break;
            case XAST_REPLAY:
                this.xastLive = (Boolean) false;
                this.xastReplay = (Boolean) true;
                break;
            default:
                this.xastLive = (Boolean) false;
                this.xastReplay = (Boolean) false;
        }
    }

    @DataBoundSetter
    public void setAssets(Assets assets) {
        this.assets = assets;
        if (assets != Assets.AllEndpoints) {
            this.includeAllEndPoints = (Boolean) false;
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
    public void setScanName(String scanName) {
        this.scanName = scanName;
    }

    @DataBoundSetter
    public void setTestEnvironment(String testEnvironment) {
        this.testEnvironment = testEnvironment;
    }

    @DataBoundSetter
    public static void setClientToken(Secret clientToken) {
        TraceableASTInitStepBuilder.clientToken = clientToken;
    }

    @DataBoundSetter
    public static void setTraceableCliBinaryLocation(String traceableCliBinaryLocation) {
        TraceableASTInitStepBuilder.traceableCliBinaryLocation = traceableCliBinaryLocation;
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
    public void setScanTimeout(String scanTimeout) {
        this.scanTimeout = scanTimeout;
    }

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
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {
        workspacePathString = workspace.getRemote();
        TraceableASTInitStepBuilder.setScanEnded(Boolean.FALSE);
        TraceableASTInitAndRunStepBuilder.setClientToken(null);

        if (cliSource.equals("download")) {
            downloadTraceableCliBinary(workspace, listener);
        } else if (cliSource.equals("localpath")) {
            if (cliField == null || cliField.equals("")) {
                throw new InterruptedException("Location of traceable cli binary not provided.");
            } else {
                traceableCliBinaryLocation = cliField;
            }
        }

        initScan(run, workspace, listener);
    }

    // Download the binary if the location of the binary is not given.
    private void downloadTraceableCliBinary(FilePath workspace, TaskListener listener)
            throws IOException, InterruptedException {
        workspace.act(new DownloadTraceableCliBinary(this.cliField));
        traceableCliBinaryLocation = workspace.getRemote() + "/traceable";
    }

    // Run the scan.
    private void initScan(Run<?, ?> run, FilePath workspace, TaskListener listener)
            throws IOException, InterruptedException {
        String replay = String.valueOf(xastReplay != null && xastReplay);
        String allEndPoint = String.valueOf(includeAllEndPoints != null && includeAllEndPoints);

        String scriptPath = "shell_scripts/init_ast_scan.sh";
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

        String tempFilePath = workspace.act(new CopyScript(scriptPath));
        runScript(workspace, listener, tempFilePath, args);
        deleteScript(workspace, listener, tempFilePath);
    }

    private void runScript(FilePath workspace, TaskListener listener, String scriptPath, String[] args) {

        try {
            List<String> command = new ArrayList<>();
            Launcher nodeLaucher = workspace.createLauncher(listener);

            command.add("/bin/bash");
            command.add(scriptPath);

            for (int i = 0; i < args.length; i++) {
                if (!StringUtils.isEmpty(args[i])) {
                    args[i] = args[i].replace(" ", "");
                }

                if (args[i] != null && !args[i].isEmpty()) command.add(args[i]);
                else command.add("''");
            }

            nodeLaucher
                    .launch()
                    .cmds(command)
                    .stdout(listener.getLogger())
                    .stderr(listener.getLogger())
                    .join();

        } catch (Exception e) {
            log.error("Exception in running {} script : {}", scriptPath, e);
            e.printStackTrace();
        }
    }

    private void deleteScript(FilePath workspace, TaskListener listener, String scriptPath)
            throws IOException, InterruptedException {
        String[] command = {"rm", scriptPath};
        Launcher nodeLaucher = workspace.createLauncher(listener);

        nodeLaucher
                .launch()
                .cmds(command)
                .stdout(listener.getLogger())
                .stderr(listener.getLogger())
                .join();
    }

    // Download the binary at the workspace node if location is not given
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
            // We want this to be executable on all type of nodes
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

        private String scriptPath;

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

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private final String STEP_NAME = "Traceable AST - Initialize";

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
