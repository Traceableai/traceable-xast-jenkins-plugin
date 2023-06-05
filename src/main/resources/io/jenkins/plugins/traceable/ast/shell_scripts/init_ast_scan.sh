#!/bin/bash
export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
dockerEnv=''
if  [[ -n ${19} ]] && [[ ${19} != "''" ]]
then
  export TRACEABLE_ROOT_CA_FILE_NAME=${19}
  dockerEnv=$dockerEnv' --env TRACEABLE_ROOT_CA_FILE_NAME '
fi
if  [[ -n ${20} ]] && [[ ${20} != "''" ]]
then
  export TRACEABLE_CLI_CERT_FILE_NAME=${20}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_CERT_FILE_NAME '
fi
if  [[ -n ${21} ]] && [[ ${21} != "''" ]]
then
  export TRACEABLE_CLI_KEY_FILE_NAME=${21}
  dockerEnv=$dockerEnv' --env TRACEABLE_CLI_KEY_FILE_NAME '
fi

setLocalCli=$1
traceableCliBinaryLocation=$2

if [ "$setLocalCli" = false ]
then
  traceableCliBinaryLocation='docker run -v ~/.traceable:/app/userdata '$dockerEnv$traceableCliBinaryLocation
fi

scanInitCmd=$traceableCliBinaryLocation' ast scan init'
optionsArr=('--scan-name' '--traffic-env' '--token' '--plugins' '--include-url-regex' '--exclude-url-regex' '--target-url' '--traceable-server' '--scan-timeout' '--build-id' '--build-url' ' --reference-env' '--openapi-spec-ids' '--openapi-spec-files' '--postman-collection' '--postman-environment')
stringArr=('--include-url-regex' '--exclude-url-regex' )

#Iterating the options available from options array and filling them with the arguments received in order
iterator=0
for option in "${@:3:16}"
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

# Run the command
$scanInitCmd