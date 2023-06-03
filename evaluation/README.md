# Evaluation Tests

## Single Client
Used to compare the eventually consistent baseline with the causally consistent prototype and the behavior of the causally consistent prototype with a single client and partition.

**Environment**:
- Push/Pull Rate of 5ms
- t4g.small/ubuntu/arm instances (1 for each compute node)
- Local region eu-west-1, Remote region us-east-1
- 1 partition
- Client processes run on the same instance as the read nodes of their local region
- Single key ("a") per read

### Goodput & Write Response Time
A single client issues write requests in closed loop. A single client issues read requests at constant rate.

**Configuration**:
- Average of 3 iterations
- Read clients read for 20s one key at a time
- 50/100/200 ms inter-read delay

#### Causal
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh v12.0.0-goodput 1 1 8080 <readIp> 8080 <writeIp> 1 a
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <readIp> 8080 <writeIp> 1 <delay> 20000 1 a
**Write Node**: ./writeNode.sh v12.0.0-goodput 1 8080 1 8080 <readIp>

#### Eventual
**Busy Write Generator**: ./evBusyWriteGenerator.sh v3.0.0 1 a
**Constant Read Generator**: ./evConstantReadGenerator.sh v3.0.0 1 <delay> 20000 a

### Read Latency
A single client issues read requests in closed loop. A single client issues write requests at constant rate.

**Configuration**:
- 50/100/200 ms inter-write delay ()
- 110 pushes in the CC prototype (11000, 5500 and 1100 writes), 110 writes in the EC baseline

**Get Logs**:
docker container cp busyReadGenerator:/logs/ .
scp -i "reference-architecture.pem" -r ubuntu@<readEuDns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

#### Causal
**Read Node**: ./readNode.sh v12.0.0-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-latency 1 1 8080 <readIp> 8080 <writeIp> 1 <delay> <writes> a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-latency 1 1 8080 <readIp> 8080 <writeIp> 1 <writes> 1 a
**Write Node**: ./writeNode.sh v12.0.0-latency 1 8080 1 8080 <readIp>

#### Eventual
**Constant Write Generator**: ./evConstantWriteGenerator.sh v3.0.0 1 <delay> 110 a
**Busy Read Generator**: ./evBusyReadGenerator.sh v3.0.0 1 110 a

### Visibility
A single client issues read requests in closed loop. A single client issues write requests at constant rate.

**Configuration**:
- 110 writes
- 50/100/200 ms inter-write delay

**Get Logs**:
docker container cp busyReadGenerator:/logs/ .
docker container cp constantWriteGenerator:/logs/ .
scp -i "reference-architecture.pem" -r ubuntu@<readEuDns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture-us.pem" -r ubuntu@<readUsDns>.compute-1.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture-us.pem" -r ubuntu@<writeEuDNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

#### Causal
##### EU-WEST-1 (Local)
**Read Node**: ./readNode.sh v12.0.0-visibility 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v12.0.0-visibility 1 1 8080 <readEuIp> 8080 <writeIp> 1 <delay> 110 a
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-visibility 1 1 8080 <readEuIp> 8080 <writeIp> 1 110 1 a
**Write Node**: ./writeNode.sh v12.0.0-visibility 1 8080 1 8080 <readEuIp> 8080 <readUsIp>

##### US-EAST-1 (Remote)
**Read Node**: ./readNode.sh v12.0.0-visibility 1 8080 1
**Busy Read Generator**: ./busyReadGenerator.sh v12.0.0-visibility 1 1 8080 <readUsIp> 8080 <writeIp> 1 110 1 a

#### Eventual
##### EU-WEST-1
**Constant Write Generator**: ./evConstantWriteGenerator.sh v3.0.0 1 <delay> 110 a
**Busy Read Generator**: ./evBusyReadGenerator.sh v3.0.0 1 110 a

##### US-EAST-1
**Busy Read Generator**: ./evBusyReadGenerator.sh v3.0.0 1 110 a

---
## Multi Client
Used to study the scalability, performance and staleness of the causally consistent prototype with multiple clients, partitions and read nodes.

<!-- TODO: Add tests description and commands -->