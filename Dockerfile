# Use the official Jenkins image from Docker Hub
FROM jenkins/jenkins:lts

# Switch to root to install Docker CLI
USER root

# Install Docker CLI to allow Jenkins to run Docker commands
RUN apt-get update && apt-get install -y apt-transport-https \
    ca-certificates curl gnupg2 \
    software-properties-common
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
RUN add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/debian \
   $(lsb_release -cs) stable"
RUN apt-get update && apt-get install -y docker-ce-cli

# Switch back to the Jenkins user
USER jenkins

# Skip the initial setup wizard
ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false

# Expose the default Jenkins port
EXPOSE 8080

# Expose the port for attached slave agents
EXPOSE 50000

# Start the Jenkins server
CMD ["jenkins.sh"]
