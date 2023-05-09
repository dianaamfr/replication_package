# Goodput Tests - Eventually Consistent S3

## Test 1
- Read Delay = 50ms
- Reads for 60s(1min)
- 1 key per read

### EU-WEST-1
**Busy Write Generator**: ./evBusyWriteGenerator.sh v2.0.0 1 a
**Constant Read Generator**: ./evConstantReadGenerator.sh v2.0.0 1 50 a

### US-EAST-1
**Constant Read Generator**: ./evConstantReadGenerator.sh v2.0.0 1 50 a

---
## Test 2
- Write Delay = 100ms
- Reads for 60s(1min)
- 1 key per read

### EU-WEST-1
**Busy Write Generator**: ./evBusyWriteGenerator.sh v2.0.0 1 a
**Constant Read Generator**: ./evConstantReadGenerator.sh v2.0.0 1 100 a

### US-EAST-1
**Constant Read Generator**: ./evConstantReadGenerator.sh v2.0.0 1 100 a

---
## Test 3
- Write Delay = 500ms
- Reads for 60s(1min)
- 1 key per read

### EU-WEST-1
**Busy Write Generator**: ./evBusyWriteGenerator.sh v2.0.0 1 a
**Constant Read Generator**: ./evConstantReadGenerator.sh v2.0.0 1 500 a

### US-EAST-1
**Constant Read Generator**: ./evConstantReadGenerator.sh v2.0.0 1 500 a

---
# Get Logs
### EU-WEST-1
**Busy Read Generator**
docker container cp busyReadGenerator:/logs/ ./logs
scp -r "reference-architecture.pem" ubuntu@<DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

### US-EAST-1
**Busy Read Generator**
docker container cp busyReadGenerator:/logs/ .
scp -r "reference-architecture-us.pem" ubuntu@<DNS>.compute-1.amazonaws.com:~/logs ./logs-ref-arch
