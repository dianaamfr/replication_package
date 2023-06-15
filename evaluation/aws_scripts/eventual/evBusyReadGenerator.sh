if [ $# -lt 4  ]
then
  echo "Usage: evBusyReadGenerator.sh <imageTag> <totalPartitions> <expectedWrites> <key>+"
  exit 1
fi

NODE=busyReadGenerator
BUCKET_SUFFIX=-reference-architecture
IMAGE="dianaamfreitas/dissertation-eventual:${1}"
N_PARTITIONS=$2
REST="${@:3}"

docker run --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $REST
