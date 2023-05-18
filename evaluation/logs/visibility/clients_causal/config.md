# visibility Tests - CC prototype

- Write Delay = 50ms
- Push/Pull Rate = 5ms
- 110 writes per client
- Read for 60s
- 10 keys per partition
- 1 key per read

## Test 1 - 100 clients (95 R, 5 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 110 10 5
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 60000 1 10 95

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 60000 1 10 95

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 2 -  500 clients (475 R, 25 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 110 10 25
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 60000 1 10 475

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 60000 1 10 475

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 3 - 900 clients (855 R, 45 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 110 10 45
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 60000 1 10 855

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 60000 1 10 855

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 4 - 1300 clients (1235 R, 65 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 110 10 65
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 60000 1 10 1235

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 60000 1 10 1235

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 5 - 1700 clients (1615 R, 85 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 110 10 85
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 60000 1 10 1615

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 60000 1 10 1615

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 6 - 2100 (1995 R, 105 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 110 10 105
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 60000 1 10 1995

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 60000 1 10 1995

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

---
# Get Logs
### Reader EU-WEST-1
docker container cp readNode:/logs/ .
docker container cp busyReadGenerator:/logs/ .
docker container cp constantWriteGenerator:/logs/ .

### Write Nodes EU-WEST-1
docker container cp writeNode:/logs/ .

### Reader US-EAST-1
docker container cp readNode:/logs/ .
docker container cp busyReadGenerator:/logs/ .

## Copy logs

scp -i "reference-architecture.pem" -r ubuntu@<read-eu-DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

scp -i "reference-architecture.pem" -r ubuntu@<write-DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

scp -i "reference-architecture-us.pem" -r ubuntu@<read-us-DNS>.compute-1.amazonaws.com:~/logs ./logs-ref-arch