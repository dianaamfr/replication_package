if [ $# -lt 9 ]
then
  echo "Usage: busyReadGenerator.sh <numberOfPartitions> <numberOfRegionPartitions> <readPort> <readIP> (<writePort> <writeIP> <partitionId>+) <expectedWrites> (<key>)+"
  exit 1
fi

NODE=busyReadGenerator
BUCKET_SUFFIX=-reference-architecture
IMAGE=dianaamfreitas/dissertation:v7.0.0-latency-validation
N_PARTITIONS=$1
REGION_PARTITIONS=$2
READ_ADDRESS="${3} ${4}"
REST="${@:5}"
LOGS=true

docker run --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX --env LOGS=$LOGS $IMAGE $REGION_PARTITIONS $READ_ADDRESS $REST
