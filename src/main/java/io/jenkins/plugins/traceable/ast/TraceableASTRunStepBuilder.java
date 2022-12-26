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
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.UUID;

public class TraceableASTRunStepBuilder extends Builder implements SimpleBuildStep {

    private String idleTimeout;
    private String maxRetries;

    public String getIdleTimeout() { return idleTimeout; }
    public String getMaxRetries() { return maxRetries; }

    @DataBoundConstructor
    public TraceableASTRunStepBuilder() {}

    @DataBoundSetter
    public void setIdleTimeout(String idleTimeout) { this.idleTimeout = idleTimeout; }

    @DataBoundSetter
    public void setMaxRetries(String maxRetries) { this.maxRetries = maxRetries; }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        runScan(listener, run);
        if (TraceableASTInitStepBuilder.getScanId() != null) {
            abortScan(listener);
        }
        TraceableASTInitStepBuilder.setScanEnded(true);
    }

    // Download the binary if the location of the binary is not given.
    private void downloadTraceableCliBinary(TaskListener listener) {
        String script_path = "shell_scripts/download_traceable_cli_binary.sh";
        String[] args = new String[]{};
        runScript(script_path, args, listener, "downloadTraceableCliBinary");
    }

    // Run the scan.
    private void runScan( TaskListener listener, Run<?, ?> run ){
        String scriptPath = "shell_scripts/run_ast_scan.sh";
        String[] args =
                new String[] {
                        TraceableASTInitStepBuilder.getSelectedLocalCliEnvironment().toString(),
                        TraceableASTInitStepBuilder.getTraceableCliBinaryLocation(),
                        TraceableASTInitStepBuilder.getClientToken(),
                        idleTimeout,
                        maxRetries,
                        TraceableASTInitStepBuilder.getTraceableRootCaFileName(),
                        TraceableASTInitStepBuilder.getTraceableCliCertFileName(),
                        TraceableASTInitStepBuilder.getTraceableCliKeyFileName()
                };
        runScript(scriptPath, args, listener, "runScan");
    }

    //Stop the scan with the given scan ID.
    private void abortScan(TaskListener listener) {
        String scriptPath = "shell_scripts/stop_ast_scan.sh";
        String[] args = new String[]{
                TraceableASTInitStepBuilder.getTraceableCliBinaryLocation().toString(),
                TraceableASTInitStepBuilder.getTraceableCliBinaryLocation(),
                TraceableASTInitStepBuilder.getScanId()
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
            String execScript = "/bin/bash " + tempFile.getAbsolutePath();
            for(int i=0;i<args.length;i++) {
                if(args[i]!=null && !args[i].equals(""))
                    execScript += " " + args[i];
                else execScript += " ''";
            }
            Process pb = Runtime.getRuntime().exec(execScript);
            logOutput(pb.getInputStream(), "", listener, caller);
            logOutput(pb.getErrorStream(), "Error: ",listener, caller);
            pb.waitFor();
            boolean deleted_temp = tempFile.delete();
            if(!deleted_temp) {
                throw new FileNotFoundException("Temp file not found");
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
                    TraceableASTInitStepBuilder.setScanId(tokens[tokens.length - 1]);
                  }
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

        private String STEP_NAME = "Traceable AST - Run";

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
