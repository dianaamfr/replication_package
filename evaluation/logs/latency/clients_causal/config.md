# Latency Tests - CC prototype

- Write Delay = 50ms
- Push/Pull Rate = 5ms
- 1000 writes per client
- Read for 60s
- 10 keys per partition
- 1 key per read
chmod +x *.sh
docker container cp multiBusyReadGenerator:/logs/ .

## Test 1 - 100 clients (95 R, 5 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-latency 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 1000 10 5
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 60000 1 10 95


### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-latency 1 8080 1 8080 54.75.176.69 8080 184.73.140.115 

## Test 2 - 200 clients (190 R, 10 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-latency 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 1000 10 10
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 60000 1 10 190


### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-latency 1 8080 1 8080 54.75.176.69 8080 184.73.140.115 


## Test 3 - 300 clients (285 R, 15 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-latency 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 1000 10 15
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 60000 1 10 285


### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-latency 1 8080 1 8080 54.75.176.69 8080 184.73.140.115 


## Test 4 - 400 clients (380 R, 20 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-latency 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 1000 10 20
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 60000 1 10 380


### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-latency 1 8080 1 8080 54.75.176.69 8080 184.73.140.115 


## Test 5 - 500 clients (475 R, 25 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-latency 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 1000 10 25
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 60000 1 10 475

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-latency 1 8080 1 8080 54.75.176.69 8080 184.73.140.115 


### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-latency 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 1000 10 105
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-latency 1 1 8080 54.75.176.69 8080 3.252.53.121 1 60000 1 10 1995

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-latency 1 8080 1 8080 54.75.176.69 8080 184.73.140.115 

---
# Get Logs
### Reader EU-WEST-1
docker container cp multiBusyReadGenerator:/logs/ .

## Copy logs
scp -i "reference-architecture.pem" -r ubuntu@ec2-54-75-176-69.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
