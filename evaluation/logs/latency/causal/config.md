# Latency Tests - CC prototype

## Test 1
- Write Delay = 50ms
- 110 pushes (11000 writes)
- Push/Pull Rate = 5ms
- 1 key per read

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 11000 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 11000 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-latency 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Test 2
- Write Delay = 100ms
- 110 pushes (5500 writes)
- Push/Pull Rate = 5ms
- 1 key per read

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 100 5500 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 5500 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-latency 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Test 3
- Write Delay = 500ms
- 110 pushes (1100 writes)
- Push/Pull Rate = 5ms
- 1 key per read

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 100 1100 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-latency 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 1100 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-latency 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
# Get Logs
### Reader EU-WEST-1
docker container cp busyReadGenerator:/logs/ .

## Copy logs
scp -i "reference-architecture.pem" -r ubuntu@<read-eu-DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
