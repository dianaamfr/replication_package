# Goodput Tests - CC prototype

- Read Delay = 50ms
- Push/Pull Rate = 5ms
- Read for 30s
- 10 keys per partition
- 2 keys per read
- 3 iterations

## Test 1 

### Reader EU-WEST-1 - 
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Busy Write Generator**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 10 1
**Constant Read Generator**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 30000 1 10 1 

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 2

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Busy Write Generator**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 10 1
**Constant Read Generator**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 30000 1 10 2 

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 3

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Busy Write Generator**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 10 1
**Constant Read Generator**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 30000 1 10 3 

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 
