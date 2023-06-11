# Evaluation Tests
## Single Client
Used to compare the eventually consistent baseline with the causally consistent prototype and the behavior of the causally consistent prototype with a single client and partition.

**Environment**:
- Push/Pull Rate of 5ms
- t4g.small/ubuntu/arm instances (1 for each compute node and 1 for each client/load generator)
- Local region eu-west-1, Remote region us-east-1
- 1 partition
- Single key ("a") per read

### Goodput & Write Response Time
A single client issues write requests in closed loop. A single client issues read requests at constant rate.

**Configuration**:
- Average of 3 iterations
- Read clients read for 20s one key at a time
- 50/100/200 ms inter-read delay

#### Causal
**Read Node**: ./readNode.sh v14.0.0-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh v14.0.0-goodput 1 1 8080 <readIp> 8080 <writeIp> 1 a
**Constant Read Generator**: ./constantReadGenerator.sh v14.0.0-goodput 1 1 8080 <readIp> 8080 <writeIp> 1 <delay> 20000 1 a
**Write Node**: ./writeNode.sh v14.0.0-goodput 1 8080 1 8080 <readIp>

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
scp -i "reference-architecture.pem" -r ubuntu@<readDns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

#### Causal
**Read Node**: ./readNode.sh v14.0.0-latency 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v14.0.0-latency 1 1 8080 <readIp> 8080 <writeIp> 1 <delay> <writes> a
**Busy Read Generator**: ./busyReadGenerator.sh v14.0.0-latency 1 1 8080 <readIp> 8080 <writeIp> 1 <writes> 1 a
**Write Node**: ./writeNode.sh v14.0.0-latency 1 8080 1 8080 <readIp>

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
docker container cp readNode:/logs/ .
docker container cp writeNode:/logs/ .

scp -i "reference-architecture.pem" -r ubuntu@<readEuDns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture.pem" -r ubuntu@<readClientEuDns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture.pem" -r ubuntu@<writeDns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture.pem" -r ubuntu@<writeClientDns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

scp -i "reference-architecture-us.pem" -r ubuntu@<readUsDns>.compute-1.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture-us.pem" -r ubuntu@<readUsClientDns>.compute-1.amazonaws.com:~/logs ./logs-ref-arch

#### Causal
##### EU-WEST-1 (Local)
**Read Node**: ./readNode.sh v14.0.0-visibility 1 8080 1
**Constant Write Generator**: ./constantWriteGenerator.sh v14.0.0-visibility 1 1 8080 <readEuIp> 8080 <writeIp> 1 <delay> 110 a
**Busy Read Generator**: ./busyReadGenerator.sh v14.0.0-visibility 1 1 8080 <readEuIp> 8080 <writeIp> 1 110 1 a
**Write Node**: ./writeNode.sh v14.0.0-visibility 1 8080 1 8080 <readEuIp> 8080 <readUsIp>

##### US-EAST-1 (Remote)
**Read Node**: ./readNode.sh v14.0.0-visibility 1 8080 1
**Busy Read Generator**: ./busyReadGenerator.sh v14.0.0-visibility 1 1 8080 <readUsIp> 8080 <writeIp> 1 110 1 a

#### Eventual
##### EU-WEST-1
**Constant Write Generator**: ./evConstantWriteGenerator.sh v3.0.0 1 <delay> 110 a
**Busy Read Generator**: ./evBusyReadGenerator.sh v3.0.0 1 110 a

##### US-EAST-1
**Busy Read Generator**: ./evBusyReadGenerator.sh v3.0.0 1 110 a

---
## Multiple Clients
Used to study the scalability, performance and staleness of the causally consistent prototype with multiple clients, partitions and read nodes.

**Environment**:
- Push/Pull Rate of 5ms
- t4g.small/ubuntu/arm instances (1 for each compute node)
- c6g.8xlarge/ubuntu/arm instances (for the client processes)
- Local region eu-west-1, Remote region us-east-1
- 8 keys

### Goodput & Write Response Time
One client issues write requests in closed loop. Multiple clients (15) issue read requests at constant rate.

**Configuration**:
- 50ms inter-write delay
- 3 iterations
- 1,2 and 4 partitions
- 1 key per read
- Read for 30s
- 1 write client and 15 read clients

**Get logs**:
docker container cp readNode:/logs/ .
docker container cp multiConstantReadGenerator:/logs/ .
scp -i "reference-architecture.pem" -r ubuntu@<readDns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture.pem" -r ubuntu@<readClientDns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

#### 1 Partition
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <readIp>
**Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <readIp> 8080 <writeIp1> 1 50 30000 1 8 15
**Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <readIp> 8080 <writeIp1> 1 8 1

#### 2 partitions
**Read Node**: ./readNode.sh v13.0.0-goodput 2 8080 1 2
**Write Nodes**: 
    ./writeNode.sh v13.0.0-goodput 2 8080 1 8080 <readIp>
    ./writeNode.sh v13.0.0-goodput 2 8080 2 8080 <readIp>
**Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 2 2 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 50 30000 1 4 15
**Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 2 2 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 4 1

#### 4 partitions
**Read Node**: ./readNode.sh v13.0.0-goodput 4 8080 1
**Write Nodes**: 
    ./writeNode.sh v13.0.0-goodput 4 8080 1 8080 <readIp>
    ./writeNode.sh v13.0.0-goodput 4 8080 2 8080 <readIp>
    ./writeNode.sh v13.0.0-goodput 4 8080 3 8080 <readIp>
    ./writeNode.sh v13.0.0-goodput 4 8080 4 8080 <readIp>
**Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 4 4 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 8080 <writeIp3> 3 8080 <writeIp4> 4 50 30000 1 2 15
**Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 4 4 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 8080 <writeIp3> 3 8080 <writeIp4> 4 2 1

### Read Latency + Visibility
Multiple clients issue read requests in closed loop. Multiple clients issue write requests at constant rate.

**Get Logs**:
docker container cp readNode:/logs/ . (EU and US)
docker container cp writeNode:/logs/ .
docker container cp multiBusyReadGenerator:/logs/ . (EU and US)
docker container cp multiConstantWriteGenerator:/logs/ .
scp -i "reference-architecture.pem" -r ubuntu@<readEuIp>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture.pem" -r ubuntu@<writeIp>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture.pem" -r ubuntu@<writeClientEuIp>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture.pem" -r ubuntu@<readClientEuIp>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture-us.pem" -r ubuntu@<readUsIp>.compute-1.amazonaws.com:~/logs ./logs-ref-arch
scp -i "reference-architecture-us.pem" -r ubuntu@<readClientUsIp>.compute-1.amazonaws.com:~/logs ./logs-ref-arch

#### Change number of readers and writers

**Configuration**:
- 50ms inter-write delay 
- Readers-Writers tests: R1-W2, R5-W10, R10-W20, R16-R18, R19-R38
- 1000 writes per client
- Read for 60s in EU (Local Region) and 90s in US (Remote Region)
- 2 keys per read

##### EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <readEuIp> 8080 <readUsIp>
**Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <readEuIp> 8080 <writeIp1> 1 60000 2 8 <R>
**Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <readEuIp> 8080 <writeIp1> 1 50 1000 8 <W>

##### US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <readEuIp> 8080 <readUsIp>
**Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <readUsIp> 8080 <writeIp1> 1 90000 2 8 <R>

#### Change number of partitions

**Configuration**:
- 50ms inter-write delay 
- 5 readers and 10 writers
- 1000 writes per client
- Read for 60s in EU (Local Region)
- 2 keys per read
- 1, 2 and 4 partitions (for 1 the results from the last test was used)

##### 2 partitions
**Read Nodes**: ./readNode.sh v13.0.0-visibility 2 8080 1 2
**Write Nodes**: 
    **eu**./writeNode.sh v13.0.0-visibility 2 8080 1 8080 <readEuIp> 8080 <readUsIp>
    **us**: ./writeNode.sh v13.0.0-visibility 2 8080 2 8080 <readEuIp> 8080 <readUsIp>
**Read Client EU**: ./multiBusyReadGenerator.sh v13.0.0-visibility 2 2 8080 <readEuIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 60000 2 4 5
**Write Client EU**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 2 2 8080 <readEuIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 50 1000 4 10
**Read Client US**: ./multiBusyReadGenerator.sh v13.0.0-visibility 2 2 8080 54.234.187.202 8080 <writeIp1> 1 8080 <writeIp2> 2 60000 2 4 5

##### 4 partitions
**Read Nodes**: ./readNode.sh v13.0.0-visibility 4 8080 1 2 3 4
**Write Nodes**:
    **eu**: ./writeNode.sh v13.0.0-visibility 4 8080 1 8080 <readEuIp> 8080 <readUsIp>
    **us**: ./writeNode.sh v13.0.0-visibility 4 8080 2 8080 <readEuIp> 8080 <readUsIp>
    **eu**: ./writeNode.sh v13.0.0-visibility 4 8080 3 8080 <readEuIp> 8080 <readUsIp>
    **us**: ./writeNode.sh v13.0.0-visibility 4 8080 4 8080 <readEuIp> 8080 <readUsIp>

**Read Client EU**: ./multiBusyReadGenerator.sh v13.0.0-visibility 4 4 8080 <readEuIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 8080 <writeIp3> 3 8080 <writeIp4> 4 60000 2 2 5
**Write Client EU**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 4 4 8080 <readEuIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 8080 <writeIp3> 3 8080 <writeIp4> 4 50 1000 2 10
**Read Client US**: ./multiBusyReadGenerator.sh v13.0.0-visibility 4 4 8080 <readUsIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 8080 <writeIp3> 3 8080 <writeIp4> 4 60000 2 2 5

### Read Throughput
Multiple clients issue read requests in closed loop. Multiple clients issue write requests at constant rate.

**Configuration**
- 50ms inter-write delay
- 10 fixed writers
- 500 writes per writer
- Variable number of read nodes (1, 2, 3, 4)
- For each number of nodes, variable number of readers (1, 5, 10, 15, 20, 25)
- 2 keys read at a time during 30 seconds
- Read for 30s

**Get logs**:
docker container cp multiBusyReadGenerator:/logs/ .
scp -i "reference-architecture.pem" -r ubuntu@<read-client-dns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

#### Change number of read nodes and readers
**Read Node**: ./readNode.sh v14.0.0-latency 1 8080 1
**Write Node**:
    **1**: ./writeNode.sh v14.0.0-latency 1 8080 1 8080 <readIp1>
    **2**: ./writeNode.sh v14.0.0-latency 1 8080 1 8080 <readIp1> 8080 <readIp2>
    **3**: ./writeNode.sh v14.0.0-latency 1 8080 1 8080 <readIp1> 8080 <readIp2> 8080 <readIp3>
    **4**: ./writeNode.sh v14.0.0-latency 1 8080 1 8080 <readIp1> 8080 <readIp2> 8080 <readIp3> 8080 <readIp4>
**Read Client**: 
    **1**: ./multiBusyReadGenerator.sh v14.0.0-latency 1 1 1 8080 <readIp1> 8080 <writeIp> 1 30000 2 8 1
    **2**: ./multiBusyReadGenerator.sh v14.0.0-latency 1 2 1 8080 <readIp1> 8080 <readIp2> 8080 <writeIp> 1 30000 2 8 <R>
    **3**: ./multiBusyReadGenerator.sh v14.0.0-latency 1 3 1 8080 <readIp1> 8080 <readIp2> 8080 <readIp3> 8080 <writeIp> 1 30000 2 8 <R>
    **4**: ./multiBusyReadGenerator.sh v14.0.0-latency 1 4 1 8080 <readIp1> 8080 <readIp2> 8080 <readIp3> 8080 <readIp4> 8080 <writeIp> 1 30000 2 8 <R>
<!-- Read ip does not matter for write client -->
**Write Client**: ./multiConstantWriteGenerator.sh v14.0.0-latency 1 1 8080 <readEuIp1> 8080 <writeIp> 1 50 500 8 10

### Write Throughput
Multiple clients issue write requests in closed loop to a set of keys.

**Configuration**
- 50ms inter-write delay
- Variable number of writes nodes / partitions (1, 2, 4)
- For each number of nodes, variable number of writers (1, 5, 10, 15, 20, 25)
- Writers issue writes during 30s 

#### 1 partition
**Write Node**: ./writeNode.sh v15.0.0-write-throughput 1 8080 1 8080 <readIp>
**Write Client**: ./multiBusyWriteGenerator.sh v14.0.0-latency 1 1 8080 <readIp> 8080 <writeIp1> 1 8 <W> 30000

#### 2 partitions
**Write Nodes**: 
    ./writeNode.sh v15.0.0-write-throughput 2 8080 1 8080 <readIp>
    ./writeNode.sh v15.0.0-write-throughput 2 8080 2 8080 <readIp>
**Write Client**: ./multiBusyWriteGenerator.sh v15.0.0-write-throughput 2 2 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 4 <W> 30000

#### 4 partitions
**Write Nodes**: 
    ./writeNode.sh v15.0.0-write-throughput 4 8080 1 8080 <readIp>
    ./writeNode.sh v15.0.0-write-throughput 4 8080 2 8080 <readIp>
    ./writeNode.sh v15.0.0-write-throughput 4 8080 3 8080 <readIp>
    ./writeNode.sh v15.0.0-write-throughput 4 8080 4 8080 <readIp>
**Write Client**: ./multiBusyWriteGenerator.sh v15.0.0-write-throughput 4 4 8080 <readIp> 8080 <writeIp1> 1 8080 <writeIp2> 2 8080 <writeIp3> 3 8080 <writeIp4> 4 2 <W> 30000
