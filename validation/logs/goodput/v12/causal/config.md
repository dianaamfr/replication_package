# Goodput Tests - CC prototype

## Test 1
- Push/Pull Rate = 5ms
- 10 new reads
- Read Delay = 50ms
- 1 key per read
- 5 iterations

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 a
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 10 1 a

### Reader US-EAST-1
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <read-us-ip> 8080 <write-ip> 1 50 10 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

### Reader EU-WEST-1

---
## Test 2
- Push/Pull Rate = 5ms
- 10 new reads
- Read Delay = 100ms
- 1 key per read
- 5 iterations

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 a
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 100 10 1 a

### Reader US-EAST-1
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <read-us-ip> 8080 <write-ip> 1 100 10 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Test 3
- Push/Pull Rate = 5ms
- Reads during 60s (1min)
- Read Delay = 500ms
- 1 key per read
- 5 iterations

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 a
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 500 10 1 a

### Reader US-EAST-1
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <read-us-ip> 8080 <write-ip> 1 500 10 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>
