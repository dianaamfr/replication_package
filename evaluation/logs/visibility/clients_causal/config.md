# Visibility Tests - CC prototype
- Write Delay = 50ms
- Push/Pull Rate = 5ms
- 500 writes per client
- Read for 120s
- 20 keys per partition 
- 2 keys per read

## Test 1 - 1 read client

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

### Client EU-WEST-1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 500 20 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 120000 2 20 1

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

### Client EU-EAST-1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 120000 2 20 1

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>  

## Test 2 - 2 read clients

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

### Client EU-WEST-1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 500 20 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 120000 2 20 2

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

### Client EU-EAST-1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 120000 2 20 2

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>  

## Test 3 - 300 clients (285 R, 15 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

### Client EU-WEST-1
**Constant Write Generator**: ./multiConstantWriteGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 500 20 1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 120000 2 20 3

### Reader US-EAST-1
**Read Node**: ./readNode.sh v13.0.0-visibility 1 8080 1

### Client EU-EAST-1
**Busy Read Generator**: ./multiBusyReadGenerator.sh v13.0.0-visibility 1 1 8080 <read-us-ip> 8080 <write-ip> 1 120000 2 20 3

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-visibility 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>  

## Test 4 - 6
2 client processes both using the setting in tests 1 to 3

## Test 7
Test the same but with a total of 1, 2, 5 and 10 partitions and a total of 20 keys (20, 10, 4 and 2 per partition)
---
# Get Logs
### Reader EU-WEST-1
docker container cp readNode:/logs/ .

### Client EU-WEST-1
docker container cp multiBusyReadGenerator:/logs/ .
docker container cp multiConstantWriteGenerator:/logs/ .

### Write Node EU-WEST-1
docker container cp writeNode:/logs/ .

### Reader US-EAST-1
docker container cp readNode:/logs/ .

### Client US-EAST-1
docker container cp multiBusyReadGenerator:/logs/ .

## Copy logs

scp -i "reference-architecture.pem" -r ubuntu@<read-eu-DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

scp -i "reference-architecture.pem" -r ubuntu@<read-us-DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

scp -i "reference-architecture.pem" -r ubuntu@<client-eu-DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

scp -i "reference-architecture.pem" -r ubuntu@<client-us-DNS>.eu-west-1.compute.amazonaws.com:~/logs ./logs-ref-arch

scp -i "reference-architecture-us.pem" -r ubuntu@<write-DNS>.compute-1.amazonaws.com:~/logs ./logs-ref-arch