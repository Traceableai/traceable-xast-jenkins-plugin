# Load bashrc to get the JFROG TOKEN
source ~/.bashrc

# Creating directory to contain the cli binary
mkdir TraceableCLI
cd TraceableCLI

#Download the cli wheel file from Jfrog
curl -H "X-JFrog-Art-Api:$JFROG_TOKEN" -O 'https://traceableai.jfrog.io/artifactory/pypi-local/traceablecli/0.1.322/traceablecli-0.1.322-py3-none-any.whl'

#Unzip the wheel file
pip3 install --force-reinstall traceablecli* --prefix=./
docker login -u dhruv.singhal -p AKCp8nFvkrBCWhGiFiSgbmc8QPFvxbT1tHAhBDsx3QecWSADJFzscmNZtc4BuNXLh4HAJKHtY https://traceableai-docker-dev.jfrog.io/v2/traceable/traceable-cli