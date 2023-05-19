# Visibility Tests - CC prototype

- Write Delay = 50ms
- Push/Pull Rate = 5ms
- 500 writes per client
- Read for 120s
- 10 keys per partition
- 1 key per read

## Test 1 - 100 clients (95 R, 5 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 500 10 5
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 120000 1 10 95

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 184.73.140.115 8080 3.252.53.121 1 120000 1 10 95

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 54.75.176.69 8080 184.73.140.115  

## Test 2 - 200 clients (190 R, 10 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 500 10 10
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 120000 1 10 190

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 184.73.140.115 8080 3.252.53.121 1 120000 1 10 190

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 54.75.176.69 8080 184.73.140.115  

## Test 3 - 300 clients (285 R, 15 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 500 10 15
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 120000 1 10 285

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 184.73.140.115 8080 3.252.53.121 1 120000 1 10 285

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 54.75.176.69 8080 184.73.140.115  

## Test 4 - 400 clients (380 R, 20 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 500 10 20
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 120000 1 10 380

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 184.73.140.115 8080 3.252.53.121 1 120000 1 10 380

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 54.75.176.69 8080 184.73.140.115  



## Test 5 -  500 clients (475 R, 25 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 50 500 10 25
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 54.75.176.69 8080 3.252.53.121 1 120000 1 10 475

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 184.73.140.115 8080 3.252.53.121 1 120000 1 10 475

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 54.75.176.69 8080 184.73.140.115  

---
# Get Logs
### Reader EU-WEST-1
docker container cp readNode:/logs/ .
docker container cp multiBusyReadGenerator:/logs/ .
docker container cp multiConstantWriteGenerator:/logs/ .

### Write Nodes EU-WEST-1
docker container cp writeNode:/logs/ .

### Reader US-EAST-1
docker container cp readNode:/logs/ .
docker container cp multiBusyReadGenerator:/logs/ .

## Copy logs

scp -i "reference-architecture.pem" -r ubuntu@ec2-54-75-176-69.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

scp -i "reference-architecture.pem" -r ubuntu@ec2-3-252-53-121.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

scp -i "reference-architecture-us.pem" -r ubuntu@ec2-184-73-140-115.compute-1.amazonaws.com:~/logs ./logs-ref-arch