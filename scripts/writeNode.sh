if [ $# -lt 4 ] || [ $# -gt 4 ]
then
  echo "Usage: writeNode.sh <imageTag> <totalPartitions> <port> <partitionId>"
  exit 1
fi

NODE=writeNode
BUCKET_SUFFIX=-reference-architecture
IMAGE="dianaamfreitas/dissertation:${1}"
N_PARTITIONS=$2
PORT=$3
PARTITION_ID=$4

docker run --name $NODE -p $PORT:$PORT --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $PARTITION_ID $PORT
