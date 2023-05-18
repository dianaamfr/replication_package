if [ $# -lt 7 ]
then
  echo "Usage: cli.sh <imageTag> <totalPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>)+"
  exit 1
fi

NODE=clientInterface
BUCKET_SUFFIX=-reference-architecture
IMAGE="dianaamfreitas/dissertation:${1}"
N_PARTITIONS=$2
READ_ADDRESS="${3} ${4}"
REST="${@:5}"

docker run -i --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $READ_ADDRESS $REST
