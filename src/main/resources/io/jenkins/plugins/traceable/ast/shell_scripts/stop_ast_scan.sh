#!/bin/bash
export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
dockerEnv=''
if  [[ -n ${3} ]] && [[ ${3} != "''" ]]
then
  export TRACEABLE_ROOT_CA_FILE_NAME=${3}
  dockerEnv=$dockerEnv' --env TRACEABLE_ROOT_CA_FILE_NAME '
fi
if  [[ -n ${4} ]] && [[ ${4} != "''" ]]
then
  export TRACEABLE_CLI_CERT_FILE_NAME=${4}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_CERT_FILE_NAME '
fi
if  [[ -n ${5} ]] && [[ ${5} != "''" ]]
then
  export TRACEABLE_CLI_KEY_FILE_NAME=${5}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_KEY_FILE_NAME '
fi

#setLocalCli=$1
traceableCliBinaryLocation=$1

#if [ "$setLocalCli" = false ]
#then
#  docker volume create traceable_ast
#  traceableCliBinaryLocation='docker run -v traceable_ast:/app/userdata '$dockerEnv$traceableCliBinaryLocation
#fi

#Running command to stop the scan with the specific scan ID.
$traceableCliBinaryLocation ast scan stop --id $2
echo "stopping scan: $2"