#!/bin/bash

export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
dockerEnv=''
if  [[ -n ${4} ]] && [[ ${4} != "''" ]]
then
  export TRACEABLE_ROOT_CA_FILE_NAME=${4}
  dockerEnv=$dockerEnv' --env TRACEABLE_ROOT_CA_FILE_NAME '
fi
if  [[ -n ${5} ]] && [[ ${5} != "''" ]]
then
  export TRACEABLE_CLI_CERT_FILE_NAME=${5}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_CERT_FILE_NAME '
fi
if  [[ -n ${6} ]] && [[ ${6} != "''" ]]
then
  export TRACEABLE_CLI_KEY_FILE_NAME=${6}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_KEY_FILE_NAME '
fi

traceableCliBinaryLocation=$1

#if [ "$setLocalCli" = false ]
#then
#  docker volume create traceable_ast
#  traceableCliBinaryLocation='docker run -v traceable_ast:/app/userdata '$dockerEnv$traceableCliBinaryLocation
#fi

#Running command to generate the output of the scan with the specific scan-id
showcmd="$traceableCliBinaryLocation ast scan report --id $2 --token $3 --wait --exit-code"
showcmd