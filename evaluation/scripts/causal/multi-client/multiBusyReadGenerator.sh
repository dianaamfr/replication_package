if [ $# -lt 13 ]
then
  echo "Usage: multiBusyReadGenerator.sh <imageTag> <totalPartitions> <regionReadNodes> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <readTime> <keysPerRead> <keysPerPartition> <readClients>"
  exit 1
fi

NODE=multiBusyReadGenerator
BUCKET_SUFFIX=-reference-architecture
IMAGE="dianaamfreitas/dissertation:${1}"
N_PARTITIONS=$2
REST="${@:3}"

docker run --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $REST
