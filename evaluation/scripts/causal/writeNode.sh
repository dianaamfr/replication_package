if [ $# -lt 6 ]
then
  echo "Usage: writeNode.sh <imageTag> <totalPartitions> <port> <partitionId> (<readPort> <readIp>)+"
  exit 1
fi

NODE=writeNode
BUCKET_SUFFIX=-reference-architecture
CLOCK_REGION=eu-west-1
IMAGE="dianaamfreitas/dissertation:${1}"
N_PARTITIONS=$2
PORT=$3
PARTITION_ID=$4
REST="${@:5}"

docker run --name $NODE -p $PORT:$PORT --env NODE=$NODE --env CLOCK_REGION=$CLOCK_REGION --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $PARTITION_ID $PORT $REST
