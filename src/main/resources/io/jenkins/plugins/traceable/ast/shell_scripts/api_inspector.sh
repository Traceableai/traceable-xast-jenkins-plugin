export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8
export LANGUAGE=en_US.UTF-8
api_inspector='/plugin/traceable/binaries/api_inspector --spec_file '${1}
if [ -z "$2" ] || [ "$2" = "''" ]
  then
    echo "checks file not provided"
  else
    api_inspector=$api_inspector' --check_file '${2}
fi
eval "$api_inspector"