#!/bin/bash
export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
dockerEnv=''
if  [[ -n ${2} ]] && [[ ${2} != "''" ]]
then
  export TRACEABLE_ROOT_CA_FILE_NAME=${2}
  dockerEnv=$dockerEnv' --env TRACEABLE_ROOT_CA_FILE_NAME '
fi
if  [[ -n ${3} ]] && [[ ${3} != "''" ]]
then
  export TRACEABLE_CLI_CERT_FILE_NAME=${3}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_CERT_FILE_NAME '
fi
if  [[ -n ${4} ]] && [[ ${4} != "''" ]]
then
  export TRACEABLE_CLI_KEY_FILE_NAME=${4}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_KEY_FILE_NAME '
fi

#setLocalCli=$1
traceableCliBinaryLocation=$1

#if [ "$setLocalCli" = false ]
#then
#  traceableCliBinaryLocation="docker run -v $HOME/.traceable:/app/userdata "$dockerEnv$traceableCliBinaryLocation
#fi

scanInitCmd=$traceableCliBinaryLocation' ast scan init'
optionsArr=('--scan-name' '--traffic-env' '--token' '--attack-policy' '--plugins' '--include-url-regex' '--exclude-url-regex' '--target-url' '--traceable-server' '--scan-timeout' '--openapi-spec-ids' '--openapi-spec-files' '--postman-collection' '--postman-environment' '--scan-suite' '--include-service-ids' '--include-endpoint-ids' '--include-endpoint-labels' '--hook-names' '--include-all-endpoints' '--xast-replay')
stringArr=('--include-url-regex' '--exclude-url-regex' )

#Iterating the options available from options array and filling them with the arguments received in order
iterator=0
for option in "${@:5:23}"
do
  # Check for "--include-all-endpoints" and its value separately
  if [[ "${optionsArr[$iterator]}" == "--include-all-endpoints" && "$option" == "true" ]]; then
    scanInitCmd="$scanInitCmd ${optionsArr[$iterator]}"
  # Check for "--xast-replay" and its value separately
  elif [[ "${optionsArr[$iterator]}" == "--xast-replay" && "$option" == "true" ]]; then
    scanInitCmd="$scanInitCmd ${optionsArr[$iterator]}"
  elif [ -z "$option" ] || [ "$option" = "''" ] || [ "$option" == "false" ]; then
    echo "${optionsArr[$iterator]} is Null"
  else
    presentInStringArr=0
    for subOption in "${stringArr[@]}"
    do
      if [ "$subOption" == "${optionsArr[$iterator]}" ]; then
        presentInStringArr=1
      fi
    done
    if [ $presentInStringArr -eq 0 ]; then
      scanInitCmd="$scanInitCmd ${optionsArr[$iterator]} ${option}"
    else
      scanInitCmd="$scanInitCmd ${optionsArr[$iterator]} \"${option}\""
    fi
  fi
  iterator=$((iterator + 1))
done

if [ -z "${24}" ] || [ "${24}" = "''" ]
then
  scanInitCmd=$scanInitCmd" --config-file "${24}
fi
echo "$scanInitCmd"
# Run the command
eval "$scanInitCmd"