#!/bin/bash

export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
dockerEnv=''
if  [[ -n ${5} ]] && [[ ${5} != "''" ]]
then
  export TRACEABLE_ROOT_CA_FILE_NAME=${5}
  dockerEnv=$dockerEnv' --env TRACEABLE_ROOT_CA_FILE_NAME '
fi
if  [[ -n ${6} ]] && [[ ${6} != "''" ]]
then
  export TRACEABLE_CLI_CERT_FILE_NAME=${6}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_CERT_FILE_NAME '
fi
if  [[ -n ${7} ]] && [[ ${7} != "''" ]]
then
  export TRACEABLE_CLI_KEY_FILE_NAME=${7}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_KEY_FILE_NAME '
fi

setLocalCli=$1
traceableCliBinaryLocation=$2

if [ "$setLocalCli" = false ]
then
  docker volume create traceable_ast
  traceableCliBinaryLocation='docker run -v traceable_ast:/app/userdata '$dockerEnv$traceableCliBinaryLocation
fi

#Running command to generate the output of the scan with the specific scan-id
$traceableCliBinaryLocation ast scan report --id $3 --token $4 --wait --output-format md --exit-code