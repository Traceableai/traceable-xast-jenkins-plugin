package io.jenkins.plugins.traceable.ast;

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
import java.io.*;
import java.net.URL;
import jenkins.tasks.SimpleBuildStep;
import lombok.extern.slf4j.Slf4j;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

@Slf4j
public class SouvikTestStepBuilder extends Builder implements SimpleBuildStep {

    private String filename;

    @DataBoundConstructor
    public SouvikTestStepBuilder() {}

    @DataBoundSetter
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, EnvVars env, Launcher launcher, TaskListener listener)
            throws InterruptedException, IOException {

        String filepath = workspace.act(new DownloadTraceableCliBinary(filename));
        listener.getLogger().println(filepath);

    }

    // Download the binary at the workspace node if location is not given
    // Download the binary at the workspace node if location is not given
    private static final class DownloadTraceableCliBinary implements FileCallable<String> {

        private String workspacePath;
        private String version;
        private String osName;
        private String arch;
        private String filename;

        DownloadTraceableCliBinary(String version) {
            this.version = version;
        }

        @Override
        public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
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
            return filepath;
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
            String[] command = {"tar", "-xvf", filepath, "-C", this.workspacePath};
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            p.waitFor();
        }
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String STEP_NAME = "Souvik Test";

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
