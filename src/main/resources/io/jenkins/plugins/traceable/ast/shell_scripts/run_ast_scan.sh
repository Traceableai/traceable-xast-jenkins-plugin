export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8

traceableCliBinaryLocation=$1
scanInitCmd=$traceableCliBinaryLocation' ast scan initAndRun'
optionsArr=('-n' '-t' '--token' '--plugins' '-i' '-e' '-u' '--traceable-server' '--idle-timeout' '--scan-timeout' '-bi' '-bu')
stringArr=('-i' '-e' )

#Iterating the options available from options array and filling them with the arguments received in order
iterator=0
for option in "${@:2}"
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
echo $scanInitCmd
$scanInitCmd