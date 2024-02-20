#!/bin/bash

dockerEnv=''
if  [[ -n ${21} ]] && [[ ${21} != "''" ]]
then
  export TRACEABLE_ROOT_CA_FILE_NAME=${21}
  dockerEnv=$dockerEnv' --env TRACEABLE_ROOT_CA_FILE_NAME '
fi
if  [[ -n ${22} ]] && [[ ${22} != "''" ]]
then
  export TRACEABLE_CLI_CERT_FILE_NAME=${22}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_CERT_FILE_NAME '
fi
if  [[ -n ${23} ]] && [[ ${23} != "''" ]]
then
  export TRACEABLE_CLI_KEY_FILE_NAME=${23}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_KEY_FILE_NAME '
fi

#setLocalCli=$1
traceableCliBinaryLocation=$1
#if [ "$setLocalCli" = false ]
#then
#  traceableCliBinaryLocation="docker run -v $HOME/.traceable:/app/userdata "$dockerEnv$traceableCliBinaryLocation
#fi

scanInitCmd=$traceableCliBinaryLocation' ast scan initAndRun'
optionsArr=('--scan-name' '--scan-suite' '--traffic-env' '--token' '--policy' '--plugins' '--include-url-regex' '--exclude-url-regex' '--target-url' '--traceable-server' '--idle-timeout' '--scan-timeout' '--build-id' '--build-url' '--reference-env' '--max-retries' '--openapi-spec-ids' '--openapi-spec-files' '--postman-collection' '--postman-environment' '--traceable-platform-agent')
stringArr=('--include-url-regex' '--exclude-url-regex' )

#Iterating the options available from options array and filling them with the arguments received in order
iterator=0
for option in "${@:2:19}"
do
  if [ -z "$option" ] || [ "$option" = "''" ]
  then
    echo "${optionsArr[$iterator]}" is Null
  else
    presentInStringArr=0
    for subOption in "${stringArr[@]}"
    do
      if [ "$subOption" == "${optionsArr[$iterator]}" ]
      then
        presentInStringArr=1
      fi
    done
    if [ $presentInStringArr -eq 0 ]
    then
      scanInitCmd=$scanInitCmd" "${optionsArr[$iterator]}" "${option}
    else
      scanInitCmd=$scanInitCmd" "${optionsArr[$iterator]}" "\"${option}\"
    fi
  fi
  iterator=$(($iterator+1))
done
if [ -z "${24}" ] || [ "${24}" = "''" ]
then
  scanInitCmd=$scanInitCmd" --config-file "${24}
fi
echo "$scanInitCmd"
# Run the command
eval "$scanInitCmd"