if [ $# -lt 7 ]
then
  echo "Usage: readGenerator.sh <numberOfPartitions> <numberOfRegionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>+) <delay>* <totalReads>*"
  exit 1
fi

NODE=readGenerator
BUCKET_SUFFIX=-reference-architecture
IMAGE=dianaamfreitas/dissertation:v5.0.0
N_PARTITIONS=$1
REGION_PARTITIONS=$2
READ_ADDRESS="${3} ${4}"
WRITE_ADDRESSES="${@:5}"
WRITE_LOGS=true
ROT_LOGS=true

docker run --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX --env ROT_LOGS=$ROT_LOGS --env WRITE_LOGS=$WRITE_LOGS $IMAGE $REGION_PARTITIONS $READ_ADDRESS $WRITE_ADDRESSES
