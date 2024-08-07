export LC_ALL=en_US.UTF-8
export LANG=en_US.UTF-8
export LANGUAGE=en_US.UTF-8
api_inspector="${1}"' --spec_file '"${2}"
if [ -z "$3" ] || [ "$3" = "''" ]
  then
    echo "checks file not provided"
  else
    api_inspector=$api_inspector' --check_file '${3}
fi
eval "$api_inspector"