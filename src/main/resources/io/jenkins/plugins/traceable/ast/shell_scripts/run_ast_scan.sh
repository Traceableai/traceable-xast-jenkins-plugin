#!/bin/bash
export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
dockerEnv=''
if  [[ -n ${6} ]] && [[ ${6} != "''" ]]
then
  export TRACEABLE_ROOT_CA_FILE_NAME=${6}
  dockerEnv=$dockerEnv' --env TRACEABLE_ROOT_CA_FILE_NAME '
fi
if  [[ -n ${7} ]] && [[ ${7} != "''" ]]
then
  export TRACEABLE_CLI_CERT_FILE_NAME=${7}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_CERT_FILE_NAME '
fi
if  [[ -n ${8} ]] && [[ ${8} != "''" ]]
then
  export TRACEABLE_CLI_KEY_FILE_NAME=${8}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_KEY_FILE_NAME '
fi

#setLocalCli=$1
traceableCliBinaryLocation=$1

#if [ "$setLocalCli" = false ]
#then
#  traceableCliBinaryLocation='docker run -v ~/.traceable:/app/userdata '$dockerEnv$traceableCliBinaryLocation
#fi

scanRunCmd=$traceableCliBinaryLocation' ast scan run'
optionsArr=('--token'  '--idle-timeout' '--max-retries' '--should-upload-logs')

iterator=0
for option in "${@:2:4}"
do
  if [ -z "$option" ] || [ "$option" = "''" ]; then
    echo "${optionsArr[$iterator]}" is Null
  else
    if [[ "${optionsArr[$iterator]}" = '--should-upload-logs' ]]; then
      if [[ "$option" = "true" ]]; then
        scanRunCmd=$scanRunCmd" "${optionsArr[$iterator]}
      fi
    else
      scanRunCmd=$scanRunCmd" "${optionsArr[$iterator]}" "$option
    fi
  fi

  iterator=$((iterator + 1))
done

echo "$scanRunCmd"

# Run the command
eval "$scanRunCmd"