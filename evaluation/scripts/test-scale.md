
docker rm -f $(docker ps -a -q)

(Delay = 50ms, Push/Pull Rate = 5ms, 1000 writes per client, Read for 60s in EU and 90s in US, 8 keys per partition, 2 keys per read)
- c6g.8xlarge clients
- t4.gsmall servers
- R:W: 1:2, 5:10, 10:20, 16:32 (16:3,29), 18:36 (18:7,29), 19:38 (19:9,29)

## Visibility + Latency Logs (1 partition)

### EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

**Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-node-1> 1 60000 2 8 19
**Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-node-1> 1 50 1000 8 9

**Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-node-1> 1 50 1000 8 29


### US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

**Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-node-1> 1 90000 2 8 19

---

## Goodput Logs (1 partition)
(Remover uma instance em EU, fica só uma)
- 3 iterations each
- R 10, 15, 20

### EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 3.253.82.239 8080 <read-us-ip>

**Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 3.253.82.239 8080 <write-node-1> 1 50 30000 1 8 <R>
**Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 3.253.82.239 8080 <write-node-1> 1 8 1

---
write-ip1 <write-node-1>
write-ip2
write-ip3
write-ip4

## Visibility + Latency Logs (2, 4 partitions) 5R:10W
### 2 partitions
**Read Nodes**: ./readNode.sh v13.0.0-visibility 2 8080 1 2

**Write Node EU**: ./writeNode.sh v13.0.0-visibility 2 8080 1 8080 3.253.82.239 8080 <read-us-ip>
**Write Node US**: ./writeNode.sh v13.0.0-visibility 2 8080 2 8080 3.253.82.239 8080 <read-us-ip>

**Read Client EU**: ./multiBusyReadGenerator.sh v13.0.0-visibility 2 2 8080 3.253.82.239 8080 <write-node-1> 1 8080 <write-node-2> 2 60000 2 4 5
**Write Client EU**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 2 2 8080 3.253.82.239 8080 <write-node-1> 1 8080 <write-node-2> 2 50 1000 4 10

**Read Client US**: ./multiBusyReadGenerator.sh v13.0.0-visibility 2 2 8080 <read-us-ip> 8080 <write-node-1> 1 8080 <write-node-2> 2 60000 2 4 5

### 4 partitions
**Read Nodes**: ./readNode.sh v13.0.0-visibility 4 8080 1 2 3 4

**Write Node1 eu**: ./writeNode.sh v13.0.0-visibility 4 8080 1 8080 3.253.82.239 8080 <read-us-ip>
**Write Node2 us**: ./writeNode.sh v13.0.0-visibility 4 8080 2 8080 3.253.82.239 8080 <read-us-ip>
**Write Node3 eu**: ./writeNode.sh v13.0.0-visibility 4 8080 3 8080 3.253.82.239 8080 <read-us-ip>
**Write Node4 us**: ./writeNode.sh v13.0.0-visibility 4 8080 4 8080 3.253.82.239 8080 <read-us-ip>

**Read Client EU**: ./multiBusyReadGenerator.sh v13.0.0-visibility 4 4 8080 3.253.82.239 8080 <write-node-1> 1 8080 <write-node-2> 2 8080 <write-node-3> 3 8080 <write-node-4> 4 60000 2 2 5
**Write Client EU**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 4 4 8080 3.253.82.239 8080 <write-node-1> 1 8080 <write-node-2> 2 8080 <write-node-3> 3 8080 <write-node-4> 4 50 1000 2 10

**Read Client US**: ./multiBusyReadGenerator.sh v13.0.0-visibility 4 4 8080 <read-us-ip> 8080 <write-node-1> 1 8080 <write-node-2> 2 8080 <write-node-3> 3 8080 <write-node-4> 4 60000 2 2 5

---

## Goodput (2, 4 partitions) 20R:1W
### 2 partitions
### EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 2 8080 1 2
**Write Node**: ./writeNode.sh v13.0.0-goodput 2 8080 1 8080 3.253.82.239 8080 <read-us-ip>
**Write Node**: ./writeNode.sh v13.0.0-goodput 2 8080 2 8080 3.253.82.239 8080 <read-us-ip>

**Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 2 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip2> 2 50 30000 1 4 <R>
**Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 2 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip2> 2 4 1

### 4 partitions
### EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 4 8080 1
**Write Node**: ./writeNode.sh v13.0.0-goodput 4 8080 1 8080 3.253.82.239 8080 <read-us-ip>

**Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 4 4 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip2> 2 8080 <write-ip3> 3 8080 <write-ip4> 4 50 30000 1 2 <R>
**Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 4 4 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip2> 2 8080 <write-ip3> 3 8080 <write-ip4> 4 2 1

---

## Scale out (3DCS + 3partitions) 
24 keys, 2 partitions, 3 regions, replication factor of 2

(Add read, write and client machine in OR(us-west-1))
(Setup replication 1 (EU to US), 2 (US to OR), 3 (OR to EU))

**Read Nodes**: 
    **EU**: ./readNode.sh v13.0.0-visibility 3 8080 1 3
    **US**: ./readNode.sh v13.0.0-visibility 3 8080 1 2
    **OR**: ./readNode.sh v13.0.0-visibility 3 8080 2 3

**Write Nodes**:
    **EU**: ./writeNode.sh v13.0.0-visibility 3 8080 1 8080 3.253.82.239 8080 <read-us-ip> 8080 <read-or-ip>
    **US**: ./writeNode.sh v13.0.0-visibility 3 8080 2 8080 3.253.82.239 8080 <read-us-ip> 8080 <read-or-ip>
    **OR**: ./writeNode.sh v13.0.0-visibility 3 8080 3 8080 3.253.82.239 8080 <read-us-ip> 8080 <read-or-ip>

#### Visibility + Latency Logs 9R:18W
**Clients**:
    **EU**: 
        **Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 3 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip3> 3 90000 2 8 9
        **Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 3 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip3> 3 50 1000 24 18
    **US**: 
        **Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 3 2 8080 <read-us-ip> 8080 <write-node-1> 1 8080 <write-ip2> 2 90000 2 8 9
        **Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 3 2 8080 <read-us-ip> 8080 <write-ip1> 1 8080 <write-ip2> 2 50 1000 24 18
    **OR**:
        **Read Client**: ./multiBusyReadGenerator.sh v13.0.0-visibility 3 2 8080 <read-or-ip> 8080 <write-ip2> 2 8080 <write-ip3> 3 90000 2 8 9
        **Write Client**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 3 2 8080 <read-or-ip> 8080 <write-ip2> 2 8080 <write-ip3> 3 50 1000 24 18

#### Goodput (3DCS + 3partitions) 20R:1W
**Clients**:
    **EU**:
        **Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 3 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip3> 3 50 30000 1 8 20
        **Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 3 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip3> 3 8 1
    **EU**:
        **Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 3 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip3> 3 50 30000 1 8 20
        **Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 3 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip3> 3 8 1
    **EU**:
        **Read Client**: ./multiConstantReadGenerator.sh v13.0.0-goodput 3 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip3> 3 50 30000 1 8 20
        **Write Client**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 3 2 8080 3.253.82.239 8080 <write-ip1> 1 8080 <write-ip3> 3 8 1

---

# Get Logs
## Latency and visibility
**Read Node**: docker container cp readNode:/logs/ . (2)
**Write Node**: docker container cp writeNode:/logs/ . (2)
**Read Client**: docker container cp multiBusyReadGenerator:/logs/ . (2)
**Write Client**: docker container cp multiConstantWriteGenerator:/logs/ . (1)

## Goodput
**Read Node**: docker container cp readNode:/logs/ . (1)
**Read Client**: docker container cp multiConstantReadGenerator:/logs/ . (1)

---
# Copy logs
## Latency and visibility
### EU
**Read Node**: scp -i "reference-architecture.pem" -r ubuntu@ec2-3-253-82-239.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
**Write Node**: scp -i "reference-architecture.pem" -r ubuntu@ec2-34-243-139-1.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
**Read Client**: scp -i "reference-architecture.pem" -r ubuntu@ec2-54-171-55-220.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
<-- **Write Client**: scp -i "reference-architecture.pem" -r ubuntu@ec2-3-249-76-207.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch -->

### US
**Read Node**: scp -i "reference-architecture-us.pem" -r ubuntu@ec2-18-215-155-3.compute-1.amazonaws.com:~/logs ./logs-ref-arch
**Read Client**: scp -i "reference-architecture-us.pem" -r ubuntu@ec2-54-146-151-35.compute-1.amazonaws.com:~/logs ./logs-ref-arch

ec2-54-89-128-238

scp -i "reference-architecture-us.pem" -r ubuntu@ec2-54-89-128-238.compute-1.amazonaws.com:~/logs ./logs-ref-arch

## Goodput
**Read Node**: scp -i "reference-architecture.pem" -r ubuntu@ec2-3-253-82-239.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
**Read Client**: scp -i "reference-architecture.pem" -r ubuntu@ec2-54-171-55-220.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch
---



