# RUN:
**On every machine**
docker rm -f $(docker ps -a -q)
docker rmi $(docker images -a -q)

# MEASURE VISIBILITY

## Reader EU-WEST-1
**Read Node**: ./readNode.sh v9.0.0-visibility 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v9.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 200 100 a
**Busy Read Generator**: ./busyReadGenerator.sh v9.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 100 a

# Reader US-EAST-1
**Read Node**: ./readNode.sh v9.0.0-visibility 1 8080 1
**Busy Read Generator**: ./busyReadGenerator.sh v9.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 100 a

## Writer EU-WEST-1
**Write Node**: ./writeNode.sh v9.0.0-visibility 1 8080 1

---
# MEASURE LATENCY

## Reader EU-WEST-1
**Read Node**: ./readNode.sh v9.0.0-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v9.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 200 500 a
**Busy Read Generator**: ./busyReadGenerator.sh v9.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 500 a

# Reader US-EAST-1
**Read Node**: ./readNode.sh v9.0.0-latency 1 8080 1
**Busy Read Generator**: ./busyReadGenerator.sh v9.0.0-latency 1 1 8080 <read-us-ip> 8080 <write-ip> 1 500 a

## Writer EU-WEST-1
**Write Node**: ./writeNode.sh v9.0.0-latency 1 8080 1

---
# MEASURE GOODPUT

## Reader EU-WEST-1
**Read Node**: ./readNode.sh v9.0.0-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh v9.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 a
**Constant Read Generator**: ./constantReadGenerator.sh v9.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 500 100 a

# Reader US-EAST-1
**Read Node**: ./readNode.sh v9.0.0-goodput 1 8080 1
**Constant Read Generator**: ./constantReadGenerator.sh v9.0.0-goodput 1 1 8080 <read-us-ip> 8080 <write-ip> 1 500 100 a

## Writer EU-WEST-1
**Write Node**: ./writeNode.sh v9.0.0-goodput 1 8080 1

---
# GET LOGS:
## Reader EU-WEST-1
**Read Node**
docker container cp readNode:/logs/readnode-eu-west-1.json .
docker container cp readNode:/logs/readnode-eu-west-1-s3.json .

scp -i "reference-architecture.pem" -r ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/readnode-eu-west-1.json ./logs-ref-arch
scp -i "reference-architecture.pem" -r ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/readnode-eu-west-1-s3.json ./logs-ref-arch

**Read Generator**
docker container cp busyReadGenerator:/logs/readclient-eu-west-1.json .
scp -i "reference-architecture.pem" -r ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/readclient-eu-west-1.json ./logs-ref-arch

**Write Generator**
docker container cp constantWriteGenerator:/logs/writeclient-eu-west-1.json .
scp -i "reference-architecture.pem" -r ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/writeclient-eu-west-1.json ./logs-ref-arch

## Write Nodes EU-WEST-1
**Write Node 1**
docker container cp writeNode:/logs/writenode-1.json .
docker container cp writeNode:/logs/writenode-1-s3.json .

scp -i "reference-architecture.pem" -r ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/writenode-1.json ./logs-ref-arch
scp -i "reference-architecture.pem" -r ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/writenode-1-s3.json ./logs-ref-arch


## Reader US-EAST-1
**Read Node**
docker container cp readNode:/logs/readnode-us-east-1.json .
docker container cp readNode:/logs/readnode-us-east-1-s3.json .

scp -i "reference-architecture-us.pem" -r ubuntu@<DNS>.compute-1.amazonaws.com:~/readnode-us-east-1.json ./logs-ref-arch
scp -i "reference-architecture-us.pem" -r ubuntu@<DNS>.compute-1.amazonaws.com:~/readnode-us-east-1-s3.json ./logs-ref-arch

**Read Generator**
docker container cp busyReadGenerator:/logs/readclient-us-east-1.json .
scp -i "reference-architecture-us.pem" -r ubuntu@<DNS>.compute-1.amazonaws.com:~/readclient-us-east-1.json ./logs-ref-arch
