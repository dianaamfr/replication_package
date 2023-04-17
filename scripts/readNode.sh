if [ $# -lt 3 ]
then
  echo "Usage: readNode.sh <numberOfPartitions> <port> <regionPartitionsIds>"
  exit 1
fi

NODE=readNode
BUCKET_SUFFIX=-reference-architecture
IMAGE=dianaamfreitas/dissertation:v5.0.0
N_PARTITIONS=$1
PORT=$2
PARTITION_IDS=$3
ROT_LOGS=false
WRITE_LOGS=true

docker run --name $NODE -p $PORT:$PORT --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX --env ROT_LOGS=$ROT_LOGS --env WRITE_LOGS=$WRITE_LOGS $IMAGE $PORT $PARTITION_IDS
