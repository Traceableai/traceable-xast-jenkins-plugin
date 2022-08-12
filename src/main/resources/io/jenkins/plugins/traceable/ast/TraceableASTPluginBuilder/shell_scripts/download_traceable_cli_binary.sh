source ~/.bashrc
mkdir TraceableCLI
cd TraceableCLI
curl -H "X-JFrog-Art-Api:$JFROG_TOKEN" -O 'https://traceableai.jfrog.io/artifactory/pypi-local/traceablecli/0.1.322/traceablecli-0.1.322-py3-none-any.whl'
pip3 install --force-reinstall traceablecli* --prefix=./
docker login -u dhruv.singhal -p AKCp8nFvkrBCWhGiFiSgbmc8QPFvxbT1tHAhBDsx3QecWSADJFzscmNZtc4BuNXLh4HAJKHtY https://traceableai-docker-dev.jfrog.io/v2/traceable/traceable-cli