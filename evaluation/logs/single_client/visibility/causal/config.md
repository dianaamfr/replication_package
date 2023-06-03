# Visibility Tests - CC prototype
- Push/Pull Rate = 5ms
- 110 writes
- 1 key per read
- t4g.small / ubuntu / arm

## Test 1
- Write Delay = 50ms

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-visibility 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 110 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 110 1 a

### Reader US-EAST-1
**Read Node**: ./readNode.sh v12.0.0-visibility 1 8080 1
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 110 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Test 2
- Write Delay = 100ms

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-visibility 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 100 110 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 110 1 a

### Reader US-EAST-1
**Read Node**: ./readNode.sh v12.0.0-visibility 1 8080 1
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 110 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Test 3
- Write Delay = 500ms

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-visibility 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 500 110 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 110 1 a

### Reader US-EAST-1
**Read Node**: ./readNode.sh v12.0.0-visibility 1 8080 1
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 110 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

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