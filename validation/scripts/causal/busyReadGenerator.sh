if [ $# -lt 10 ]
then
  echo "Usage: busyReadGenerator.sh <imageTag> <totalPartitions> <regionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+ <expectedWrites> <keysPerRead> <key>+"
  exit 1
fi

NODE=busyReadGenerator
BUCKET_SUFFIX=-reference-architecture
IMAGE="dianaamfreitas/dissertation:${1}"
N_PARTITIONS=$2
REGION_PARTITIONS=$3
READ_ADDRESS="${4} ${5}"
REST="${@:6}"

docker run --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $REGION_PARTITIONS $READ_ADDRESS $REST
