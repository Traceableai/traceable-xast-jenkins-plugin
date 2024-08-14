package io.jenkins.plugins.traceable.ast.scan.utils;

import hudson.remoting.VirtualChannel;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import jenkins.MasterToSlaveFileCallable;

public class DownloadTraceableCliBinary extends MasterToSlaveFileCallable<Void> {

    private String workspacePath;
    private String version;
    private String osName;
    private String arch;
    private String filename;

    public DownloadTraceableCliBinary(String version) {
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
            url = "https://downloads.traceable.ai/cli/release/" + version + "/traceable-cli-" + version + "-" + arch;
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
