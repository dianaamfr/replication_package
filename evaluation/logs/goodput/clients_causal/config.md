# Goodput Tests - CC prototype

- Read Delay = 50ms
- Push/Pull Rate = 5ms
- Read for 30s
- 10 keys per partition
- 1 key per read
- 3 iterations

## Test 1 - 100 clients (95 R, 5 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Busy Write Generator**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 10 5
**Constant Read Generator**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 30000 1 10 95 

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 2 -  500 clients (475 R, 25 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Busy Write Generator**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 10 25
**Constant Read Generator**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 30000 1 10 475 

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 3 - 900 clients (855 R, 45 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Busy Write Generator**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 10 45
**Constant Read Generator**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 30000 1 10 855 

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 4 - 1300 clients (1235 R, 65 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Busy Write Generator**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 10 65
**Constant Read Generator**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 30000 1 10 1235

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 5 - 1700 clients (1615 R, 85 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Busy Write Generator**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 10 85
**Constant Read Generator**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 30000 1 10 1615 

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 

## Test 6 - 2100 (1995 R, 105 W)

### Reader EU-WEST-1
**Read Node**: ./readNode.sh v13.0.0-goodput 1 8080 1
**Busy Write Generator**: ./multiBusyWriteGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 10 105
**Constant Read Generator**: ./multiConstantReadGenerator.sh v13.0.0-goodput 1 1 8080 <read-eu-ip> 8080 <write-ip> 1 50 30000 1 10 1995 

### Writer EU-WEST-1
**Write Node**: ./writeNode.sh v13.0.0-goodput 1 8080 1 8080 <read-eu-ip> 8080 <read-us-ip> 
