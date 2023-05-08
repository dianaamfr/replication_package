# Docker clean
**On every machine**
docker rm -f $(docker ps -a -q)
docker rmi $(docker images -a -q)

# Causal
## Visibility

### Reader EU-WEST-1
**Read Node**: ./readNode.sh <image-tag>-visibility 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh <image-tag>-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 <delay> <totalWrites> <keys>
**Busy Read Generator**: ./busyReadGenerator.sh <image-tag>-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 <totalWrites> <keysPerRead> <keys>

### Reader US-EAST-1
**Read Node**: ./readNode.sh <image-tag>-visibility 1 8080 1
**Busy Read Generator**: ./busyReadGenerator.sh <image-tag>-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 <totalWrites> <keysPerRead> <keys>

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh <image-tag>-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Latency

### Reader EU-WEST-1
**Read Node**: ./readNode.sh <image-tag>-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh <image-tag>-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 <delay> <totalWrites> <keys>
**Busy Read Generator**: ./busyReadGenerator.sh <image-tag>-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 <totalWrites> <keysPerRead> <keys>

### Reader US-EAST-1
**Read Node**: ./readNode.sh <image-tag>-latency 1 8080 1
**Busy Read Generator**: ./busyReadGenerator.sh <image-tag>-latency 1 1 8080 <read-us-ip> 8080 <write-ip> 1 <totalWrites> <keysPerRead> <keys>

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh <image-tag>-latency 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Goodput

### Reader EU-WEST-1
**Read Node**: ./readNode.sh <image-tag>-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh <image-tag>-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 <keys>
**Constant Read Generator**: ./constantReadGenerator.sh <image-tag>-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 <delay> <totalWrites> <keysPerRead> <keys>

### Reader US-EAST-1
**Read Node**: ./readNode.sh <image-tag>-goodput 1 8080 1
**Constant Read Generator**: ./constantReadGenerator.sh <image-tag>-goodput 1 1 8080 <read-us-ip> 8080 <write-ip> 1 <delay> <totalWrites> <keysPerRead> <keys>

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh <image-tag>-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Get Logs
### Reader EU-WEST-1
**Read Node**
docker container cp readNode:/logs/ ./logs
scp -r "reference-architecture.pem" ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

**Read Generator**
docker container cp busyReadGenerator:/logs/ ./logs
scp -r "reference-architecture.pem" ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

**Constant Write Generator**
docker container cp constantWriteGenerator:/logs/ ./logs
scp -r "reference-architecture.pem" ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

### Write Nodes EU-WEST-1
**Write Node 1**
docker container cp writeNode:/logs/ ./logs
scp -r "reference-architecture.pem" ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

### Reader US-EAST-1
**Read Node**
docker container cp readNode:/logs/ ./logs
scp -r "reference-architecture-us.pem" ubuntu@<DNS>.compute-1.amazonaws.com:~/logs ./logs-ref-arch

**Busy Read Generator**
docker container cp busyReadGenerator:/logs/ .
scp -i "reference-architecture-us.pem" ubuntu@<DNS>.compute-1.amazonaws.com:~/logs ./logs-ref-arch

# Eventual

## Visibility + Latency

### EU-WEST-1
**Constant Write Generator**: ./evConstantWriteGenerator.sh v1.0.0 1 <delay> <totalWrites> <keys>
**Busy Read Generator**: ./evBusyReadGenerator.sh v1.0.0 1 <totalWrites> <keys>

### US-EAST-1
**Busy Read Generator**: ./evBusyReadGenerator.sh v1.0.0 1 <totalWrites> <keys>

---
## Goodput

### EU-WEST-1
**Busy Write Generator**: ./evBusyWriteGenerator.sh v1.0.0 1 <keys>
**Constant Read Generator**: ./evConstantReadGenerator.sh v1.0.0 1 <delay> <totalWrites> <keys>

### US-EAST-1
**Constant Read Generator**: ./evConstantReadGenerator.sh v1.0.0 1 <delay> <totalWrites> <keys>
