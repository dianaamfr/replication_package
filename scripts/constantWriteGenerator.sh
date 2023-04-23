if [ $# -lt 10 ]
then
  echo "Usage: constantWriteGenerator.sh <numberOfPartitions> <numberOfRegionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>+) <delay> <totalWrites> (<keys>)+"
  exit 1
fi

NODE=constantWriteGenerator
BUCKET_SUFFIX=-reference-architecture
IMAGE=dianaamfreitas/dissertation:v7.0.0-latency-validation
N_PARTITIONS=$1
REGION_PARTITIONS=$2
READ_ADDRESS="${3} ${4}"
REST="${@:5}"
LATENCY_LOGS=true

docker run --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX --env LATENCY_LOGS=$LATENCY_LOGS $IMAGE $REGION_PARTITIONS $READ_ADDRESS $REST

