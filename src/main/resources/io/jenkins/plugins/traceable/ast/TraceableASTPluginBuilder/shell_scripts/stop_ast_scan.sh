export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8
traceableCliBinaryLocation=$1

#Running command to stop the scan with the specific scan ID.
$traceableCliBinaryLocation ast scan stop --scan-id $2
echo "stopping scan: $2"