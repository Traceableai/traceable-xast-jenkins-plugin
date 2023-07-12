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
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.UUID;

@Slf4j
public class TraceableApiInspectorStepBuilder extends Builder implements SimpleBuildStep {


    private String specFilePath;

    private String checksFilePath;
    private String report ;

    public String getSpecFilePath() {
        return specFilePath;
    }

    public String getChecksFilePath() {
        return checksFilePath;
    }

    @DataBoundSetter
    public void setSpecFilePath(String specFilePath) {
        this.specFilePath = specFilePath;
    }

    @DataBoundSetter
    public void setChecksFilePath(String checksFilePath) {
        this.checksFilePath = checksFilePath;
    }

    @DataBoundConstructor
    public TraceableApiInspectorStepBuilder() {
    }


    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        report = "";
        String scriptPath = "shell_scripts/api_inspector.sh";
        String[] args = new String[]{
                specFilePath, checksFilePath
        };
        runScript(scriptPath , args, listener);
        run.addAction(new TraceableApiInspectorReportAction(report));
    }

    private void runScript(String scriptPath, String[] args, TaskListener listener) {
        try{
            // Read the bundled script as string
            String bundledScript = CharStreams.toString(
                    new InputStreamReader(getClass().getResourceAsStream(scriptPath), StandardCharsets.UTF_8));
            // Create a temp file with uuid appended to the name just to be safe
            File tempFile = File.createTempFile("script_" + scriptPath.replaceAll(".sh", "")+ "_" + UUID.randomUUID().toString(), ".sh");
            // Write the string to temp file
            BufferedWriter x = Files.newWriter(tempFile,  Charsets.UTF_8);
            x.write(bundledScript);
            x.close();
            StringBuilder execScript = new StringBuilder("/bin/bash " + tempFile.getAbsolutePath());
            for(int i=0;i<args.length;i++) {
                if(args[i]!=null && !args[i].equals(""))
                    execScript.append(" ").append(args[i]);
                else execScript.append(" ''");
            }
            Process pb = Runtime.getRuntime().exec(execScript.toString());
            logOutput(pb.getInputStream(), "",listener);
            logOutput(pb.getErrorStream(), "Error: ",listener);
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

