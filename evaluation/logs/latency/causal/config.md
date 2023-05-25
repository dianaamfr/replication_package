# Latency Tests - CC prototype
- Push/Pull Rate = 5ms
- 110 pushes
- 1 key per read
- t4g.small / ubuntu / arm

## Test 1
- Write Delay = 50ms
- 11000 writes

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 11000 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 11000 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-latency 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Test 2
- Write Delay = 100ms
- 5500 writes

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 100 5500 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 5500 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-latency 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Test 3
- Write Delay = 200ms
- 1100 writes

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 200 1100 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 1100 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-latency 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
# Get Logs
### Reader EU-WEST-1
docker container cp busyReadGenerator:/logs/ .

## Copy logs
scp -i "reference-architecture.pem" -r ubuntu@<read-eu-DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
