export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
traceableCliBinaryLocation=/Users/dhruvsinghal/IdeaProjects/active-security-testing/ast-cli/dist/traceable/bin/traceable
scanInitCmd=$traceableCliBinaryLocation' ast scan initAndRun'
optionsArr=('-n' '-t' '--token' '--plugins' '-i' '-e' '-u' '--traceable-server' '--idle-timeout' '--scan-timeout' '-bi' '-bu')
stringArr=('-i' '-e' )
stringSensitive
echo $scanInitCmd
iterator=0
for option in "$@"
do
  if [ -z "$option" ]
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
echo $scanInitCmd
$scanInitCmd