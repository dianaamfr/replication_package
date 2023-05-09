# Visibility + Latency Tests - Eventually Consistent S3

## Test 1
- Write Delay = 50ms
- 110 writes
- 1 key per read

### EU-WEST-1
**Constant Write Generator**: ./evConstantWriteGenerator.sh v2.0.0 1 50 110 a
**Busy Read Generator**: ./evBusyReadGenerator.sh v2.0.0 1 110 a

### US-EAST-1
**Busy Read Generator**: ./evBusyReadGenerator.sh v2.0.0 1 110 a

---
## Test 2
- Write Delay = 100ms
- 110 writes
- 1 key per read

### EU-WEST-1
**Constant Write Generator**: ./evConstantWriteGenerator.sh v2.0.0 1 100 110 a
**Busy Read Generator**: ./evBusyReadGenerator.sh v2.0.0 1 110 a

### US-EAST-1
**Busy Read Generator**: ./evBusyReadGenerator.sh v2.0.0 1 110 a

---
## Test 3
- Write Delay = 500ms
- 110 writes
- 1 key per read

### EU-WEST-1
**Constant Write Generator**: ./evConstantWriteGenerator.sh v2.0.0 1 500 110 a
**Busy Read Generator**: ./evBusyReadGenerator.sh v2.0.0 1 110 a

### US-EAST-1
**Busy Read Generator**: ./evBusyReadGenerator.sh v2.0.0 1 110 a

---
# Get Logs

### EU-WEST-1
docker container cp busyReadGenerator:/logs/ ./logs
docker container cp constantWriteGenerator:/logs/ ./logs

### US-EAST-1
docker container cp busyReadGenerator:/logs/ .

# Copy logs
scp -i "reference-architecture.pem" -r ubuntu@ec2-3-253-110-206.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture-us.pem" -r ubuntu@ec2-3-90-26-1.compute-1.amazonaws.com:~/logs ./logs-ref-arch
