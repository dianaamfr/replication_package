if [ $# -lt 12 ]
then
  echo "Usage: multiBusyReadGenerator.sh <imageTag> <totalPartitions> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <readTime> <keysPerRead> <keysPerPartition> <readClients>"
  exit 1
fi

NODE=multiBusyReadGenerator
BUCKET_SUFFIX=-reference-architecture
IMAGE="dianaamfreitas/dissertation:${1}"
N_PARTITIONS=$2
REGION_PARTITIONS=$3
READ_ADDRESS="${4} ${5}"
REST="${@:6}"

docker run --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $REGION_PARTITIONS $READ_ADDRESS $REST
