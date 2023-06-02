
(Delay = 50ms, Push/Pull Rate = 5ms, 1000 writes per client, Read for 60s in EU and 90s in US, 8 keys per partition, 2 keys per read)
- c6g.8xlarge clients
- t4.gsmall servers
- R:W: 1:2, 5:10, 10:20, 16:32 (16:3,29), 18:36 (18:7,29), 19:38 (19:9,29)

## Visibility + Latency Logs (1 partition)

### EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

**Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip1> 1 60000 2 8 19
**Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip1> 1 50 1000 8 9

**Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip1> 1 50 1000 8 29


### US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

**Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip1> 1 90000 2 8 19

---

## Visibility + Latency Logs (2, 4 partitions) 5R:10W
### 2 partitions
**Read Nodes**: ./readNode.sh v13.0.0-visibility 2 8080 1 2

**Write Node EU**: ./writeNode.sh v13.0.0-visibility 2 8080 1 8080 54.75.61.151 8080 54.234.187.202
**Write Node US**: ./writeNode.sh v13.0.0-visibility 2 8080 2 8080 54.75.61.151 8080 54.234.187.202

**Read Client EU**: ./multiBusyReadGenerator.sh v13.0.0-visibility 2 2 8080 54.75.61.151 8080 3.249.254.252 1 8080 100.26.42.29 2 60000 2 4 5
**Write Client EU**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 2 2 8080 54.75.61.151 8080 3.249.254.252 1 8080 100.26.42.29 2 50 1000 4 10

**Read Client US**: ./multiBusyReadGenerator.sh v13.0.0-visibility 2 2 8080 54.234.187.202 8080 3.249.254.252 1 8080 100.26.42.29 2 60000 2 4 5

### 4 partitions
**Read Nodes**: ./readNode.sh v13.0.0-visibility 4 8080 1 2 3 4

**Write Node1 eu**: ./writeNode.sh v13.0.0-visibility 4 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>
**Write Node2 us**: ./writeNode.sh v13.0.0-visibility 4 8080 2 8080 <read-eu-ip> 8080 <read-us-ip>
**Write Node3 eu**: ./writeNode.sh v13.0.0-visibility 4 8080 3 8080 <read-eu-ip> 8080 <read-us-ip>
**Write Node4 us**: ./writeNode.sh v13.0.0-visibility 4 8080 4 8080 <read-eu-ip> 8080 <read-us-ip>

**Read Client EU**: ./multiBusyReadGenerator.sh v13.0.0-visibility 4 4 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 8080 <write-node-3> 3 8080 <write-node-4> 4 60000 2 2 5
**Write Client EU**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 4 4 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 8080 <write-node-3> 3 8080 <write-node-4> 4 50 1000 2 10

**Read Client US**: ./multiBusyReadGenerator.sh v13.0.0-visibility 4 4 8080 <read-us-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 8080 <write-node-3> 3 8080 <write-node-4> 4 60000 2 2 5

---

## Goodput (2, 4 partitions) 20R:1W
### 1 partition
- 3 iterations each
- R 10, 15, 20
- 1 key per read

**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

**Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip1> 1 50 30000 1 8 15
**Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip1> 1 8 1

### 2 partitions
**Read Node**: ./readNode.sh v13.0.0-goodput 2 8080 1 2
**Write Node**: ./writeNode.sh v13.0.0-goodput 2 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>
**Write Node**: ./writeNode.sh v13.0.0-goodput 2 8080 2 8080 <read-eu-ip> 8080 <read-us-ip>

**Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 2 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 50 30000 1 4 15
**Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 2 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 4 1

### 4 partitions
**Read Node**: ./readNode.sh v13.0.0-goodput 4 8080 1
**Write Node**: 
./writeNode.sh v13.0.0-goodput 4 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>
./writeNode.sh v13.0.0-goodput 4 8080 2 8080 <read-eu-ip> 8080 <read-us-ip>

./writeNode.sh v13.0.0-goodput 4 8080 3 8080 <read-eu-ip> 8080 <read-us-ip>
./writeNode.sh v13.0.0-goodput 4 8080 4 8080 <read-eu-ip> 8080 <read-us-ip>

**Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 4 4 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 8080 <write-ip3> 3 8080 <write-ip4> 4 50 30000 1 2 15
**Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 4 4 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 8080 <write-ip3> 3 8080 <write-ip4> 4 2 1

---

## Scale Read Nodes - Latency
- 8 keys total, 1 partition
- 10 fixed writers making 500 writes at a constant rate with 50ms between requests
- R 1, 5, 10, 15, 20, 25
- Repeat for 1, 2, 3 and 4 read nodes
- 2 keys read at a time during 30 seconds

### EU-WEST-1
**Read Node**: ./readNode.sh v14.0.0-latency 1 8080 1
**Write Node**:
    **1**: ./writeNode.sh v14.0.0-latency 1 8080 1 8080 <read-eu-ip1>
    **2**: ./writeNode.sh v14.0.0-latency 1 8080 1 8080 <read-eu-ip1> 8080 <read-eu-ip2>
    **3**: ./writeNode.sh v14.0.0-latency 1 8080 1 8080 <read-eu-ip1> 8080 <read-eu-ip2> 8080 <read-eu-ip3>
    **4**: ./writeNode.sh v14.0.0-latency 1 8080 1 8080 <read-eu-ip1> 8080 <read-eu-ip2> 8080 <read-eu-ip3> 8080 <read-eu-ip4>

**Read Client**: 
    **1**: ./multiBusyReadGenerator.sh v14.0.0-latency 1 1 1 8080 <read-eu-ip1> 8080 <write-ip> 1 30000 2 8 1
    **2**: ./multiBusyReadGenerator.sh v14.0.0-latency 1 2 1 8080 <read-eu-ip1> 8080 <read-eu-ip2> 8080 <write-ip> 1 30000 2 8 1
    **3**: ./multiBusyReadGenerator.sh v14.0.0-latency 1 3 1 8080 <read-eu-ip1> 8080 <read-eu-ip2> 8080 <read-eu-ip3> 8080 <write-ip> 1 30000 2 8 1
    **4**: ./multiBusyReadGenerator.sh v14.0.0-latency 1 4 1 8080 <read-eu-ip1> 8080 <read-eu-ip2> 8080 <read-eu-ip3> 8080 <read-eu-ip4> 8080 <write-ip> 1 30000 2 8 1

<!-- Read ip does not matter for write client -->
**Write Client**: ./multiConstantWriteGenerator.sh v14.0.0-latency 1 1 8080 <read-eu-ip1> 8080 <write-ip> 1 50 500 8 10

**Read Client**: docker container cp multiBusyReadGenerator:/logs/ .
**Read Client**: scp -i "reference-architecture.pem" -r ubuntu@<read-client-dns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

## Scale out (3DCS + 3partitions) 
24 keys, 2 partitions, 3 regions, replication factor of 2

(Add read, write and client machine in OR(us-west-1))
(Setup replication 1 (EU to US), 2 (US to OR), 3 (OR to EU))

**Read Nodes**: 
    **EU**: ./readNode.sh v13.0.0-visibility 3 8080 1 3
    **US**: ./readNode.sh v13.0.0-visibility 3 8080 1 2
    **OR**: ./readNode.sh v13.0.0-visibility 3 8080 2 3

**Write Nodes**:
    **EU**: ./writeNode.sh v13.0.0-visibility 3 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 8080 <read-or-ip>
    **US**: ./writeNode.sh v13.0.0-visibility 3 8080 2 8080 <read-eu-ip> 8080 <read-us-ip> 8080 <read-or-ip>
    **OR**: ./writeNode.sh v13.0.0-visibility 3 8080 3 8080 <read-eu-ip> 8080 <read-us-ip> 8080 <read-or-ip>

#### Visibility + Latency Logs 9R:18W
**Clients**:
    **EU**: 
        **Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 3 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip3> 3 90000 2 8 9
        **Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 3 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip3> 3 50 1000 24 18
    **US**: 
        **Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 3 2 8080 <read-us-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 90000 2 8 9
        **Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 3 2 8080 <read-us-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 50 1000 24 18
    **OR**:
        **Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 3 2 8080 <read-or-ip> 8080 <write-ip2> 2 8080 <write-ip3> 3 90000 2 8 9
        **Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 3 2 8080 <read-or-ip> 8080 <write-ip2> 2 8080 <write-ip3> 3 50 1000 24 18

#### Goodput (3DCS + 3partitions) 20R:1W
**Clients**:
    **EU**:
        **Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 3 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip3> 3 50 30000 1 8 20
        **Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 3 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip3> 3 8 1
    **EU**:
        **Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 3 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip3> 3 50 30000 1 8 20
        **Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 3 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip3> 3 8 1
    **EU**:
        **Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 3 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip3> 3 50 30000 1 8 20
        **Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 3 2 8080 <read-eu-ip> 8080 <write-ip1> 1 8080 <write-ip3> 3 8 1

---

# Get Logs
## Latency and visibility
**Read Node**: docker container cp readNode:/logs/ .
**Write Node**: docker container cp writeNode:/logs/ .
**Read Client**: docker container cp multiBusyReadGenerator:/logs/ .
**Write Client**: docker container cp multiConstantWriteGenerator:/logs/ .

## Goodput
**Read Node**: docker container cp readNode:/logs/ .
**Read Client**: docker container cp multiConstantReadGenerator:/logs/ .

---
# Copy logs
## Latency and visibility
### EU
**Read Node**: scp -i "reference-architecture.pem" -r ubuntu@<read-eu-ip>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
**Write Node**: scp -i "reference-architecture.pem" -r ubuntu@<write-eu-ip>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
**Read Client**: scp -i "reference-architecture.pem" -r ubuntu@<read-client-eu-ip>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

### US
**Read Node**: scp -i "reference-architecture-us.pem" -r ubuntu@<read-eu-ip>.compute-1.amazonaws.com:~/logs ./logs-ref-arch
**Read Client**: scp -i "reference-architecture-us.pem" -r ubuntu@ec2-23-22-140-216.compute-1.amazonaws.com:~/logs ./logs-ref-arch
**Write Client**: scp -i "reference-architecture-us.pem" -r ubuntu@<write-client-us-ip>.compute-1.amazonaws.com:~/logs ./logs-ref-arch

## Goodput
**Read Node**: scp -i "reference-architecture.pem" -r ubuntu@<read-eu-dns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
**Read Client**: scp -i "reference-architecture.pem" -r ubuntu@<read-client-eu-dns>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
