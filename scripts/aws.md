# RUN:
**On every machine**
docker rm -f $(docker ps -a -q)

## Reader EU-WEST-1
**Read Node**: ./readNode.sh 1 8080 1
**Constant Write Generator**
<!-- <delay> <totalWrites> <keys> -->
./constantWriteGenerator.sh 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 200 100 a b
**Busy Read Generator**
<!-- <totalWrites> <keys> -->
./busyReadGenerator.sh 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 100 a b

# Reader US-EAST-1
**Read Node**: ./readNode.sh 1 8080 1
**Busy Read Generator**
<!-- <totalWrites> <keys> -->
./busyReadGenerator.sh 1 1 8080 <read-us-ip> 8080 <write-ip> 1 100 a b

## Writer EU-WEST-1
**Write Node**: ./writeNode.sh 1 8080 1

---
# GET LOGS:
## Reader EU-WEST-1
**Read Node**
docker container cp readNode:/logs/readnode-eu-west-1.json .
docker container cp readNode:/logs/readnode-eu-west-1-s3.json .

scp -i "reference-architecture-ubuntu.pem" -r <DNS>.eu-west-1.compute.amazonaws.com:~/readnode-eu-west-1.json ./logs-ref-arch
scp -i "reference-architecture-ubuntu.pem" -r <DNS>.eu-west-1.compute.amazonaws.com:~/readnode-eu-west-1-s3.json ./logs-ref-arch

**Read Generator**
docker container cp busyReadGenerator:/logs/readclient-eu-west-1.json
scp -i "reference-architecture-ubuntu.pem" -r <DNS>.eu-west-1.compute.amazonaws.com:~/readclient-eu-west-1.json ./logs-ref-arch

**Write Generator**
docker container cp readNode:/logs/writeclient-eu-west-1.json .
scp -i "reference-architecture-ubuntu.pem" -r <DNS>.eu-west-1.compute.amazonaws.com:~/writeclient-eu-west-1.json ./logs-ref-arch

## Write Nodes EU-WEST-1
**Write Node 1**
docker container cp readNode:/logs/writenode-1.json .
docker container cp readNode:/logs/writenode-1-s3.json .

scp -i "reference-architecture-ubuntu.pem" -r <DNS>.eu-west-1.compute.amazonaws.com:~/writenode-1.json ./logs-ref-arch
scp -i "reference-architecture-ubuntu.pem" -r <DNS>.eu-west-1.compute.amazonaws.com:~/writenode-1-s3.json ./logs-ref-arch


## Reader US-EAST-1
**Read Node**
docker container cp readNode:/logs/readnode-us-east-1.json .
docker container cp readNode:/logs/readnode-us-east-1-s3.json .

scp -i "reference-architecture-ubuntu-us.pem" -r <DNS>.compute-1.amazonaws.com:~/readnode-us-east-1.json ./logs-ref-arch
scp -i "reference-architecture-ubuntu-us.pem" -r <DNS>.compute-1.amazonaws.com:~/readnode-us-east-1-s3.json ./logs-ref-arch

**Read Generator**
docker container cp busyReadGenerator:/logs/readclient-us-east-1.json
scp -i "reference-architecture-ubuntu-us.pem" -r <DNS>.compute-1.amazonaws.com:~/readclient-us-east-1.json ./logs-ref-arch
