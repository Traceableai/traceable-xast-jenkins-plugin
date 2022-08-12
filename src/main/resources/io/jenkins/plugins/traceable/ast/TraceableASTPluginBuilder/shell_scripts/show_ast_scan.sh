export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
traceableCliBinaryLocation=$1

#Running command to generate the output of the scan with the specific scan-id
$traceableCliBinaryLocation ast scan show --id $2
echo "stopping scan: $2"