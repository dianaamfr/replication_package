if [ $# -lt 3 ] || [ $# -gt 3 ]
then
  echo "Usage: writeNode.sh <numberOfPartitions> <port> <partitionId>"
  exit 1
fi

NODE=writeNode
BUCKET_SUFFIX=-reference-architecture
IMAGE=dianaamfreitas/dissertation:v7.0.0-latency-validation
N_PARTITIONS=$1
PORT=$2
PARTITION_ID=$3
LATENCY_LOGS=true
GOODPUT_LOGS=true


docker run --name $NODE -p $PORT:$PORT --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX --env LATENCY_LOGS=$LATENCY_LOGS --env GOODPUT_LOGS=$GOODPUT_LOGS $IMAGE $PARTITION_ID $PORT
