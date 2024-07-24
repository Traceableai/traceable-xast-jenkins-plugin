package io.jenkins.plugins.traceable.ast;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TraceableApiInspectorStepBuilder extends Builder implements SimpleBuildStep {


    private String repoPath;
    private String specFilePath;
    private  String traceableServer;
    private String traceableToken;
    private String report ;
    public String getRepoPath() {
        return repoPath;
    }

    public String getSpecFilePath() {
        return specFilePath;
    }

    public String getTraceableServer() { return  traceableServer; }
    public String getTraceableToken() { return  traceableToken; }

    @DataBoundSetter
    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    @DataBoundSetter
    public void setSpecFilePath(String specFilePath) {
        this.specFilePath = specFilePath;
    }


    @DataBoundSetter
    public void setTraceableServer(String traceableServer) { this.traceableServer = traceableServer; }

    @DataBoundSetter
    public void setTraceableToken(String traceableToken) {this.traceableToken = traceableToken;}

    @DataBoundConstructor
    public TraceableApiInspectorStepBuilder() {
    }


    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        report = "";
        String scriptPath = "shell_scripts/api_inspector.sh";
        String repoWorkspacePath = workspace.getRemote();
        if(!StringUtils.isBlank(repoPath)) {
            repoWorkspacePath += repoPath;
        }
        if(StringUtils.isBlank(specFilePath)) {
            String findSpecScriptPath = "shell_scripts/spec_finder.sh";
            String specFilePaths = runScript(findSpecScriptPath ,new String[]{repoWorkspacePath} , listener, false);
            String[] specFilePathsList = specFilePaths.split("\n");
            if(specFilePathsList.length==0) {
                listener.getLogger().println("No Open Api Spec Found at " + repoWorkspacePath);
            } else {
                listener.getLogger().println("Found open api specs at" + repoWorkspacePath + " : \n" + specFilePaths);
            }

            for (String specFile : specFilePathsList) {
                String[] args = new String[]{
                        traceableServer, traceableToken, specFile
                };
                String newOpenApiSpecString = "========================================\nUploading open api spec : " + specFile + "\n";
                listener.getLogger().println(newOpenApiSpecString);
                report += newOpenApiSpecString;
                runScript(scriptPath, args, listener, true);
            }
        } else {
            String[] args = new String[]{
                    traceableServer, traceableToken, specFilePath
            };
            runScript(scriptPath, args, listener, true);
        }
        run.addAction(new TraceableApiInspectorReportAction(report));
    }

    private String runScript(String scriptPath, String[] args, TaskListener listener, boolean printLogsToConsole) {
        try{
            String returnOutput = "";
            // Read the bundled script as string
            String bundledScript = CharStreams.toString(
                    new InputStreamReader(getClass().getResourceAsStream(scriptPath), StandardCharsets.UTF_8));
            // Create a temp file with uuid appended to the name just to be safe
            File tempFile = File.createTempFile("script_" + scriptPath.replaceAll(".sh", "")+ "_" + UUID.randomUUID().toString(), ".sh");
            // Write the string to temp file
            BufferedWriter x = Files.newWriter(tempFile,  Charsets.UTF_8);
            x.write(bundledScript);
            x.close();
            // Create an array for the command and its arguments
            String[] command = new String[args.length + 2];
            command[0] = "/bin/bash";
            command[1] = tempFile.getAbsolutePath();

            for (int i = 0; i < args.length; i++) {
                if (args[i] != null && !args[i].isEmpty()) {
                    command[i + 2] = args[i];
                } else {
                    command[i + 2] = "";
                }
            }
            // Execute the command
            Process pb = Runtime.getRuntime().exec(command);
            if(printLogsToConsole) {
                logOutput(pb.getInputStream(), "", listener);
                logOutput(pb.getErrorStream(), "Error: ", listener);
            } else {
                Scanner scanner = new Scanner(pb.getInputStream(), "UTF-8");
                while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        returnOutput += line + "\n";
                }
                scanner.close();
            }
            pb.waitFor();
            boolean deleted_temp = tempFile.delete();
            if(!deleted_temp) {
                throw new FileNotFoundException("Temp script file not found");
            }
            return returnOutput;

        } catch (Exception e){
            log.error("Exception in running {} script : {}", scriptPath, e);
            e.printStackTrace();
        }
        return "";
    }

    private void logOutput(InputStream inputStream, String prefix, TaskListener listener) {
        new Thread(() -> {
            Scanner scanner = new Scanner(inputStream, "UTF-8");
            while (scanner.hasNextLine()) {
                synchronized (this) {
                    String line = scanner.nextLine();
                    report += line + "\n";
                    listener.getLogger().println(prefix + line);
                }
            }
            scanner.close();
        }).start();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String STEP_NAME = "Traceable API Inspector";

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

