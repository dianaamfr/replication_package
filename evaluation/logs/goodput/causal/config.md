# Goodput Tests - CC prototype
- Push/Pull Rate = 5ms
- Read for 20s
- 1 key per read
- 3 iterations
- t4g.small / ubuntu / arm

## Test 1
- Read Delay = 50ms

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 a
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 20000 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Test 2
- Read Delay = 100ms

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 a
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 100 20000 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>

---
## Test 3
- Read Delay = 200ms

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v12.0.0-goodput 1 8080 1
**Busy Write Generator**: ./busyWriteGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 a
**Constant Read Generator**: ./constantReadGenerator.sh v12.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 500 20000 1 a

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v12.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip>
