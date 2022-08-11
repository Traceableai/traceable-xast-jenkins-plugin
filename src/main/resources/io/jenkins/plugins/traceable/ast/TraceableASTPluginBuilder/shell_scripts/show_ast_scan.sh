export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
traceableCliBinaryLocation=$1
$traceableCliBinaryLocation ast scan show --id $2
echo "stopping scan: $2"