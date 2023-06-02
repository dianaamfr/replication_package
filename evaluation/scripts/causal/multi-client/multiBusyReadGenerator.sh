if [ $# -lt 12 ]
then
  echo "Usage: multiBusyReadGenerator.sh <imageTag> <totalPartitions> <regionReadNodes> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <readTime> <keysPerRead> <keysPerPartition> <readClients>"
  exit 1
fi

NODE=multiBusyReadGenerator
BUCKET_SUFFIX=-reference-architecture
IMAGE="dianaamfreitas/dissertation:${1}"
N_PARTITIONS=$2
REGION_READ_NODES=$3
REGION_PARTITIONS=$4
READ_ADDRESS="${5} ${6}"
REST="${@:7}"

docker run --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $REGION_PARTITIONS $READ_ADDRESS $REST
