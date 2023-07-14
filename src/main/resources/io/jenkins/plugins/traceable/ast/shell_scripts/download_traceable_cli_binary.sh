#!/bin/bash

export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8

cd ${1}
version=${2}

if [[ "$version"  = *"-rc."* ]]
then
  curl -OL https://downloads.traceable.ai/cli/rc/"${version}"/traceable-cli-"${version}"-linux-x86_64.tar.gz
  tar -xvf ./traceable-cli-"${version}"-linux-x86_64.tar.gz
elif [ "$version" = "latest" ] || [ -z "$version" ] || [ "$version" = "''" ]
then
  curl -OL https://downloads.traceable.ai/cli/release/latest/traceable-cli-latest-linux-x86_64.tar.gz
  tar -xvf ./traceable-cli-latest-linux-x86_64.tar.gz
else
    curl -OL https://downloads.traceable.ai/cli/release/"${version}"/traceable-cli-"${version}"-linux-x86_64.tar.gz
    tar -xvf ./traceable-cli-"${version}"-linux-x86_64.tar.gz
fi
