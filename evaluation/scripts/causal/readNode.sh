if [ $# -lt 4 ]
then
  echo "Usage: readNode.sh <imageTag> <totalPartitions> <port> <regionPartitionsIds>"
  exit 1
fi

NODE=readNode
BUCKET_SUFFIX=-reference-architecture
CLOCK_REGION=eu-west-1
IMAGE="dianaamfreitas/dissertation:${1}"
N_PARTITIONS=$2
PORT=$3
PARTITION_IDS=$4

docker run --name $NODE -p $PORT:$PORT --env NODE=$NODE --env CLOCK_REGION=$CLOCK_REGION --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $PORT $PARTITION_IDS
