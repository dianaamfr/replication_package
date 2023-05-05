if [ $# -le 5 ]
then
  echo "Usage: evConstantReadGenerator.sh <imageTag> <totalPartitions> <readDelay> <totalReads> <key>+"
  exit 1
fi

NODE=constantReadGenerator
BUCKET_SUFFIX=-reference-architecture
IMAGE="dianaamfreitas/dissertationEventualBaseline:${1}"
N_PARTITIONS=$2
REST="${@:3}"

docker run --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE $REST
