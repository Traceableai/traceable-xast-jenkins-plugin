export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
traceableCliBinaryLocation=/Users/dhruvsinghal/IdeaProjects/active-security-testing/ast-cli/dist/traceable/bin/traceable
$traceableCliBinaryLocation ast scan show --id $1
echo "stopping scan: $1"