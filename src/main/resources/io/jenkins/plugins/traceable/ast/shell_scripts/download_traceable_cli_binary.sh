#!/bin/bash

export LC_ALL=en_US.utf-8
export LANG=en_US.utf-8

cd ${1}
version=${2}

os_name=$(uname -s)
if [[ "$os_name" == "Darwin" ]];
then
    arch="macosx-x86_64.tar.gz"
else
    arch="linux-x86_64.tar.gz"
fi

if [[ "$version"  = *"-rc."* ]]
then
  curl -OL https://downloads.traceable.ai/cli/rc/"${version}"/traceable-cli-"${version}"-"${arch}"
  tar -xvf ./traceable-cli-"${version}"-"${arch}"
elif [ "$version" = "latest" ] || [ -z "$version" ] || [ "$version" = "''" ]
then
  curl -OL https://downloads.traceable.ai/cli/release/latest/traceable-cli-latest-"${arch}"
  tar -xvf ./traceable-cli-latest-"${arch}"
else
    curl -OL https://downloads.traceable.ai/cli/release/"${version}"/traceable-cli-"${version}"-"${arch}"
    tar -xvf ./traceable-cli-"${version}"-"${arch}"
fi
