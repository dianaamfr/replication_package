if [ $# -ne 2 ]
then
  echo "Usage: evCli.sh <imageTag> <totalPartitions>"
  exit 1
fi

NODE=clientInterface
BUCKET_SUFFIX=-reference-architecture
IMAGE="dianaamfreitas/dissertation-eventual:${1}"
N_PARTITIONS=$2

docker run -i --name $NODE --env NODE=$NODE --env PARTITIONS=$N_PARTITIONS --env BUCKET_SUFFIX=$BUCKET_SUFFIX $IMAGE
