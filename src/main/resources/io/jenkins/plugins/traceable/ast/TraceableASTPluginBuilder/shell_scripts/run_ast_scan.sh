export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
traceableCliBinaryLocation=/Users/dhruvsinghal/IdeaProjects/active-security-testing/ast-cli/dist/traceable/bin/traceable
scanInitCmd=$traceableCliBinaryLocation' ast scan initAndRun'
optionsArr=('-n' '-t' '--token' '--traceable_server' '--idle-timeout' '--scan-timeout')
echo $scanInitCmd
iterator=0
for option in "$@"
do
  if [ -z "$option" ]
  then
    echo "${optionsArr[$iterator]}" is Null
  else
    scanInitCmd=$scanInitCmd" "${optionsArr[$iterator]}" "$option
  fi
  iterator=$(($iterator+1))
done
echo $scanInitCmd
$scanInitCmd